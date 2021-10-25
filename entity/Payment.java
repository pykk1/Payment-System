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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "debit_account", referencedColumnName = "id")
    private Account debitAccount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "credit_account", referencedColumnName = "id")
    private Account creditAccount;

    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String currency;
    private String reference;
    private String username;
    private String amount;
    private String status;
    private String debitAccountNumber;
    private String creditAccountNumber;
    private String type;
    private String externalBIC;
}
