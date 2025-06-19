package com.odorok.OdorokApplication.infrastructures.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "profile_deco")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileDeco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "item_id")
    private Long itemId;
}