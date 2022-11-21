package com.secure.secure.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "groups")
@Data
public class Group {
    @Id
    private String id;
    private String groupName;
    private String groupDescription;
    private Date createdTime;
    private String createdBy;
    private List<User> groupMembers;
    private Long uploadLimit;
    private Long totalUploadedSize;
}
