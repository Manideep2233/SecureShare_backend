package com.secure.secure.controller;

import com.secure.secure.config.JwtUtility;
import com.secure.secure.config.UserService;
import com.secure.secure.dto.Input;
import com.secure.secure.dto.Output;
import com.secure.secure.entity.GroupJoinRequest;
import com.secure.secure.entity.User;
import com.secure.secure.repository.GroupJoinRequestRepository;
import com.secure.secure.repository.GroupRepository;
import com.secure.secure.repository.PostRepository;
import com.secure.secure.repository.UserRepository;
import com.secure.secure.util.CustomException;
import com.secure.secure.util.ResponseMapper;
import com.secure.secure.util.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController implements ResponseMapper {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupJoinRequestRepository groupJoinRequestRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private PostRepository postRepository;

    @PostMapping("/login")
    public ResponseObject login(@RequestBody Input.Login input) throws IOException, CustomException {

        if(input.username()==null || input.username().isEmpty()){
            return errorResponse(new CustomException("Enter valid Username."));
        }
        if(input.password()==null || input.password().isEmpty()){
            return errorResponse(new CustomException("Enter valid Password."));
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.username(),
                            input.password()
                    )
            );
        }
        catch (Exception e){
            return errorResponse(new CustomException("BAD CREDENTIALS"));
        }

        final UserDetails userDetails= userService.loadUserByUsername(input.username());
        Optional<User> user = userRepository.findByUsername(input.username());
        final String token = jwtUtility.generateToken(userDetails);
        String uRole = user.get().getUsername().equals("defaultAdmin")?"ADMIN":"GUEST";
        return successResponse(new Output.Login(user.get().getUsername(), uRole,token));
    }

    @PostMapping("/register")
    public ResponseObject save(@RequestBody Input.Register input) throws IOException, CustomException {
        if(!input.password().equals(input.confirmPassword())){
            return errorResponse(new CustomException("Password did not match"));
        }
        //todo validate each input and add limit to each input
        //todo check if user exist with same username
        var user = new User();
        user.setFullName(input.fullName());
        user.setUsername(input.username());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));
        user.setUploadLimit(50000L); //50 MB
        var saved = userRepository.save(user);
        var res = login(new Input.Login(saved.getUsername(),input.password()));
        return successResponse(res.getResponse());
    }


    @DeleteMapping("/delete/{id}")
    public ResponseObject deleteUser(@PathVariable(name ="id") String userId){
        postRepository.deleteAll(postRepository.getAllUserPosts1(getUsername()));
        userRepository.deleteById(userId);
        return successResponse("Successfully deleted the user");
    }

    @PutMapping("/update-limit")
    public ResponseObject updateLimit(@RequestBody Input.UpdateLimit input){
    //todo validate limit and check max size
        var user = userRepository.findById(input.userId());
        if(!user.isPresent()){
            return errorResponse(new CustomException("No user with userId:" + input.userId()));
        }
        user.get().setUploadLimit(input.limit());
        userRepository.save(user.get());
        return successResponse("Updated limit for User:"+ input.limit());
    }

    @PutMapping("/update-profile")
    public ResponseObject updateProfile(@RequestBody Input.UpdateUser input){
        var user = userRepository.findById(input.userId());
        if(!user.isPresent()){
            return errorResponse(new CustomException("User not Found!"));
        }
        //todo validate each input and add limit to each input
        user.get().setFullName(input.fullName());
        user.get().setUsername(input.username());
        user.get().setEmail(input.email());
        user.get().setPassword(passwordEncoder.encode(input.password()));
        var saved = userRepository.save(user.get());

        return successResponse("Successfully updated your profile user" + saved.getId());
    }
        //todo default admin creation is pending

    public void addAdmin(){
        if(!userRepository.findByUsername("defaultAdmin").isPresent()){
            var user = new User();
            user.setFullName("default admin");
            user.setEmail("default@Admin.com");
            user.setUsername("defaultAdmin");
            user.setPassword(passwordEncoder.encode("defaultAdmin123"));
            user.setUploadLimit(500000L);
            userRepository.save(user);
        }
    }

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

    @GetMapping("/notifications")
    public ResponseObject viewRequests(){
        List<GroupJoinRequest> list = groupJoinRequestRepository.findUserGroupsRequests(getUsername());
        return successResponse(list.stream().map(x->new Output.JoinRequest(x.getId(),x.getGroup().getGroupName(),
                x.getUser().getUsername(),x.getGroup().getId(),x.getUser().getId())));
    }

    @PutMapping("/req-accept/{id}")
    public ResponseObject acceptRequest(@PathVariable(name ="id") String reqId){

        var req = groupJoinRequestRepository.findById(reqId);
        if(!getUsername().equals(req.get().getGroup().getCreatedBy())){
            return errorResponse(new CustomException("Not Authorized"));
        }
        var group = groupRepository.findById(req.get().getGroup().getId());
        var user = userRepository.findByUsername(req.get().getUser().getId());
        if(!user.get().getUsername().equals("defaultAdmin") ||
                !group.get().getCreatedBy().equals(getUsername())){
            return errorResponse(new CustomException("Not Authorized"));
        }
        group.get().getGroupMembers().add(user.get());
        groupJoinRequestRepository.delete(req.get());
        groupRepository.save(group.get());
        return successResponse("Added User to group");
    }

    @PutMapping("/req-delete/{id}")
    public ResponseObject rejectRequest(@PathVariable(name ="id") String reqId){
        var req = groupJoinRequestRepository.findById(reqId);
        if(!getUsername().equals(req.get().getGroup().getCreatedBy())){
            return errorResponse(new CustomException("Not Authorized"));
        }
        groupJoinRequestRepository.delete(req.get());
        return successResponse("Deleted Request");
    }

}
