package com.postify.main.services;

import com.postify.main.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);


    @Async
    public void sendEmail(String to,String subject, String body){
//        logger.info("Starting email sending process to {}...",to);
        log.info("Starting email sending process to {}...",to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("mahadikbhushan768@gmail.com");
        javaMailSender.send(message);
//        logger.info("Email send successfully to user {}...",to);
        log.info("Email send successfully to user {}...",to);
    }

    @Async
//    @Scheduled(fixedRate = 10 * 1000)
//    @Scheduled(cron = "0 52 20 * * ?")
    public void sendPromotionalEmailsToAllUsers(){
        log.info("Starting bulk email sending process...");
        userRepository.findAll().forEach(user -> {
            String emailBody = "Dear "+user.getUsername()+",\n\n" +
                    "Are you ready to take your blogging to next level? Introducing postify, " +
                    "the ultimate platform designed to empower bloggers like you! Whether you're a seasoned writer or just starting out," +
                    " postify has everything you need to create, share," +
                    "and grow your blog effortlessly.\n\n" +
                    "Happy Blogging,\nThe Postify Team ";

            sendEmail(user.getEmail(),"Discover Postify: You Ultimate Blogging Platform",emailBody);
        });
        log.info("Ending bulk email sending process...");
    }
}
