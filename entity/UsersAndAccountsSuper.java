package com.montran.paymentsystem.entity;

import lombok.*;

import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@MappedSuperclass
public class UsersAndAccountsSuper {

    private String fullName;
    private String address;
    private String status;
    private String modified_by;
    private String approved_by;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
