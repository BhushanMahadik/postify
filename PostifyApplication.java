package com.postify.main;

import com.postify.main.services.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PostifyApplication {

	@Autowired
	UserService userService;

	public static void main(String[] args) {
		SpringApplication.run(PostifyApplication.class, args);
	}

	@PostConstruct
	public void init(){
		try{
			System.out.println("Creating super user....");
			userService.createSuperUser("SuperUsername","YourPassword@1234","Youremail@gmail.com");
			System.out.println("Super user created.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error creating super user: "+e.getMessage());
		}
	}



}
