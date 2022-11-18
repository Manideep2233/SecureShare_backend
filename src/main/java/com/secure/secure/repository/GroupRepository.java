package com.secure.secure.repository;

import com.secure.secure.entity.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GroupRepository extends MongoRepository<Group, String> {

    @Query("{'groupMembers.id':?0}")
    public List<Group> findUserGroups(String userId);

    @Query("{'groupMembers.username':?0}")
    public List<Group> findUserGroups1(String username);
}

