package com.secure.secure.repository;

import com.secure.secure.entity.Group;
import com.secure.secure.entity.GroupJoinRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GroupJoinRequestRepository extends MongoRepository<GroupJoinRequest,String > {

//    @Query("{'user.username':?0}")
    @Query("{'user.username':?0}")
    public List<GroupJoinRequest> findUserGroupsRequests(String username);
    @Query("{'group.createdBy':?0}")
    public List<GroupJoinRequest> findUserGroupsRequests1(String username);
    @Query("{'group.id':?0}")
    public List<GroupJoinRequest> getRequest(String groupId);
}
