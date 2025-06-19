package com.odorok.OdorokApplication.infrastructures.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_point")
    private Integer activityPoint;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "msg_frequency")
    private String msgFrequency;

    @Column(name = "msg_agree")
    private Boolean msgAgree;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "achievement1")
    private Long achievement1;

    @Column(name = "achievement2")
    private Long achievement2;

    @Column(name = "achievement3")
    private Long achievement3;

    @Column(name = "tier_id")
    private Long tierId;

    @Column(name = "sido_code")
    private Integer sidoCode;

    @Column(name = "sigungu_code")
    private Integer sigunguCode;

    @Column(name = "diary_id")
    private Long diaryId;
}
