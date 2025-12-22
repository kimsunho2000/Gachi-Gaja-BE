# ✈️ 같이가자 (Gachi-Gaja) - Backend

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/SpringBoot-3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Google%20Gemini-AI-4285F4?style=for-the-badge&logo=googlegemini&logoColor=white">
</p>

> **"모두가 만족하는 여행의 시작"**
> **같이가자**는 단체 여행 계획 시 발생하는 의견 충돌과 번거로운 조율 과정을 AI(Gemini)가 대신 해결해 주는 서비스입니다. 그룹원들의 다양한 취향을 수집하고 분석하여 최적의 여행 일정을 제안합니다.

<br>

## 🛠 Tech Stack

### Framework & Language
- **Language:** Java 17
- **Framework:** Spring Boot 3.4.0
- **Build Tool:** Gradle 8.11.1

### Persistence & Database
- **ORM:** Spring Data JPA
- **Database:** MySQL 8.0

### Security & AI
- **Security:** Spring Security & JWT (Json Web Token)
- **AI Engine:** Google Gemini AI (`google-genai`)

### API Documentation
- **Swagger:** SpringDoc OpenAPI 2.2.0

<br>

## ✨ Key Features

### 1. 회원 및 인증 관리 (User & Auth)
- **보안 통신:** Spring Security와 JWT를 연동한 Stateless 인증 체계 구축.
- **안전한 회원 관리:** BCrypt 암호화 알고리즘을 통한 비밀번호 보호 및 이메일 중복 검증.
- **마이페이지:** 사용자 프로필 조회, 정보 수정 및 회원 탈퇴 프로세스 제공.

### 2. 스마트 그룹 관리 (Group)
- **유연한 그룹 생성:** 여행지, 일정, 예산 등 기본 설정을 바탕으로 여행 그룹 생성.
- **초대 시스템:** 유니크한 초대 링크를 통한 멤버 모집 및 정원 제한 기능.
- **권한 관리:** 리더(생성자) 중심의 그룹 정보 수정 및 삭제 권한 부여.

### 3. 데이터 기반 요구사항 수집 (Requirement)
- **상세 취향 분석:** 여행 스타일, 숙소 기준, 식사 예산, 이동 거리 등 개인별 요구사항 입력.
- **데이터 Aggregation:** 개별 요구사항을 통합하여 AI 분석이 가능한 정형 데이터로 가공.

### 4. AI 기반 여행 플래닝 (AI Planning)
- **후보 일정 생성:** Gemini AI가 그룹의 특성을 분석하여 복수의 여행 시나리오 제안.
- **민주적 투표 시스템:** AI가 제안한 후보 중 그룹원들의 실시간 투표를 통해 의견 수렴.
- **최종 확정 알고리즘:** 투표 결과와 리더의 선택을 반영하여 시간 단위의 상세 일정표 자동 생성.

<br>

## 📂 Project Structure

```plaintext
src/main/java/com/Gachi_Gaja/server
├── configure       # Security, Swagger, RestTemplate 등 프레임워크 설정
├── controller      # API 엔드포인트 정의 (Auth, Group, Plan 등)
├── domain          # JPA Entity 및 도메인 모델 (User, Group, Plan 등)
├── dto             # 계층 간 데이터 전송을 위한 DTO (Request/Response)
├── exception       # 전역 예외 처리(GlobalExceptionHandler) 및 커스텀 예외
├── jwt             # JWT 발급, 검증 및 필터 로직
├── repository      # 데이터베이스 액세스를 위한 Spring Data JPA 인터페이스
└── service         # AI 연동, 여행 로직, 투표 관리 등 핵심 비즈니스 로직
```
## 🚀 Getting Started
### Prerequisites
JDK 17 이상

MySQL 8.0 이상

Google Gemini API Key (Google AI Studio에서 발급 가능)
### Configuration
src/main/resources/application.properties 파일을 생성하고 아래 내용을 환경에 맞게 수정하세요.
```
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/gachi_gaja
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update

# Gemini API Configuration
gemini.model.name=gemini-1.5-flash
gemini.api.key=YOUR_GEMINI_API_KEY

# JWT Secret Key
jwt.secret=YOUR_JWT_SECRET_KEY_AT_LEAST_32_CHARACTERS
```
### Run
터미널에서 아래 명령어를 실행하여 서버를 구동합니다.
```
./gradlew bootRun
```
📖 API Documentation
서버 구동 후, Swagger UI를 통해 모든 API 명세를 확인하고 직접 테스트해 볼 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```
👥 Contributors

김진영(PM,star3960) - DB 설계, AI 연동 및 프롬프트 작성, 여행 계획 기능 구현(CRUD), AI 응답 속도 개선

김나연(skdus2)- 시나리오 설계, 로그인 및 회원 관리, 여행 모임 기능 구현(CRUD), 여행 계획 후보 투표 기능 구현, JWT 적용

김선호(kimsunho2000) - API 설계, 전체 환경 설정 및 domain 구성, 사용자 요구 사항 기능 구현(CRUD), 참가 요청 기능 구현, GCP 배포 및 프론트 연동

