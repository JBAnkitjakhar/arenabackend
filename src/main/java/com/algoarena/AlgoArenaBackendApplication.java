// algoarena-backend/src/main/java/com/algoarena/AlgoArenaBackendApplication.java

package com.algoarena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Add this annotation to enable the keep-alive scheduler
public class AlgoArenaBackendApplication {

	public static void main(String[] args) {
		// Debug: Print environment variables BEFORE starting Spring Boot
        // System.out.println("=== DEBUG: Environment Variables ===");
        // System.out.println("MONGODB_URI: " + System.getenv("MONGODB_URI"));
        // System.out.println("MONGODB_URI (property): " + System.getProperty("MONGODB_URI"));
        // System.out.println("=====================================");
		SpringApplication.run(AlgoArenaBackendApplication.class, args);
	}

}