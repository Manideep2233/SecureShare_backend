package com.secure.secure.dto;

import com.secure.secure.entity.Comment;

import java.util.List;

public class Output {
    public record GroupsList(
            String id,
            String name,
            String description){
    }

    public record postList(
            String postId,
            String message,
            String fileName,
            Long fileSize,
            String createdBy,
            String postedTime,
            List<Comment> comments){
    }

    public record Login(
            String userId,
            String role,
            String jwt){
    }

    public record JoinRequest(
            String reqId,
            String groupName,
            String username,
            String groupId,
            String userId){
    }
    public record MyProfile(
            String id,
            String fullName,
            String email,
            String username){
    }

    public record UsersList(
            String id,
            String username,
            String fullname,
            String utilized,
            String total){
    }


    public record GroupsListAll(
            String id,
            String name,
            String description,
            String utilized,
            String total){
    }

    public record ExploreList(
            String id,
            String name,
            String description,
            boolean isRequested){
    }

    public record myGroups(
            String groupId,
            String groupName,
            List<Output.Users> users
    ){}
    public record Users(
            String userid,
            String username){
    }
}
