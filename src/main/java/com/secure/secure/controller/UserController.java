package com.secure.secure.controller;

import com.secure.secure.config.JwtUtility;
import com.secure.secure.config.UserService;
import com.secure.secure.dto.Input;
import com.secure.secure.dto.Output;
import com.secure.secure.entity.Group;
import com.secure.secure.entity.GroupJoinRequest;
import com.secure.secure.entity.User;
import com.secure.secure.repository.GroupJoinRequestRepository;
import com.secure.secure.repository.GroupRepository;
import com.secure.secure.repository.PostRepository;
import com.secure.secure.repository.UserRepository;
import com.secure.secure.util.CustomException;
import com.secure.secure.util.ResponseMapper;
import com.secure.secure.util.ResponseObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/user")
public class UserController implements ResponseMapper {

    @Value("${jwt.secret}")
    private String secretKey;
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
    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String toEmail,
                                String subject,
                                String body
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("secureshare@gmail.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        mailSender.send(message);
        System.out.println("Mail Send...");
    }

    @PostMapping("/validate-login")
    public ResponseObject validateLogin(@RequestBody Input.Login input){
        if(input.username()==null || input.username().isEmpty()){
            return errorResponse(new CustomException("Enter valid Username."));
        }
        if(input.password()==null || input.password().isEmpty()){
            return errorResponse(new CustomException("Enter valid Password."));
        }
        var isvalid = validate("Sample Name",input.username(),"Random@223");
        if(isvalid.getStatus().equals("ERROR")){
            return errorResponse(new CustomException(isvalid.getResponse().toString()));
        }
        Optional<User> user = userRepository.findByUsername(input.username());
        if(user.get().getTempLoginToken() == null){
            return errorResponse(new CustomException("Invalid Token"));
        }

        String regex = "[a-zA-Z]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input.password());
        boolean valid = true;
        while(!m.find()) {
            valid=false;
        }
        if(!valid){
            return errorResponse(new CustomException("Token should contain a-z A-Z 0-9"));
        }
        if(input.password().length()!=8){
            return errorResponse(new CustomException("Token is 8 character string."));
        }
        if(!passwordEncoder.matches(input.password(),user.get().getTempLoginToken())){
            return errorResponse(new CustomException("Token did not match"));
        }
        user.get().setTempLoginToken(null);
        userRepository.save(user.get());
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            input.username(),
//                            user.get().getPassword()
//                    )
//            );
//        }
//        catch (Exception e){
//            return errorResponse(new CustomException("BAD CREDENTIALS"));
//        }
        final UserDetails userDetails= userService.loadUserByUsername(input.username());
        final String token = jwtUtility.generateToken(userDetails);
        String uRole = user.get().getUsername().equals("Manideep223")?"ADMIN":"GUEST";
        return successResponse(new Output.Login(user.get().getUsername(), uRole,token));

    }

