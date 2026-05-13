package com.comissions.korp;

import com.comissions.korp.entity.Distribuidor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KorpApplication {

	public static void main(String[] args)
    {
		SpringApplication.run(KorpApplication.class, args);
	}

}
