package com.secure.secure.entity;

import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;
    private String fullName;
    private String username;
    private String email;
    private String password;
//    private Binary profilePhoto;
    private Long uploadLimit;
    private long totalUploadedSize;
    private String tempLoginToken;

}
