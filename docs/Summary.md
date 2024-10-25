# FIND PHARMACY
___

## 1. 도커 싱글 컨테이너 실습
**도커 이미지 생성**
```java
docker build -t dcplife/application-project-test .
```

**도커 포트포워딩**
```java
docker run dcplife/application-project-test  -p 8080:8080
```
**도커 컨테이너 내부 진입**
```java
docker exec -it {docker id}
```
___

## 2. Profile 분리하기
> 어플리케이션 설정을 특정 환경에서만 적용하거나 환경별로 다르게 적용할때
> 사용된다.

#### 아래와 같은 네이밍 컨벤션이 존재한다.
- `application-{profile}.yml`

**Yaml에서 프로파일 분리 예시**
```yaml
spring:
  profiles:
    active: local # 기본적으로 활성화 할 프로파일
    group:
      local:
        - common
      prod:
        - common
        
--- # 구분선

spring:
  config:
    activate:
      on-profile: local

---

spring:
  config:
    activate:
      on-profile: prod
```
> **어플리케이션 실행**을 통해 `프로파일`이 잘 실행되는지 확인할 수 있다.
<img src="images/profile1.png">

___

## 3. 다중 컨테이너
> `Dockerfile`과 `docker compose`를 통하여 다중 컨테이너를 구현할 계획이다.

#### 루트 Dockerfile
```dockerfile
FROM openjdk:17
ARG JAR_FILE=build/libs/app.jar
COPY ${JAR_FILE} ./app.jar
ENV TZ=Asia/Seoul
ENTRYPOINT ["java","-jar","./app.jar"]
```
- 설정 내용
  - `java 17` 버전 사용
  - `JAR_FILE` 이라는 인자 생성
  - 컨테이너 내에 `JAR_FILE`을 `./app.jar`에 카피
  - 한국 `시간 설정`
  - `java -jar ./app.jar` 실행

- #### db Dockerfile
```dockerfile
FROM mariadb:10

ENV TZ=Asis/Seoul
```

- #### redis Dockerfile
```dockerfile
FROM redis:6

ENV TZ=Asis/Seoul
```
- #### 추가적으로 한글이 깨지는 경우가 있어 다음과 같은 cnf 설정
```lombok.config
[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4

[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
skip-character-set-client-handshake

[mysqldump]
default-character-set=utf8mb4

```


외부에 공개되면 안 되는 **환경변수**를 사용해야 하는 경우가 많은데
이번 실습은 `.env` 파일에 관리할 예정.<br/>
보통은 `Vault`에 하는 경우가 많음.

- #### `.env` 파일을 생성하여 다음과 같이 설정하였다.
```lombok.config
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=1234
```

#### docker-compose 실행
```lombok.config
docker-compose -f docker-compose-local.yml up
```
 - 이와 같이 실행하면 도커 컴포즈가 실행된다.
 - `docker ps` 를 입력하여 제대로 작동되고 있는지 확인한다.

### 로컬 DB 설정
> .env 파일은 docker-compose 가 실행될 때 적용된다.

- 즉, local 로 개발하는 경우 .env 파일이 적용되지 않는 것이다.
```lombok.config
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/pharmacy-recommendation
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```
- 위는 `application.yml` 의 파일 중 일부이다.
- `Edit Configurations` 에서 **환경변수** 설정을 해주자.

___

## 4. 추천 서비스 구현

#### Kakao 와 연동

```yaml
kakao:
  rest:
    api:
      key: ${KAKAO_REST_API_KEY}
```
> 이번 프로젝트는 카카오를 사용할 것이다. 위와 같이 작성하여 주고
> **환경변수**를 주입해준다.


#### @JsonProperty
**카카오 API 문서**를 보면 **JSON**을 통해 `total_count`을 가져와줘야 합니다.
그러나 JAVA에서는 `total_count`로 변수를 작성하지 않습니다.<br/>
따라서 `totalCount`와 `total_count`를 매핑해주어야 하는데요. 아래와 같이 코드를
작성해주면 됩니다.

```java
@JsonProperty("total_count")
private Integer totalCount;
```

#### Testing

> 이번 프로젝트에서는 `Spock`, `Testcontainers` 를 사용할 예정입니다.

1. **테스트의 중요성**
- **테스트 코드**는 기능에 대한 불확실성을 감소시키며, 개발자가 만든 기능을 **안전하게 보호**합니다.
  - **ex) A라는 기능을 추가로 개발하였더니 기존에 잘 사용하던 B에 기능 문제 발생**
- **개발 단계 초기**에 문제 발견에 도움을 줍니다.
  - **ex) 배포 후 문제 발생하면 원인 파악을 하여야 하고 다시 테스트하고 코드를 수정해야 합니다.**

> `테스트코드` 작성에 시간이 아깝다 생각하지 말고, 적절한 `테스트코드` 작성하는데에 집중해야 합니다.

2. **Spock**
- Spock 이란 Groovy 언어를 이용하여 테스트 코드를 작성할 수 있는 프레임워크입니다.
- JUnit과 비교하여 코드를 더 간결하게 작성 가능합니다.

3. **Spock 세팅**
- build.gradle
```yaml
//	Spock Setting
	testImplementation 'org.spockframework:spock-core:2.4-M4-groovy-4.0'
	testImplementation 'org.spockframework:spock-spring:2.4-M4-groovy-4.0'

//	런타임 클래스 기반 Spock Mock 만들기 위해
	implementation 'net.bytebuddy:byte-buddy:1.15.5'
```
```yaml
plugins {
  id 'java'
  id 'org.springframework.boot' version '3.3.4'
  id 'io.spring.dependency-management' version '1.1.6'
  id 'groovy'	// groovy 추가
}
```

- **url을 제대로 인코딩하는지 확인해보겠습니다**
```groovy
class KakaoUriBuilderServiceTest extends Specification {

    KakaoUriBuilderService kakaoUriBuilderService;

    def setup() {
        kakaoUriBuilderService = new KakaoUriBuilderService();
    }

    def "한글 파라미터의 경우 정상적으로 인코딩" () {
        given:
        String address = "서울 성북구"
        def charset = StandardCharsets.UTF_8

        when:
        def uri = kakaoUriBuilderService.buildUriByAddressSearch(address)
        def decodeResult = URLDecoder.decode(uri.toString(), charset)

        then:
        decodeResult == "https://dapi.kakao.com/v2/local/search/address.json?query=서울 성북구"

    }

}
```

4. **통합 테스팅**

_위의 Spock 과 같은 경우는 따로 Spring Container 띄어놓지 않고 단위 테스팅을
진행을 했습니다._

> **통합테스트**는 `스프링컨테이너`를 띄우고 **의존성 주입**을 통한 여러 모듈 간
> **연동까지 검증하는 테스트**입니다.

- `@SpringBootTest` 어노테이션을 붙이면 **통합테스트**가 가능합니다.


























