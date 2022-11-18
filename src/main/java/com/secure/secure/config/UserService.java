package com.secure.secure.config;

import com.secure.secure.entity.User;
import com.secure.secure.repository.UserRepository;
import com.secure.secure.util.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            return new org.springframework.security.core.userdetails.User(user.get().getUsername(),
                    user.get().getPassword(),new ArrayList<>());
        }catch (Exception e){
            throw new RuntimeException("Username is not Valid..");
        }
    }
}
