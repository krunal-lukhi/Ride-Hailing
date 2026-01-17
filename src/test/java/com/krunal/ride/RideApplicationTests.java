package com.krunal.ride;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "ride.api-key=test-secret-key")
class RideApplicationTests {

	@Test
	void contextLoads() {
	}

}
