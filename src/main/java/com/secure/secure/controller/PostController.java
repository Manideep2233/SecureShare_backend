package com.secure.secure.controller;

import com.secure.secure.dto.Input;
import com.secure.secure.dto.Output;
import com.secure.secure.entity.Comment;
import com.secure.secure.entity.Group;
import com.secure.secure.entity.Post;
import com.secure.secure.repository.GroupRepository;
import com.secure.secure.repository.PostRepository;
import com.secure.secure.repository.UserRepository;
import com.secure.secure.util.CustomException;
import com.secure.secure.util.ResponseMapper;
import com.secure.secure.util.ResponseObject;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/post")
public class PostController implements ResponseMapper {
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public GroupRepository groupRepository;
    @Autowired
    public PostRepository postRepository;

    public String getUsername(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username ="";
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }

    @PostMapping("/add/{groupId}/{message}")
    public ResponseObject createPost(@PathVariable(name ="message") String message,
                                     @PathVariable(name ="groupId") String groupId,
            //@RequestParam(name = "groupId") String groupId, @RequestParam String message,
            @RequestParam MultipartFile file) throws IOException {
        var user = userRepository.findByUsername(getUsername());
        var group = groupRepository.findById(groupId);
        Post post = new Post();
        post.setMessage(message);
        if(file!=null && !file.isEmpty()){
            post.setFileData(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
            post.setFileName(file.getOriginalFilename());
            post.setFileSize(file.getSize());
            post.setContentType(file.getContentType());
        }
        else{
            post.setFileData(null);
            post.setFileName("");
            post.setFileSize(0L);
            post.setContentType("");
        }
        post.setCreator(user.get());
        post.setGroup(group.get());
        post.setPostedTime(new Date());
        List<Comment> comments = new ArrayList<>();
        post.setComments(comments);
        var saved = postRepository.save(post);
        return successResponse("Added post: " + saved.getId() + " "+ post.getFileSize() );

        //todo uploaded size increase in group and user entity and validate also
    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject deletePost(@PathVariable(name ="id") String postId){
        var post = postRepository.findById(postId);
        if(!post.isPresent()){
            return errorResponse(new CustomException("Post not found"));
        }
        //todo uploaded size increase in group and user entity
        postRepository.delete(post.get());
        return successResponse("Deleted post");
    }

  /*  @DeleteMapping("/delete-user/{userId}")
    public ResponseObject deleteUserPosts(@PathVariable(name ="userId") String userId){
        var posts = postRepository.getAllUserPosts(userId);
        if(posts==null || posts.isEmpty()){
            return errorResponse(new CustomException("Posts not found"));
        }
        postRepository.deleteAll(posts);
        return successResponse("Deleted user posts...");
    }*/
/*
    @DeleteMapping("/delete-group/{groupId}")
    public ResponseObject deleteGroupPosts(@PathVariable(name ="groupId") String groupId){
        var posts = postRepository.getAllGroupPosts(groupId);
        if(posts==null || posts.isEmpty()){
            return errorResponse(new CustomException("Posts not found"));
        }
        postRepository.deleteAll(posts);
        return successResponse("Deleted Group posts...");
    }*/

    @PutMapping("/add-comment")
    public ResponseObject addComment(@RequestBody Input.Comment input){
        var post = postRepository.findById(input.postId());
        if(!post.isPresent()){
            return errorResponse(new CustomException("Post not found"));
        }
        Comment comment = new Comment();
        comment.setMessage(input.message());
        comment.setCommentedBy(input.commentedBy());
        comment.setCommentedTime(new Date());
        post.get().getComments().add(comment);
        postRepository.save(post.get());
        return successResponse("Added post...");
    }
    
    @GetMapping("/user")
    public ResponseObject getAllUserPosts(){
        var posts = postRepository.getAllUserPosts1(getUsername());
        if(posts==null || posts.isEmpty()){
            return errorResponse(new CustomException("Posts not found"));
        }
        return successResponse(
                posts.stream().map(x->new Output.postList(x.getId(),x.getMessage(),
                x.getFileName(),x.getFileSize(),x.getCreator().getUsername(),
                x.getPostedTime(),x.getComments())).toList()
        );
    }

    @GetMapping("/user/{groupId}")
    public ResponseObject getAllGroupPosts(@PathVariable(name ="groupId") String groupId){
        var group = groupRepository.findById(groupId);
        var user = userRepository.findByUsername(getUsername());
        if(!user.get().getUsername().equals("defaultAdmin") ||
                !group.get().getCreatedBy().equals(getUsername())){
            return errorResponse(new CustomException("Not Authorized"));
        }
        var posts = postRepository.getAllGroupPosts(groupId);
        return successResponse(
                posts.stream().map(x->new Output.postList(x.getId(),x.getMessage(),
                        x.getFileName(),x.getFileSize(),x.getCreator().getUsername(),
                        x.getPostedTime(),x.getComments())).toList()
        );
    }

    @GetMapping("/get-file/{postId}")
    public ResponseEntity<Resource> fileOfPost(@PathVariable(name ="postId") String postId){
        var post = postRepository.findById(postId).get();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(post.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + post.getFileName() + "\"")
                .body(new ByteArrayResource(post.getFileData().getData()));
    }
}
