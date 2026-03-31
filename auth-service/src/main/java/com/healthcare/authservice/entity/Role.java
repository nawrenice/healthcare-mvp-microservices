package com.healthcare.authservice.entity;

//this drives role-based access control (RABC) and is used later in spring security (hasRole())
public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN,
    RECEPTION
}
