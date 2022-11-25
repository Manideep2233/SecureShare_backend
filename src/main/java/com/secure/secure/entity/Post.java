package com.secure.secure.entity;

import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@Data
public class Post {
    @Id
    private String id;
    private String message;
    private Binary fileData;
    private Long fileSize;
    private String fileName;
    private String contentType;
    private User creator;
    private Group group;
    private String postedTime;
    private List<Comment> comments;


}
