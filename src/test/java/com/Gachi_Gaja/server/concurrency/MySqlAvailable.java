package com.Gachi_Gaja.server.concurrency;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 동시성 재현 테스트는 MySQL(InnoDB)이 있어야만 의미가 있다.
 * MySQL이 없거나 접속 정보가 맞지 않는 환경(CI, 다른 개발 PC)에서는
 * 컨텍스트 로딩 실패로 요란하게 깨지는 대신 조용히 skip 되게 한다.
 *
 * 접속 정보는 환경변수로 준다:
 *   TEST_DB_URL (기본 jdbc:mysql://localhost:3306/gachi_gaja_test?createDatabaseIfNotExist=true)
 *   TEST_DB_USER (기본 root)
 *   TEST_DB_PASSWORD (기본 빈 값)
 */
final class MySqlAvailable {

    private MySqlAvailable() {}

    static final String URL = envOr("TEST_DB_URL",
            "jdbc:mysql://localhost:3306/gachi_gaja_test?createDatabaseIfNotExist=true");
    static final String USER = envOr("TEST_DB_USER", "root");
    static final String PASSWORD = envOr("TEST_DB_PASSWORD", "");

    /** JUnit @EnabledIf 가 호출한다. 스프링 컨텍스트가 뜨기 전에 평가된다. */
    static boolean isReachable() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return conn.isValid(3);
        } catch (Exception e) {
            System.out.println("[skip] MySQL 접속 불가 → 동시성 테스트를 건너뛴다: " + e.getMessage());
            System.out.println("       TEST_DB_USER / TEST_DB_PASSWORD 환경변수를 설정한 뒤 다시 실행하라.");
            return false;
        }
    }

    private static String envOr(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
