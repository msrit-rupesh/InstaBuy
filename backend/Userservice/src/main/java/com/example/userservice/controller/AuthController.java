package com.example.userservice.controller;

import com.example.userservice.Exception.*;
import com.example.userservice.dto.*;
import com.example.userservice.model.OtpCode;
import com.example.userservice.model.OtpStatus;
import com.example.userservice.model.User;
import com.example.userservice.model.VendorProfile;
import com.example.userservice.security.JwtUtil;
import com.example.userservice.service.OtpCodeService;
import com.example.userservice.service.RoleService;
import com.example.userservice.service.UserService;
import com.example.userservice.service.VendorProfileService;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/auth")
@Component
public class AuthController {

    private final UserService userService;
    private final OtpCodeService otpCodeService;
    private final JwtUtil jwtUtil;
    private final VendorProfileService vendorProfileService;

    public AuthController(UserService userService,OtpCodeService otpCodeService,JwtUtil jwtUtil,VendorProfileService vendorProfileService){
        this.userService=userService;
        this.otpCodeService=otpCodeService;
        this.jwtUtil=jwtUtil;
        this.vendorProfileService=vendorProfileService;
    }

    @PostMapping("/register/init")
    public ResponseEntity<?> registerInit(@RequestBody @Valid RegisterDTO user) {
        System.out.println("Controller Reached");
        try
        {
            if(!userService.verify(user)){
                throw new UserAlreadyExistException("User with id already exist");
            }
            long id=otpCodeService.sentOtp(user.getUsername(), user.getEmail());
            Map<String,String> res=new HashMap<>();
            res.put("id",String.valueOf(id));
            res.put("message","User Verified and OTP has been sent to your email id");
            return  ResponseEntity.status(HttpStatus.OK).body(res);
        }
        catch (Exception e)
        {
            Map<String,String> res=new HashMap<>();
            res.put("message",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }

    }

    @PostMapping("/register/complete")
    public ResponseEntity<Map<String,String>> register(@Valid @RequestBody RegisterCompleteDTO user) {
        Map<String,String> result=new HashMap<>();
        try
        {
            OtpCode otp=otpCodeService.getById(user.getId());
            if(otp.getStatus()==OtpStatus.VERIFIED && otp.getEmail().compareTo(user.getEmail())==0)
            {
                System.out.println("Hello");
                userService.create(user);
            }
            else{
                throw new OtpCodeNotFoundException("Otp is either invalid nor not verified");
            }
            result.put("message","User Created Successfully");
            return  ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        catch (Exception e)
        {
            result.put("message",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String,String>> otpVerify(@Valid @RequestBody OtpDTO otpObj)
    {
        System.out.println(otpObj.getId());
        System.out.println(otpObj.getOtp());
        System.out.println(otpObj.getEmail());
        Map<String,String> result=new HashMap<>();
        try {
            OtpCode otpCode = otpCodeService.getById(otpObj.getId());
            if (otpCode.getEmail().compareTo(otpObj.getEmail()) != 0) {
                throw new OtpCodeNotFoundException("Email is not mapped with this otp id");
            }
            if (otpCode.getStatus() == OtpStatus.VERIFIED) {
                throw new OtpCodeNotFoundException("Otp Already Verified");
            }
            OtpStatus status = otpCodeService.verifyOtp(otpObj.getId(), otpObj.getOtp());
            if (status == OtpStatus.VERIFIED) {
                result.put("message", "OTP has been verified");
                return ResponseEntity.status(HttpStatus.OK).body(result);
            } else if (status == OtpStatus.EXPIRED) {
                result.put("message", "OTP has been expired");
                return ResponseEntity.status(HttpStatus.GONE).body(result);
            } else {
                result.put("message", "OTP is invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
        }
        catch (Exception e){
            result.put("message",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/forget_username_password")
    public ResponseEntity<Map<String,String>> forgetPassword(@Valid @RequestBody ForgetUsernamePasswordDTO dto){
        System.out.println("Hello World");
        Map<String,String> result=new HashMap<>();
        try {
            User user = userService.getUserByEmail(dto.getEmail());
            if (user == null) {
                throw new UserNotFoundException("Email not Found");
            }
            long id=otpCodeService.sentOtp(user.getUsername(),user.getEmail());
            result.put("id",String.valueOf(id));
            result.put("message","OTP has been sent to your email id");
            return  ResponseEntity.status(HttpStatus.OK).body(result);

        }catch (UserNotFoundException e){
            result.put("message","Invalid User");
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        catch (Exception e){
            result.put("message",e.getMessage());
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String,String>> forgetPassword(@Valid @RequestBody ForgetPasswordDTO userDTO){
        Map<String,String> result=new HashMap<>();
        try {
            User user = userService.getUserByEmail(userDTO.getEmail());
            if (user == null) {
                throw new UserNotFoundException("Username not Found");
            }

            OtpCode otp=otpCodeService.getById(userDTO.getId());
            if(otp.getStatus()==OtpStatus.VERIFIED && otp.getEmail().compareTo(user.getEmail())==0)
            {
                if(userService.checkPassword(user,userDTO.getPassword())){
                    otpCodeService.updateStatus(userDTO.getId(),OtpStatus.PENDING);
                    throw new InvalidPasswordChangeException("The new password matches the previous password");
                }
                userService.changePassword(user.getEmail(),userDTO.getPassword());
            }
            else{
                throw new OtpCodeNotFoundException("Otp is either invalid nor not verified");
            }
            result.put("message","Password changed Successfully");
            return  ResponseEntity.status(HttpStatus.CREATED).body(result);


        }catch (UserNotFoundException e){
            result.put("message","Invalid User");
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        } catch (Exception e){
            result.put("message",e.getMessage());
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

    }
    @PostMapping("/reset-expired-password")
    public ResponseEntity<Map<String,String>> resetExpiredPassword(@Valid @RequestBody ResetPasswordDTO dto){


        Map<String,String> result=new HashMap<>();
        try {
            OtpCode otpCode = otpCodeService.getById(dto.getId());
            User user=userService.getUserByUserName(dto.getUsername());
            if(otpCode.getEmail().compareTo(user.getEmail())!=0){
                throw new OtpCodeNotFoundException("Email is not mapped with this otp id");
            }
            if(otpCode.getStatus()==OtpStatus.VERIFIED){
                throw new OtpCodeNotFoundException("Otp Already Verified");
            }
            OtpStatus status = otpCodeService.verifyOtp(dto.getId(), dto.getOtp());
            if (status == OtpStatus.VERIFIED) {
                if(userService.checkPassword(user,dto.getPassword())){
                    throw new InvalidPasswordChangeException("The new password matches the previous password");
                }
                userService.changePassword(user.getEmail(),dto.getPassword());
                userService.login(new LoginDTO(dto.getUsername(),dto.getPassword()));
                String role=user.getRole().getName();
                String jwtToken = jwtUtil.generateToken(user.getUsername(),role,user.getId());
                result.put("message", "User Login Successful");
                result.put("accessToken", jwtToken);
                result.put("name",(String)result.get("name"));
                return ResponseEntity.status(HttpStatus.OK).body(result);
            } else if (status == OtpStatus.EXPIRED) {
                result.put("message","OTP has been expired");
                return ResponseEntity.status(HttpStatus.GONE).body(result);
            } else {
                result.put("message","OTP is invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
        }
        catch (Exception e){
            result.put("message",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody LoginDTO user){
        Map<String, String> result = new HashMap<>();
        try {
            Map<String, Object> output=userService.login(user);
            String name=userService.getUserByUserName(user.getUsername()).getName();
            if ((Boolean)output.get("isValid")) {
                if((Boolean)output.get("isExpired")){
                    long id=otpCodeService.sentOtp(user.getUsername(),(String)output.get("email"));
                    result.put("id",""+id);
                    throw new PasswordExpiredException("Password Expired. Please change it");
                }
                String role=(String)output.get("role");
                Long userId=(Long)output.get("id");
                String jwtToken = jwtUtil.generateToken(user.getUsername(),role,userId);
                result.put("message", "User Login Successful");
                result.put("name",name);
                result.put("role",role.toUpperCase());
                result.put("accessToken", jwtToken);
                return ResponseEntity.status(HttpStatus.OK).body(result);
            } else {
                int numberOfAttempts=userService.getNumberOfFailedAttempts(user.getUsername());
                int MAX_NUMBER_OF_ATTEMPTS = 5;
                if(numberOfAttempts >= MAX_NUMBER_OF_ATTEMPTS){
                    result.put("message", "User is blocked use forget password to change the password");
                }
                else{
                    result.put("message", "Either Username not Password is invalid");
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
        }
        catch (PasswordExpiredException e){
            result.put("status","PASSWORD_EXPIRED");
            result.put("message",e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        catch (UserNotFoundException e){
            result.put("message", "Either Username not Password is invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        catch (Exception e){
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/email/{id}")
    public ResponseEntity<?> getUserEmail(
            @PathVariable Long id,
            Authentication authentication
    ){
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user.getEmail());
        }catch (Exception e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    @GetMapping("/vendor/{id}")
    public ResponseEntity<?> getVendorProfile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            VendorProfile profile = vendorProfileService.getProfile(id);
            return ResponseEntity.ok(profile);
        }catch (NotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

}