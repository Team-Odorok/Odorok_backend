package com.odorok.OdorokApplication.infrastructures.domain;

import jakarta.persistence.*;
        import lombok.AllArgsConstructor;
        import lombok.Builder;
        import lombok.Data;
        import lombok.NoArgsConstructor;

@Entity(name = "themes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theme")
    private String theme;
}
