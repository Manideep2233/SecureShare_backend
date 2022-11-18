package com.secure.secure.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "groupRequest")
@Data
public class GroupJoinRequest {
    @Id
    private String id;
    private Group group;
    private User user;
}
