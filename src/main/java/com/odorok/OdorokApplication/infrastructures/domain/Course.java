package com.odorok.OdorokApplication.infrastructures.domain;

import com.google.gson.JsonObject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "courses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Course implements JsonSettable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    @Column(name = "id")
    private Long id;

    @Column(name = "theme_id")
    private Long themeId;

    @Column(name = "idx")
    private String idx;

    @Column(name = "route_idx")
    private String routeIdx;

    @Column(name = "name")
    private String name;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "req_time")
    private Double reqTime;

    @Column(name = "level")
    private Integer level;

    @Column(name = "cycle")
    private Boolean cycle;

    @Column(name = "summary")
    private String summary;

    @Column(name = "contents")
    private String contents;

    @Column(name = "traveler_info")
    private String travelerInfo;

    @Column(name = "sido_code")
    private Integer sidoCode;

    @Column(name = "sigungu_code")
    private Integer sigunguCode;

    @Column(name = "brd_div")
    private Boolean brdDiv;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "reward")
    private Integer reward;



    @Override
    public void setWithJson(JsonObject object) {
        this.idx = object.get(PN_CRS_IDX).getAsString();
        this.routeIdx = object.get(PN_ROUTE_IDX).getAsString();
        this.distance = object.get(PN_CRS_DSTNC).getAsDouble();
        this.name = object.get(PN_CRS_KOR_NM).getAsString();
        this.level = object.get(PN_CRS_LEVEL).getAsInt();
        this.summary = object.get(PN_CRS_SUMMARY).getAsString();
        this.contents = object.get(PN_CRS_CONTENTS).getAsString();
        this.reqTime = object.get(PN_CRS_TOTL_RQRMHOUR).getAsDouble();
        this.travelerInfo = object.get(PN_TRAVELER_INFO).getAsString();
        this.cycle = object.get(PN_CRS_CYCLE).getAsString().equals("순환형");

        // 자전거 길 : DNBW
        this.brdDiv = object.get(PN_BRD_DIV).getAsString().equals("DNBW");
        this.createdAt = convertDateType(object.get(PN_CREATED_TIME).getAsString());
        this.modifiedAt = convertDateType(object.get(PN_MODIFIED_TIME).getAsString());

        this.reward = this.level * 300;
    }

    public static final String PN_ROUTE_IDX = "routeIdx";
    public static final String PN_CRS_IDX = "crsIdx";
    public static final String PN_CRS_KOR_NM = "crsKorNm";
    public static final String PN_CRS_DSTNC = "crsDstnc";
    public static final String PN_CRS_TOTL_RQRMHOUR = "crsTotlRqrmHour";
    public static final String PN_CRS_LEVEL = "crsLevel";
    public static final String PN_CRS_CYCLE = "crsCycle";
    public static final String PN_CRS_CONTENTS = "crsContents";
    public static final String PN_CRS_SUMMARY = "crsSummary";
    public static final String PN_CRS_TOUR_INFO = "crsTourInfo";
    public static final String PN_TRAVELER_INFO = "travelerinfo";
    public static final String PN_SIGUN = "sigun";
    public static final String PN_BRD_DIV = "brdDiv";
    public static final String PN_CREATED_TIME = "createdtime";
    public static final String PN_MODIFIED_TIME = "modifiedtime";
}
