package com.gym.gym_membership_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class 	GymMembershipSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymMembershipSystemApplication.class, args);
	}

}
