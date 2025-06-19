package com.odorok.OdorokApplication.infrastructures.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "attractions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id")
    private Integer contentId;

    private String title;

    @Column(name = "first_image")
    private String firstImage;

    @Column(name = "first_image2")
    private String firstImage2;

    @Column(name = "map_level")
    private Integer mapLevel;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "tel")
    private String tel;

    @Column(name = "addr1")
    private String addr1;

    @Column(name = "addr2")
    private String addr2;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "overview")
    private String overview;

    @Column(name = "content_type_id")
    private Integer contentTypeId;

    @Column(name = "sido_code")
    private Integer sidoCode;

    @Column(name = "sigungu_code")
    private Integer sigunguCode;
}
