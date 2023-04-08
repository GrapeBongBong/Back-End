package com.example.capstone.entity;

public enum PostType {
    T("Talent"),
    A("Anonymous");

    private final String type;

    PostType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}