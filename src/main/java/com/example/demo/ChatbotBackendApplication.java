package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatbotBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotBackendApplication.class, args);

		System.setProperty("javax.net.debug", "ssl,handshake");

System.out.println(System.getProperty("java.home"));


	}

}
