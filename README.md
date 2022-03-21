# coupangEatsA-test-server-haena-rian

Rising Test

## 2022-03-19 진행상황

  - 기획서 작성
  - EC2 인스턴스 구축
  - RDS 데이터베이스 구축
  - localhost:9009 포트 연결 확인
  - ERD 설계 완료
    - https://aquerytool.com/aquerymain/index/?rurl=e4a2c069-adcf-423d-9e44-11c53c677c0b
    - passward : a74vd2

## 2022-03-20 진행상황
      
### API 리스트 관련
- 회원가입 API 구현
- 로그인 API 구현
- 홈 화면 조회 API 구현 (거리 관련 제외)  
<br>
- validation 구현
- 인가 구현
- 

### 기타
- DB 테이블 생성
- ERD 수정
- RESTful API 리스트업
  - https://docs.google.com/spreadsheets/d/1VIkuCFoaXgTUkJoDqHE50YlTO9RZGANg/edit?usp=sharing&ouid=108228008875958634623&rtpof=true&sd=true
- 더미데이터 추가

### TODO
- 회원가입 시, 비밀번호 정규식 심화 구현
- 홈 화면 조회 시 거리 관련 정보 구현

## 2022-03-21 진행상황

### API 리스트 관련

- 홈화면 API 수정 
  - 정렬, 필터 기능 추가
- 가게 상세화면 조회 API 구현
  - storeIdx Params -> String
  - storeIdx 존재 하지 않을 경우 예외처리 추가


### 트러블 슈팅
```Optional int parameter 'storeIdx' is present but cannot be translated into a null value due to being declared as a primitive type.```
- storeIdx를 RequestParam으로 받을 때 int일 경우 null 값으로 처리할 수 없기 때문에 발생한 에러
    - -> String 값으로 받아 null 값을 확인한 후, 아닐 경우 int로 바꾸어 진행할 수 있게 하였다.

### 위클리 스크럼 내용
- 