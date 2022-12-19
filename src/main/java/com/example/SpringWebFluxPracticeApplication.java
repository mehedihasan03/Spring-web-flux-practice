package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class SpringWebFluxPracticeApplication {

//	static {
//		BlockHound.install();
//	}

	public static void main(String[] args) {

		System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder()
				.encode("moon"));
		SpringApplication.run(SpringWebFluxPracticeApplication.class, args);
	}

}
