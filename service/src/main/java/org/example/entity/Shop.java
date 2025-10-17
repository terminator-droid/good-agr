package org.example.entity;

import lombok.Getter;

@Getter
public enum Shop {
    LAVKA("Лавка"),
    SAMOKAT("Самокат");

    private final String displayName;

    Shop(String displayName) {
        this.displayName = displayName;
    }

}