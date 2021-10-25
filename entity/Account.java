package com.montran.paymentsystem.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@Table(name = "accounts")
public class Account extends UsersAndAccountsSuper {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String number;
    private String currency;
    private String accountStatus;

}
