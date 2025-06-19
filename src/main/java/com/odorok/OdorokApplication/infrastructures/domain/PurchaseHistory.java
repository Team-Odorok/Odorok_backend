package com.odorok.OdorokApplication.infrastructures.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "purchase_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "bought_at")
    private LocalDateTime boughtAt;

    @Column(name = "cost")
    private Integer cost;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "item_id")
    private Long itemId;
}
