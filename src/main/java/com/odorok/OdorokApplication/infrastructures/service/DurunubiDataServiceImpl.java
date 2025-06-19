package com.odorok.OdorokApplication.infrastructures.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.odorok.OdorokApplication.commons.webflux.util.WebClientUtil;
import com.odorok.OdorokApplication.infrastructures.domain.Course;
import com.odorok.OdorokApplication.infrastructures.domain.PathCoord;
import com.odorok.OdorokApplication.infrastructures.domain.Route;
import com.odorok.OdorokApplication.infrastructures.repository.CourseRepository;
import com.odorok.OdorokApplication.infrastructures.repository.PathCoordRepository;
import com.odorok.OdorokApplication.infrastructures.repository.RouteRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DurunubiDataServiceImpl implements DurunubiDataService{
    private final WebClient client;
    private final String DURUNUBI_URL_SCHEME = "https";
    private final String DURUNUBI_URL_HOST = "apis.data.go.kr";
    private final String DURUNUBI_URL_GIL_PATH = "/B551011/Durunubi/routeList";
    private final String DURUNUBI_URL_COURSE_PATH = "/B551011/Durunubi/courseList";
    private final String secretKey;

    private final CourseRepository courseRepository;
    private final RouteRepository gilRepository;
    private final PathCoordRepository pathCoordRepository;
    private final Map<String, String> DURUNUBI_PARAM_MAP;

    private final Integer INITIAL_LIST_SIZE = 7000;
    private final String CONTENTS_TAG_NAME = "item";
    private final String GPXPATH_PROP_NAME = "gpxpath";

    public DurunubiDataServiceImpl(@Autowired @Qualifier("durunubiClient") WebClient client,
                                   @Autowired CourseRepository courseRepository,
                                   @Autowired RouteRepository gilRepository,
                                   @Autowired PathCoordRepository pathCoordRepository,
                                   @Value("${external.keys.durunubi}") String secretKey) {
        this.client = client;
        this.courseRepository = courseRepository;
        this.gilRepository = gilRepository;
        this.pathCoordRepository = pathCoordRepository;
        this.secretKey = secretKey;
        log.debug("DURUNUBI serviceKey : {}",secretKey);
        DURUNUBI_PARAM_MAP = Map.of("MobileOS", "ETC", "MobileApp", "Odorok", "numOfRows", "8000","pageNo", "1", "_type", "json", "serviceKey", secretKey);
    }

    @Transactional
    @Override
    public void loadGilDatas() {
        String json = WebClientUtil.doGetBlock(client, DURUNUBI_URL_GIL_PATH, DURUNUBI_PARAM_MAP);
        log.debug("gil data json : {}", json);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray items = root.getAsJsonObject("response").getAsJsonObject("body").getAsJsonObject("items").getAsJsonArray("item");

        List<Route> totRoutes = new ArrayList<>(INITIAL_LIST_SIZE);
        for(JsonElement ele : items) {
            JsonObject item = ele.getAsJsonObject();

            Route route = new Route();
            route.setWithJson(item);
            log.debug("Parsed Route info : {}",route.toString());
            totRoutes.add(route);
        }
        gilRepository.saveAll(totRoutes); // saveAll의 결과로 모든 엔티티에 자동할당 값이 채워진다.
    }

    @Transactional
    @Override
    public void loadCourseDatas() {
        String json = WebClientUtil.doGetBlock(client, DURUNUBI_URL_COURSE_PATH, DURUNUBI_PARAM_MAP);

        log.debug("course data json : {}", json.substring(0, 1000));
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray items = root.getAsJsonObject("response").getAsJsonObject("body").getAsJsonObject("items").getAsJsonArray("item");

        for(JsonElement ele : items) {
            JsonObject item = ele.getAsJsonObject();

            Course course = new Course();
            course.setWithJson(item);

            // 시군구, 시도 코드 설정해야함.
            courseRepository.save(course);

            if(!item.has(GPXPATH_PROP_NAME)) continue;
            String gpxPath = item.get(GPXPATH_PROP_NAME).getAsString();

            loadGPX(gpxPath, course.getId());
        }
    }

    @Transactional
    @Override
    public void loadGPX(String url, Long crsId) {
        String gpxXml = client.get().uri(url).accept(MediaType.APPLICATION_XML).retrieve().bodyToMono(String.class).block();
        log.debug("path data xml : {}", gpxXml.substring(0, 1000));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
            DurunubiXmlHandler handler = new DurunubiXmlHandler();
            handler.init(crsId);

            parser.parse(new InputSource(new StringReader(gpxXml)), handler);

            List<PathCoord> track = handler.getCoords();
            pathCoordRepository.saveAll(track);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (SAXException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadToLocalDB() {
        loadGilDatas();
        loadCourseDatas();
        log.debug("로컬 DB로 업데이트 완료!");
    }

    @Override
    public void updateLocalDB() {
        throw new RuntimeException("아직 개발되지 않은 기능");
    }

    @Data
    static class DurunubiXmlHandler extends DefaultHandler {
        private final String TRACK_POINT_TAG_NAME = "trkpt";
        private final String LATITUDE_ATTR_NAME = "lat";
        private final String LONGITUDE_ATTR_NAME = "lon";
        private int ordering = 0;

        private List<PathCoord> coords;
        private Long crsId;

        public void init(long id) {
            coords = new ArrayList<>();
            this.crsId = id;
            ordering = 0;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase(TRACK_POINT_TAG_NAME)) {
                PathCoord coord = new PathCoord();
                coord.setLatitude(Double.parseDouble(attributes.getValue(LATITUDE_ATTR_NAME)));
                coord.setLongitude(Double.parseDouble(attributes.getValue(LONGITUDE_ATTR_NAME)));
                coord.setCourseId(crsId);
                coord.setOrdering(ordering++);
                coords.add(coord);
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadDataFromApiServerOnce() {
        log.debug("attempt to load datas from durunubi api -------------");
        loadToLocalDB();
    }
}
