package com.bms.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmsApplication.class, args);
		System.out.println("----------------------------------------");
		System.out.println("Book Management Server has started...");
		System.out.println("Visit http://localhost:8080/swagger-ui.html for api docs");
		System.out.println("----------------------------------------");
	}

}
