# Springboot_find-pharmacy
___
## 개요

외부 API 와 공공 데이터를 활용하는 목적을 지녔습니다.<br>
추천된 길 안내는 카카오 길찾기 및 로드뷰 URL로 제곧 됩니다.
___
## 요구사항 분석

- 약국 찾기 서비스 요구사항<br><br>
  - 약국 현황 데이터를 관리하고 있다고 가정하고, 약국 현황 데이터는 위도 경도의 위치 정보 데이터를 지니고 있습니다.<br><br>
  - 해당 서비스로 주소 정보를 입력하여 요청하면 위치 기준에서 가까운 약국 3곳을 추출 합니다.<br><br>
  - 주소는 도로명 주소 또는 지번을 입력하여 요청 받습니다.<br><br>
  - 주소는 정확한 상세 주소를 제외한 주소 정보를 이용하여 추천 합니다.<br><br>
  - 입력 받은 주소를 위도, 경도로 변환 하여 기존 약국 데이터와 비교 및 가까운 약국을 찾습니다.<br><br>
  - 입력한 주소 정보에서 정해진 반경(10km) 내에 있는 약국만 추천 합니다.<br><br>
___
## 프로젝트 설계

<img src="pharmacy.jpg">

- Spring Data JPA를 이용한 CRUD 메서드 구현
- Spock을 활용한 테스트 코드 작성
- Testcontainers를 활용하여 독립 테스트 환경 구축
- 카카오 주소검색 API 연동하여 주소를 위도, 경도로 변환
- 추천 결과를 카카오 지도 URL로 연동하여 제공
- 공공 데이터를 활용하여 개발
- Handlebars를 이용한 간단한 View 만들기
- 도커를 사용하여 다중 컨테이너 애플리케이션 만들기
- 애플리케이션을 클라우드 서비스에 배포
- Spring retry를 이용한 재처리 구현
- base62를 활용한 shorten url 개발
- redis를 활용하여 성능 최적화하기

___
## 기술 스택
- JDK 17
- Spring Boot 3.3.3
- Spring Data JPA
- Gradle
- Handlebars
- Lombok
- Github
- Docker
- AWS EC2
- Redis
- Maria DB
- Spock
- Testcontainers
___