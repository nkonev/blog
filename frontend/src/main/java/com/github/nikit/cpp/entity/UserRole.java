package com.github.nikit.cpp.entity;

public enum UserRole {
    // You shouldn't to change order of enum entries because these used in Hibernate's @Enumerated
    ROLE_ADMIN,
    ROLE_USER
}
