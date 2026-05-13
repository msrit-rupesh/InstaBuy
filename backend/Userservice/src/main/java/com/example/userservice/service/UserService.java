package com.example.userservice.service;

import com.example.userservice.Exception.NotFoundException;
import com.example.userservice.Exception.RoleNotFoundException;
import com.example.userservice.Exception.UserAlreadyExistException;
import com.example.userservice.Exception.UserNotFoundException;
import com.example.userservice.dto.LoginDTO;
import com.example.userservice.dto.RegisterCompleteDTO;
import com.example.userservice.dto.RegisterDTO;
import com.example.userservice.model.Role;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserService(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder,RoleService roleService)
    {
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.roleService=roleService;
    }
    public boolean verify(RegisterDTO userDTO) throws Exception{
        if(userRepository.findByUsername(userDTO.getUsername()).isPresent())
        {
            throw new UserAlreadyExistException("Username already Exists");
        }

        if(userRepository.findByEmail(userDTO.getEmail()).isPresent())
        {
            throw new UserAlreadyExistException("Email already Exists");
        }
        Role roleRef = roleService.getRoleById(userDTO.getRoleId());
        if(roleRef==null){
            throw new RoleNotFoundException("Role not Found");
        }
        return true;
    }
    public void create(RegisterCompleteDTO userDTO) throws Exception{

        User user=new User();
        if(userRepository.findByUsername(userDTO.getUsername()).isPresent())
        {
            throw new UserAlreadyExistException("Username already Exists");
        }
        user.setUsername(userDTO.getUsername());
        if(userRepository.findByEmail(userDTO.getEmail()).isPresent())
        {
            throw new UserAlreadyExistException("Email already Exists");
        }
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setCreatedAt(Instant.now());
        user.setEmailVerified(true);
        user.setPasswordUpdatedAt(Instant.now());
        System.out.println(userDTO.getRoleId());
        Role roleRef = roleService.getRoleById(userDTO.getRoleId());
        if(roleRef==null){
            throw new RoleNotFoundException("Role not Found");
        }
        user.setRole(roleRef);

        userRepository.save(user);
    }

    public int getNumberOfFailedAttempts(String username) throws UserNotFoundException{
        User user=userRepository.findByUsername(username).orElse(null);
        if(user==null){
            throw new UserNotFoundException("Username not Found");
        }
        return user.getFailedAttempts();
    }

    @Transactional
    public Map<String,Object> login(LoginDTO userdata) throws UserNotFoundException {
        Map<String,Object> result=new HashMap<>();
        User user = userRepository.findByUsername(userdata.getUsername()).orElse(null);
        if(user==null){
            throw new UserNotFoundException("Username does not exists");
        }
        if(user.getFailedAttempts()==5){
            result.put("isValid",false);
        }
        if(checkPassword(user,userdata.getPassword())){
            user.setLastLoginIn(Instant.now());
            user.setFailedAttempts(0);
            result.put("isValid",true);
            result.put("name",user.getName());
            result.put("id",user.getId());
            if(Instant.now().isAfter(user.getPasswordUpdatedAt().plus(180, ChronoUnit.DAYS))){
                result.put("isExpired",true);
                result.put("email",user.getEmail());
            }
            else{
                result.put("isExpired",false);
                result.put("role",user.getRole().getName());
            }
        }
        else{
            user.setFailedAttempts(user.getFailedAttempts()+1);
            result.put("isValid",false);
        }
        return result;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void changePassword(String email,String password) throws UserNotFoundException{

        User user=userRepository.findByEmail(email).orElse(null);
        if(user==null){
            throw new UserNotFoundException("Email Id not Found");
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordUpdatedAt(Instant.now());
        user.setFailedAttempts(0);
        userRepository.save(user);

    }
    public boolean checkPassword(User user,String password){
        if(passwordEncoder.matches(password,user.getPassword())){
            user.setFailedAttempts(0);
            return true;
        }
        return false;
    }

    public User getUserByUserName( String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User getUserById(Long id) throws NotFoundException {
        User user=userRepository.findById(id).orElse(null);
        if(user==null){
            throw new NotFoundException("User with Id not found");
        }
        return user;
    }
}
