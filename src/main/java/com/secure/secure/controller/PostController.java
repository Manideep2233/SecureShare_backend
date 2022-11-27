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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            @RequestParam MultipartFile file) throws IOException {
        Pattern p2 = Pattern.compile("[^<>*+?:;{}'' ]", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(message);
        if(!m2.find()){
            return errorResponse(new CustomException("Post Description does not allow Special Characters  <>*?:;{+}''"));
        }
        if(message.length()>500){
            return errorResponse(new CustomException("A Post Description can have maximum of 500 characters..."));
        }
        var filename = file.getOriginalFilename();
        String regex = "([^\\s]+(\\.(?i)(jpe?g|png|gif|txt))$)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(filename);
        if(!m.matches()){
            return errorResponse(new CustomException("Not Valid, PNG | JPG | JPEG | GIF | TXT"));
        }
        var user = userRepository.findByUsername(getUsername());
        var group = groupRepository.findById(groupId);
        Post post = new Post();
        post.setMessage(message);
        if(file==null || file.isEmpty()){
            return errorResponse(new CustomException("Please select an non-empty file for creating a post"));
        }
        if(file.getSize()>5*1024*1000){
            return errorResponse(new CustomException("Cannot create a file of size more than 5MB."));
        }
        post.setFileData(new Binary(BsonBinarySubType.BINARY, file.getBytes()));

        post.setFileName(file.getOriginalFilename());
        post.setFileSize(file.getSize());
        post.setContentType(file.getContentType());
        var size_u = user.get().getTotalUploadedSize() + file.getSize();
        if(size_u>user.get().getUploadLimit()){
            return errorResponse(new CustomException("Cannot Post, You will be exceeding your limit"));
        }
        var size_g = group.get().getTotalUploadedSize()+ file.getSize();
        if(size_g>group.get().getUploadLimit()){
            return errorResponse(new CustomException("Cannot Post, Upload limit for Group will be exceeded"));
        }
        user.get().setTotalUploadedSize(user.get().getTotalUploadedSize() + file.getSize());
        group.get().setTotalUploadedSize(group.get().getTotalUploadedSize()+ file.getSize());
        post.setCreator(user.get());
        post.setGroup(group.get());
        post.setPostedTime((new Date()).toString());
        List<Comment> comments = new ArrayList<>();
        post.setComments(comments);
        var saved = postRepository.save(post);
        userRepository.save(user.get());
        groupRepository.save(group.get());
        return successResponse("Added post: " + saved.getId() + " "+ post.getFileSize() );

    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject deletePost(@PathVariable(name ="id") String postId){
        var post = postRepository.findById(postId);
        if(!post.isPresent()){
            return errorResponse(new CustomException("Post not found"));
        }
        if(!post.get().getFileName().equals("")){
            var user = userRepository.findByUsername(post.get().getCreator().getUsername());
            var group = groupRepository.findById(post.get().getGroup().getId());

            user.get().setTotalUploadedSize(user.get().getTotalUploadedSize()-post.get().getFileSize());
            group.get().setTotalUploadedSize(group.get().getTotalUploadedSize()-post.get().getFileSize());
            userRepository.save(user.get());
            groupRepository.save(group.get());
        }
        postRepository.delete(post.get());
        return successResponse("Deleted post");
    }

    @PutMapping("/add-comment")
    public ResponseObject addComment(@RequestBody Input.Comment input){
        var user = userRepository.findByUsername(getUsername());
        var post = postRepository.findById(input.postId());
        if(!post.isPresent()){
            return errorResponse(new CustomException("Post not found"));
        }
        Pattern p2 = Pattern.compile("[^<>*+?:;{}'' ]", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(input.message());
        if(!m2.find()){
            return errorResponse(new CustomException("Comments does not accept Special Characters  <>*?:;{+}''"));
        }
        if(input.message().length()>500){
            return errorResponse(new CustomException("A comment can have maximum of 500 characters..."));
        }
        Comment comment = new Comment();
        comment.setMessage(input.message());
        comment.setCommentedBy(user.get().getUsername());
        comment.setCommentedTime(new Date().toString());
        post.get().getComments().add(comment);
        postRepository.save(post.get());
        return successResponse("Added Comment...");
}
    
    @GetMapping("/user")
    public ResponseObject getAllUserPosts(){
        var posts = postRepository.getAllUserPosts1(getUsername());
        if(posts==null || posts.isEmpty()){
            return errorResponse(new CustomException("Posts not found"));
        }
        return successResponse(
                posts.stream().map(x->new Output.postList(x.getId(),x.getMessage(),
                x.getFileName(), (long) Math.ceil((double)x.getFileSize()/1000),x.getCreator().getUsername(),
                x.getPostedTime(),x.getComments())).toList()
        );
    }

    @GetMapping("/user-all")
    public ResponseObject getProfilePosts(){
        List<Group> userGroups = groupRepository.findUserGroups1(getUsername());
        List<String> groupIds = userGroups.stream().map(Group::getId).toList();
        List<Post> posts = postRepository.findByGroupsList(groupIds);
        return successResponse(
                posts.stream().map(x->new Output.postList(x.getId(),x.getMessage(),
                        x.getFileName(),(long) Math.ceil((double)x.getFileSize()/1000),x.getCreator().getUsername(),
                        x.getPostedTime(),x.getComments())).toList()
        );
    }

    @GetMapping("/user/{groupId}")
    public ResponseObject getAllGroupPosts(@PathVariable(name ="groupId") String groupId){
        var posts = postRepository.getAllGroupPosts(groupId);
        return successResponse(
                posts.stream().map(x->new Output.postList(x.getId(),x.getMessage(),
                        x.getFileName(), (long) Math.ceil((double)x.getFileSize()/1000),x.getCreator().getUsername(),
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
