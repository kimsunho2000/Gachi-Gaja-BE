package com.Gachi_Gaja.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 컨텍스트 로딩만 확인하는 스모크 테스트.
 *
 * test 프로파일을 쓰지 않으면 개발 DB(gachi_gaja)에 그대로 붙어,
 * 그 스키마가 없는 PC에서는 "Unknown database" 로 깨진다.
 * 테스트는 개발 DB에 의존하면 안 되므로 application-test.properties 를 사용한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
