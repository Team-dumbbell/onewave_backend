package com.onewave.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "google.ai.api-key=test-key-placeholder",
        "google.ai.model=gemini-2.0-flash"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
