package com.harshith.E_Cart;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ECartApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		Map<String, Object> dotenvProperties = new HashMap<>();
		dotenv.entries().forEach(entry ->
				dotenvProperties.put(entry.getKey(), entry.getValue())
		);

		SpringApplication application =
				new SpringApplication(ECartApplication.class);
		application.setDefaultProperties(dotenvProperties);
		application.run(args);
	}
}