package com.gab.notifoad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.gab.notifoad")
public class NotifoadApplication {
	
	public static void main(String[] args)  {
		
		SpringApplication.run(NotifoadApplication.class);
	
	}

}
