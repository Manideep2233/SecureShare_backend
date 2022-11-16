package com.secure.secure.repository;

import com.secure.secure.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PostRepository  extends MongoRepository<Post,String > {

    @Query("{'creator.id':?0}")
    public List<Post> getAllUserPosts(String userId);

    @Query("{'group.id':?0}")
    public List<Post> getAllGroupPosts(String userId);
}
