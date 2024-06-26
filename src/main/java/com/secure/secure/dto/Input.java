package com.secure.secure.dto;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

public class Input {
    public record Register(
            String fullName,
            String email,
            String username,
            String password,
            String confirmPassword) {
    }

    public record Login(
            String username,
            String password){
    }

    public  record  UpdateLimit(
            String userId,
            Long limit){
    }

    public record UpdateUser(
            String userId,
            String fullName,
            String email,
            String username,
            String password) {
    }

    public record CreateGroup(
            String groupName,
            String groupDescription) {
    }

    public record GroupDescription(
            String groupId,
            String groupDescription) {
    }

    public record UserGroup(
            String groupId,
            String userId){
    }

    public record Post(
            String groupId,
            String message){
    }

    public record Comment(
            String postId,
            String message,
            String commentedBy){
    }

    public record GroupRequest(
      String groupId
    ){
    }
}
