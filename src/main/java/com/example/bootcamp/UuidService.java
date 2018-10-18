package com.example.bootcamp;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@Component("uuid")
class UuidService {

	public String buildUuid() {
		return UUID.randomUUID().toString();
	}

}
