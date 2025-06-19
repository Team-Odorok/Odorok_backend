package com.odorok.OdorokApplication.infrastructures.domain;

import com.google.gson.JsonObject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "routes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Route implements JsonSettable{
    @Override
    public void setWithJson(JsonObject object) {
        this.idx = object.get(PN_ROUTE_IDX).getAsString();
        this.name = object.get(PN_THEME_NM).getAsString();
        this.lineMsg = object.get(PN_LINE_MSG).getAsString();
        this.overview = object.get(PN_THEME_DESCS).getAsString();
        // 자전거 길 : DNBW
        this.brdDiv = object.get(PN_BRD_DIV).getAsString().equals("DNBW");
        this.createdAt = convertDateType(object.get(PN_CREATED_TIME).getAsString());
        this.modifiedAt = convertDateType(object.get(PN_MODIFIED_TIME).getAsString());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    @Column(name = "id")
    private Long id;

    @Column(name = "idx")
    private String idx;

    @Column(name = "name")
    private String name;

    @Column(name = "line_msg")
    private String lineMsg;

    @Column(name = "overview")
    private String overview;

    @Column(name = "brd_div")
    private Boolean brdDiv;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    private static final String PN_ROUTE_IDX = "routeIdx";
    private static final String PN_THEME_NM = "themeNm";
    private static final String PN_LINE_MSG = "linemsg";
    private static final String PN_THEME_DESCS = "themedescs";
    private static final String PN_BRD_DIV = "brdDiv";
    private static final String PN_CREATED_TIME = "createdtime";
    private static final String PN_MODIFIED_TIME = "modifiedtime";
}
