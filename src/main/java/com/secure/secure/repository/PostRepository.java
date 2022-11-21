package com.secure.secure.repository;

import com.secure.secure.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PostRepository  extends MongoRepository<Post,String > {

    @Query("{'creator.id':?0}")
    public List<Post> getAllUserPosts(String userId);

    @Query("{'creator.username':?0}")
    public List<Post> getAllUserPosts1(String username);


    @Query("{'group.id':?0}")
    public List<Post> getAllGroupPosts(String userId);

    @Query("{ 'group.id' : { $in : ?0}}")
    List<Post> findByGroupsList(List<String> groupId);
}
