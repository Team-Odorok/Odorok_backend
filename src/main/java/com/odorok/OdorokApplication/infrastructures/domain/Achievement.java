package com.odorok.OdorokApplication.infrastructures.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "achievements")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "goal")
    private String goal;

    @Column(name = "reward")
    private Integer reward;

    @Column(name="img_url")
    private String imgUrl;
}
