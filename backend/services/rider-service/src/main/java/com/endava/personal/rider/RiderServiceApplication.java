package com.endava.personal.rider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.endava.personal")
public class RiderServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RiderServiceApplication.class, args);
	}
}
