package com.odorok.OdorokApplication.coursestatus.service;

import com.odorok.OdorokApplication.course.repository.PathCoordRepository;
import com.odorok.OdorokApplication.infrastructures.domain.PathCoord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseStatusServiceImpl implements CourseStatusService {

    private final PathCoordRepository pathCoordRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;

    // ===== 하버사인 =====
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dphi = Math.toRadians(lat2 - lat1);
        double dlam = Math.toRadians(lon2 - lon1);
        double s1 = Math.sin(dphi / 2.0);
        double s2 = Math.sin(dlam / 2.0);
        double a = s1 * s1 + Math.cos(phi1) * Math.cos(phi2) * s2 * s2;
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        return distanceKm(lat1, lon1, lat2, lon2) * 1000.0;
    }

    // ===== 로컬 평면(이퀴렉탱귤러) 좌표 변환: 단위 km =====
    private double[] localXY(double lat0, double lon0, double lat, double lon) {
        double x = Math.toRadians(lon - lon0) * Math.cos(Math.toRadians(lat0)) * EARTH_RADIUS_KM;
        double y = Math.toRadians(lat - lat0) * EARTH_RADIUS_KM;
        return new double[]{x, y};
    }

    // ===== 누적거리 테이블(km) =====
    private double[] buildCumulativeKm(List<double[]> path) {
        int n = path.size();
        double[] cum = new double[n];
        if (n == 0) return cum;
        cum[0] = 0.0;
        for (int i = 1; i < n; i++) {
            double[] a = path.get(i - 1), b = path.get(i);
            cum[i] = cum[i - 1] + distanceKm(a[0], a[1], b[0], b[1]);
        }
        return cum;
    }

    // ===== 현재 좌표를 가장 가까운 세그먼트에 스냅 =====
    private static class SnapResult {
        int segStartIndex0;  // 세그먼트 시작 인덱스(0-based, i → [i, i+1])
        double t;            // 세그먼트 내 진행비율(0~1)
        double crossKm;      // 경로까지 수직거리(km) → 이탈 판정 등에 사용
        double segLenKm;     // 세그먼트 길이(km)
    }

    private SnapResult findClosestSegment(List<double[]> path, double curLat, double curLon) {
        if (path.size() < 2) throw new IllegalArgumentException("path size must be >= 2");

        double lat0 = curLat, lon0 = curLon;
        double[] P = localXY(lat0, lon0, curLat, curLon);
        double Px = P[0], Py = P[1];

        double bestCross = Double.POSITIVE_INFINITY;
        int bestI = 0;
        double bestT = 0.0;
        double bestSegLenKm = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            double[] Ageo = path.get(i), Bgeo = path.get(i + 1);
            double[] A = localXY(lat0, lon0, Ageo[0], Ageo[1]);
            double[] B = localXY(lat0, lon0, Bgeo[0], Bgeo[1]);
            double Ax = A[0], Ay = A[1], Bx = B[0], By = B[1];

            double vx = Bx - Ax, vy = By - Ay;
            double wx = Px - Ax, wy = Py - Ay;
            double vv = vx * vx + vy * vy;
            if (vv == 0.0) continue; // A==B 보호

            double tRaw = (wx * vx + wy * vy) / vv;
            double t = Math.max(0.0, Math.min(1.0, tRaw)); // 선분 클램프

            double Cx = Ax + t * vx, Cy = Ay + t * vy;
            double cross = Math.hypot(Px - Cx, Py - Cy);   // km(평면근사)

            if (cross < bestCross) {
                bestCross = cross;
                bestI = i;
                bestT = t;
                bestSegLenKm = distanceKm(Ageo[0], Ageo[1], Bgeo[0], Bgeo[1]);
            }
        }

        SnapResult r = new SnapResult();
        r.segStartIndex0 = bestI;
        r.t = bestT;
        r.crossKm = bestCross;
        r.segLenKm = bestSegLenKm;
        return r;
    }

    // ===== 외부 제공: 진행거리(미터) =====
    @Override
    public double getTraveledMeters(long courseId, double curLat, double curLon) {
        List<PathCoord> coords = pathCoordRepository.findByCourseId(courseId);
        if (coords == null || coords.isEmpty()) return 0.0;

        // 엔티티 → 경로 배열 [lat, lon]
        List<double[]> path = new ArrayList<>(coords.size());
        for (var c : coords) {
            // c.getLatitude(), c.getLongitude()는 실제 필드명으로 맞춰주세요
            path.add(new double[]{c.getLatitude(), c.getLongitude()});
        }
        if (path.size() == 1) {
            // 시작점만 있는 경우: 시작점→현재까지 거리 반환(선택)
            return distanceMeters(path.get(0)[0], path.get(0)[1], curLat, curLon);
        }

        // 누적거리 테이블(km) & 스냅
        double[] cumKm = buildCumulativeKm(path);
        SnapResult snap = findClosestSegment(path, curLat, curLon);

        // 시작점→세그먼트 시작점까지 누적 + 세그먼트 내 진행량
        double traveledKm = cumKm[snap.segStartIndex0] + snap.segLenKm * snap.t;

        // 과도/음수 방지
        if (traveledKm < 0) traveledKm = 0;
        double totalKm = cumKm[cumKm.length - 1];
        if (traveledKm > totalKm) traveledKm = totalKm;

        return traveledKm * 1000.0; // 미터로 반환
    }
}