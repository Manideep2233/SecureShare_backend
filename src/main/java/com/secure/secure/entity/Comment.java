package com.secure.secure.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private String message;
    private String commentedBy;
    private String commentedTime;
}
