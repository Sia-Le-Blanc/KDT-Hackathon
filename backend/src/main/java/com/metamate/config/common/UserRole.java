package com.metamate.config.common;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserRole
{
    applicant, employee, manager, admin;

    @JsonCreator
    public static UserRole from(String value) {
        return value == null ? null : UserRole.valueOf(value.toUpperCase());
    }
}
