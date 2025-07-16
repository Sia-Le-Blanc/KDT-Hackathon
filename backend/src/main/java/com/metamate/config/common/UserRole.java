package com.metamate.config.common;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserRole
{
    APPLICANT, EMPLOYEE, MANAGER, ADMIN;

    @JsonCreator
    public static UserRole from(String value) {
        return value == null ? null : UserRole.valueOf(value.toUpperCase());
    }
}