    @PostMapping("/login")
    public ResponseObject login(@RequestBody Input.Login input) throws IOException, CustomException {

        if(input.username()==null || input.username().isEmpty()){
            return errorResponse(new CustomException("Enter valid Username."));
        }
        if(input.password()==null || input.password().isEmpty()){
            return errorResponse(new CustomException("Enter valid Password."));
        }
        var isvalid = validate("Sample Name",input.username(),input.password());
        if(isvalid.getStatus().equals("ERROR")){
            return errorResponse(new CustomException(isvalid.getResponse().toString()));
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
        //here send email after generating token.
        String token = RandomStringUtils.random(8, true, true);
        Optional<User> user = userRepository.findByUsername(input.username());
        user.get().setTempLoginToken(passwordEncoder.encode(token));
        userRepository.save(user.get());
        sendSimpleEmail(user.get().getEmail(),"Token for Login",token);
        return successResponse("A mail with token is sent to your email, please enter it to continue.");

//        final UserDetails userDetails= userService.loadUserByUsername(input.username());
////        Optional<User> user = userRepository.findByUsername(input.username());
//        final String token = jwtUtility.generateToken(userDetails);
//        String uRole = user.get().getUsername().equals("defaultAdmin")?"ADMIN":"GUEST";
//        return successResponse(new Output.Login(user.get().getUsername(), uRole,token));
    }

    @PostMapping("/register")
    public ResponseObject save(@RequestBody Input.Register input) throws IOException, CustomException {
        if(input.username()==null || input.username().isEmpty()){
            return errorResponse(new CustomException("Enter valid Username."));
        }
        if(input.password()==null || input.password().isEmpty()){
            return errorResponse(new CustomException("Enter valid Password."));
        }
        if(input.email()==null || input.email().isEmpty()){
            return errorResponse(new CustomException("Enter valid Email."));
        }
        if(input.fullName()==null || input.fullName().isEmpty()){
            return errorResponse(new CustomException("Enter valid FullName."));
        }
        if(!input.password().equals(input.confirmPassword())){
            return errorResponse(new CustomException("Password did not match"));
        }
        var isvalid = validate(input.fullName(),input.username(),input.password());
        if(isvalid.getStatus().equals("ERROR")){
            return errorResponse(new CustomException(isvalid.getResponse().toString()));
        }
        //OWASP Validation Regular Expression for email
        var emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if(!Pattern.compile(emailRegex)
                .matcher(input.email())
                .matches()){
            return errorResponse(new CustomException("Invalid Email format."));
        }

        var userInDb = userRepository.findByUsernameIgnoreCase(input.username().toLowerCase());
        if(!userInDb.isEmpty()){
            return errorResponse(new CustomException("Username is already taken"));
        }
        var emailIOnDb = userRepository.findByEmailIgnoreCase(input.email());
        if(!emailIOnDb.isEmpty()){
            return errorResponse(new CustomException("Email is already Used"));
        }
        //todo validate each input
        //todo check if user exist with same username
        var user = new User();
        user.setFullName(input.fullName());
        user.setUsername(input.username());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));
        user.setUploadLimit(50*1024*1000L); //50 MB
        var saved = userRepository.save(user);
//        var res = login(new Input.Login(saved.getUsername(),input.password()));
//        return successResponse(res.getResponse());
        return successResponse("User Register Successfully!, Please Login and continue using the application.");
    }

    @PutMapping("/update-profile")
    public ResponseObject updateProfile(@RequestBody Input.UpdateUser input){
        if(input.username()==null || input.username().isEmpty()){
            return errorResponse(new CustomException("Enter valid Username."));
        }
        if(input.email()==null || input.email().isEmpty()){
            return errorResponse(new CustomException("Enter valid Email."));
        }
        if(input.fullName()==null || input.fullName().isEmpty()){
            return errorResponse(new CustomException("Enter valid FullName."));
        }

        var isvalid = validate(input.fullName(),input.username(),"Sample@223");
        if(isvalid.getStatus().equals("ERROR")){
            return errorResponse(new CustomException(isvalid.getResponse().toString()));
        }
        //OWASP Validation Regular Expression for email
        var emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if(!Pattern.compile(emailRegex)
                .matcher(input.email())
                .matches()){
            return errorResponse(new CustomException("Invalid Email format."));
        }


        var user = userRepository.findById(input.userId());
        if(!user.isPresent()){
            return errorResponse(new CustomException("User not Found!"));
        }
        if(!user.get().getUsername().equals(input.username())){
            var userInDb = userRepository.findByUsernameIgnoreCase(input.username().toLowerCase());
            if(!userInDb.isEmpty()){
                return errorResponse(new CustomException("Username is already taken"));
            }
        }
        if(!user.get().getEmail().equals(input.email())){
            var emailIOnDb = userRepository.findByEmailIgnoreCase(input.email());
            if(!emailIOnDb.isEmpty()){
                return errorResponse(new CustomException("Email is already Used"));
            }
        }
        //todo validate each input and add limit to each input
        user.get().setFullName(input.fullName());
        user.get().setUsername(input.username());
        user.get().setEmail(input.email());
        var saved = userRepository.save(user.get());

        return successResponse("Successfully updated your profile user" + saved.getId());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject deleteUser(@PathVariable(name ="id") String userId){
        var userToDelete = userRepository.findById(userId);
        var userGroups = groupRepository.findUserGroups(userId);
        if(!userGroups.isEmpty()){
            userGroups.forEach(x-> x.getGroupMembers().remove(userToDelete.get()));
            groupRepository.saveAll(userGroups);
            var userPosts = postRepository.getAllUserPosts1(getUsername());
            if(!userPosts.isEmpty()){
                List<Group> groupsToUpdate = new ArrayList<>();
                userPosts.forEach(x-> {
                            var group = x.getGroup();
                            group.setTotalUploadedSize(group.getTotalUploadedSize()-x.getFileSize());
                            groupsToUpdate.add(group);
                        });
                groupRepository.saveAll(groupsToUpdate);
                postRepository.deleteAll(userPosts);
            }
        }
        userRepository.deleteById(userId);
        return successResponse("Successfully deleted the user");
    }

    @PutMapping("/update-limit")
    public ResponseObject updateLimit(@RequestBody Input.UploadRequest input){
    //todo validate limit and check max size
        var user = userRepository.findById(input.id());
        if(!user.isPresent()){
            return errorResponse(new CustomException("User is Not Found"));
        }
        if(user.get().getUploadLimit()> input.updateSize()*1024*1000){
            return errorResponse(new CustomException("Updating limit is lessthan current limit"));
        }
        if(input.updateSize()*1024*1000 > 100*1024*1000){
            return errorResponse(new CustomException("Maximum Upload size for a User is 100MB"));
        }
        user.get().setUploadLimit(input.updateSize()*1024*1000);
        userRepository.save(user.get());
        return successResponse("Now User upload limit is increased to "+ input.updateSize() + " MB");
    }


        //todo default admin creation is pending

    public void addAdmin(){
        if(!userRepository.findByUsername("Manideep223").isPresent()){
            var user = new User();
            user.setFullName("Manideep Shanigaram");
            user.setEmail("shanigarammanideep223@gmail.com");
            user.setUsername("Manideep223");
            user.setPassword(passwordEncoder.encode("Manideep@223"));
            user.setUploadLimit(500000L);
            user.setTotalUploadedSize(0L);
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
        List<GroupJoinRequest> list = groupJoinRequestRepository.findUserGroupsRequests1(getUsername());
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
//        var user = userRepository.findByUsername(req.get().getUser().getId());
//        if(!user.get().getUsername().equals("defaultAdmin") ||
//                !group.get().getCreatedBy().equals(getUsername())){
//            return errorResponse(new CustomException("Not Authorized"));
//        }
        group.get().getGroupMembers().add(req.get().getUser());
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
    @GetMapping("/my-profile")
    public ResponseObject getProfile(){
        var user = userRepository.findByUsername(getUsername());
        return successResponse(new Output.MyProfile(user.get().getId(),
                user.get().getFullName(),
                user.get().getEmail(),
                user.get().getUsername()));
    }

    @GetMapping("/all")
    public ResponseObject getAllUsers(){
        return  successResponse(
        userRepository.findAll().stream()
                .filter(x->!x.getUsername().equals("Manideep223"))
                .map(x->
                        new Output.UsersList(
                                x.getId(),
                                x.getUsername(),
                                x.getFullName(),
                                Math.round(x.getTotalUploadedSize()/(1024*1000)) +" MB",
                                Math.round(x.getUploadLimit()/(1024*1000)) + " MB"
                        ))
                .toList());
    }

    private ResponseObject validate(String fullName, String username, String password){

        Pattern p1 = Pattern.compile("[^<>:{}'',~!@#$%+^&*(){}?; ]", Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(fullName);
        if(!m1.find()){
            return errorResponse(new CustomException("Full name does not allow Special Characters  <>:{}'',~!@#$%^&*()+{}?;"));
        }
        if(fullName.length()>80){
            return errorResponse(new CustomException("Max length for Full name is 80 characters."));
        }

        Matcher m2 = p1.matcher(username);
        if(!m2.find()){
            return errorResponse(new CustomException("username does not allow Special Characters  <>:{}'',~!@#$%^&*(){}?;"));
        }
        if(username.length()>16){
            return errorResponse(new CustomException("Max length for username is 16 characters."));
        }

        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if(!m.matches()){
            return errorResponse(new CustomException("Password is not satisfying requirement criteria"));
        }

        return successResponse("Validated");
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){

        String authorization = request.getHeader("Authorization");
        if(null!=authorization){
            authorization = authorization.substring(7,authorization.length());
        }
        final Date createdDate = new Date();

        final Claims claims = jwtUtility.getAllClaimsFromToken(authorization);
        claims.setIssuedAt(createdDate);
        claims.setExpiration(new Date(createdDate.getTime() + 1 * 1000l));

        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }

}
