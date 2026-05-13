package com.example.userservice.service;

import com.example.userservice.Exception.OtpCodeNotFoundException;
import com.example.userservice.model.OtpCode;
import com.example.userservice.model.OtpStatus;
import com.example.userservice.repository.OtpCodeRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;




@Service
public class OtpCodeService {

    private final OtpCodeRepository otpCodeRepository;
    private final BCryptPasswordEncoder otpEncoder;
    private final JavaMailSender mailSender;

    public OtpCodeService(OtpCodeRepository otpCodeRepository,BCryptPasswordEncoder otpEncoder,JavaMailSender mailSender)
    {
        this.otpCodeRepository=otpCodeRepository;
        this.otpEncoder=otpEncoder;
        this.mailSender=mailSender;
    }

    public OtpCode getById(long id) throws OtpCodeNotFoundException
    {
        OtpCode code=otpCodeRepository.findById(id).orElse(null);
        if(code==null){
            throw new OtpCodeNotFoundException("Otp code not found");
        }
        return code;
    }

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public long saveOtp(String email,String otp){
        OtpCode otpCode=new OtpCode();
        otpCode.setEmail(email);
        otpCode.setOtp(otpEncoder.encode(otp));
        otpCode.setCreatedAt(Instant.now());
        otpCode.setStatus(OtpStatus.PENDING);
        otpCodeRepository.save(otpCode);
        return otpCode.getId();
    }


    public long sentOtp(String username,String email)
    {
        String otp=generateOTP();
        
        SimpleMailMessage message=new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your UserName: " +username+"\nYour One Time Password is: "+otp);
        mailSender.send(message);

        return saveOtp(email,otp);
    }


    public OtpStatus verifyOtp(long id, String otp) throws OtpCodeNotFoundException{
        OtpStatus flag = OtpStatus.FAILED;
        OtpCode obj = otpCodeRepository.findById(id).orElse(null);
        Instant futureInstant = obj.getCreatedAt().plus(10, ChronoUnit.MINUTES);
        if (futureInstant.compareTo(Instant.now()) > 0) {
            System.out.println(otp);
            if (otpEncoder.matches(otp,obj.getOtp())) {
                flag = OtpStatus.VERIFIED;
                updateStatus(obj.getId(), OtpStatus.VERIFIED);
            }
        } else {
            System.out.println("2");
            if (otpEncoder.matches(otp,obj.getOtp())) {
                flag = OtpStatus.EXPIRED;
            }
            updateStatus(obj.getId(), OtpStatus.EXPIRED);
        }
        return flag;
    }

    public void updateStatus(Long id, OtpStatus otpStatus) throws OtpCodeNotFoundException{
        OtpCode otpCode=otpCodeRepository.findById(id).orElse(null);
        if(otpCode==null){
            throw new OtpCodeNotFoundException("OTP id not Found");
        }
        otpCode.setStatus(otpStatus);
        otpCodeRepository.save(otpCode);
    }
}
