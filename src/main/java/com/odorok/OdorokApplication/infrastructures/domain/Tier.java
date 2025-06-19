package com.odorok.OdorokApplication.infrastructures.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "tiers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "level")
    private Integer level;

    @Column(name = "bound")
    private Integer bound;

}
