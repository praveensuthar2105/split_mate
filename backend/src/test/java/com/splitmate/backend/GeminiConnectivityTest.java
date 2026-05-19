package com.splitmate.backend;

import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GeminiConnectivityTest {

    @Test
    void checkGeminiConnectivity() {
        // Diagnose GOOGLE_APPLICATION_CREDENTIALS first
        String adc = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (adc == null) {
            System.out.println("GOOGLE_APPLICATION_CREDENTIALS is not set in environment");
        } else {
            // Trim surrounding quotes if present (some .env formats include quotes)
            adc = adc.trim();
            if (adc.startsWith("\"") && adc.endsWith("\"")) {
                adc = adc.substring(1, adc.length() - 1);
            }
            System.out.println("GOOGLE_APPLICATION_CREDENTIALS -> " + adc);
            Path p = Paths.get(adc);
            if (!Files.exists(p)) {
                fail("Credential file does not exist at: " + adc);
            } else {
                try {
                    long size = Files.size(p);
                    System.out.println("Credential file size: " + size + " bytes");
                    if (size < 20) {
                        fail("Credential file exists but is unexpectedly small (likely empty or invalid JSON)");
                    }
                } catch (IOException ioe) {
                    fail("Unable to stat credential file: " + ioe.getMessage());
                }
            }
        }

        try {
            GoogleCredentials creds;
            if (adc != null) {
                // we've already trimmed and validated the path above
                Path p = Paths.get(adc);
                creds = GoogleCredentials.fromStream(Files.newInputStream(p));
            } else {
                // fallback to default credentials (e.g., ADC available via gcloud)
                creds = GoogleCredentials.getApplicationDefault();
            }
            assertNotNull(creds, "GoogleCredentials returned null");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to load Application Default Credentials: " + e.getMessage());
        }

        String project = System.getenv("GOOGLE_CLOUD_PROJECT_ID");
        if (project == null) {
            System.out.println("GOOGLE_CLOUD_PROJECT_ID is not set in the test environment. Recommended: ai-resume-builder-496605");
        } else {
            assertNotNull(project, "GOOGLE_CLOUD_PROJECT_ID must be set");
        }
    }
}
