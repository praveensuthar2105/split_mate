package com.splitmate.backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DotEnvConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Try multiple paths to find .env file
        String[] envPaths = {
            ".env",
            "backend/.env",
            System.getProperty("user.dir") + "/backend/.env",
            System.getProperty("user.dir") + "/.env"
        };
        
        for (String envPath : envPaths) {
            try {
                if (Files.exists(Paths.get(envPath))) {
                    Map<String, Object> properties = new HashMap<>();
                    
                    Files.lines(Paths.get(envPath))
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .forEach(line -> {
                                String[] parts = line.split("=", 2);
                                if (parts.length == 2) {
                                    String key = parts[0].trim();
                                    String value = parts[1].trim();
                                    properties.put(key, value);
                                    System.setProperty(key, value);
                                }
                            });
                    
                    if (!properties.isEmpty()) {
                        environment.getPropertySources()
                            .addFirst(new MapPropertySource("dotenv", properties));
                        System.out.println("[DotEnv] Loaded " + properties.size() + " properties from: " + envPath);
                        return;
                    }
                }
            } catch (IOException e) {
                // Try next path
            }
        }
        System.out.println("[DotEnv] No .env file found in any standard location. Using application.properties defaults.");
    }
}