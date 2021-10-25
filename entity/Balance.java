package com.montran.paymentsystem.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "balances")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private String available = "0";
    private String pendingDebit = "0";
    private String pendingCredit = "0";
    private String amountDebit = "0";
    private String amountCredit = "0";
    private String countDebit = "0";
    private String countCredit = "0";
    private String credit = "0";
    private String debit = "0";
    private String projected = "0";


}
