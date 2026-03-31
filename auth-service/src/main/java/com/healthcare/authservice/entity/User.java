package com.healthcare.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity                 //makes this class as a database table
@Table(name = "users")  //avoids using reserved keyword user in DB.
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)     //auto-increment primary key
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)            //no duplicate accounts
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)               //Stores string(DOCTOR, PATIENT,ADMIN) instead of 0,1
    @Column(nullable = false)
    private Role role;


}
