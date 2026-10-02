# spring-mvc-form-binding-13

Spring MVC와 Thymeleaf의 폼 통합 기능을 학습하고, 상품 등록·조회·수정 예제를 통해 폼 데이터가 객체에 바인딩되는 과정을 정리한 저장소입니다.

`th:object`, 선택 변수 표현식, `th:field`를 활용한 입력 필드 처리부터 단일·멀티 체크박스, 라디오 버튼, 셀렉트 박스까지 다양한 폼 요소의 렌더링과 서버 바인딩 방식을 학습했습니다.

## 학습 목적

Spring MVC Controller가 전달한 객체를 Thymeleaf 입력 폼과 연결하고, 사용자가 전송한 값을 다시 자바 객체로 바인딩하는 전체 흐름을 이해하기 위해 정리했습니다.

기본 HTML 폼의 동작과 Thymeleaf가 제공하는 자동 처리 기능을 비교하면서, 체크 상태와 선택값을 등록·상세·수정 화면에 일관되게 반영하는 방법을 익히는 데 중점을 두었습니다.

## 학습 내용

- 상품 목록, 상세, 등록, 수정 기능 구현
- `th:object`를 활용한 폼 객체 지정
- `*{...}` 선택 변수 표현식을 활용한 객체 필드 접근
- `th:field`를 활용한 `id`, `name`, `value` 자동 처리
- `@ModelAttribute`를 활용한 폼 선택 데이터 공통 제공
- 단일 체크박스의 값 전송과 체크 해제 처리
- Thymeleaf가 생성하는 체크박스 hidden marker 동작
- 멀티 체크박스와 `List<String>` 바인딩
- `th:each`와 `#ids.prev()`를 활용한 반복 폼 요소 생성
- `enum`과 라디오 버튼을 활용한 상품 종류 선택
- 객체 목록과 셀렉트 박스를 활용한 배송 방식 선택
- 등록·상세·수정 화면의 체크 및 선택 상태 자동 반영
- `RedirectAttributes`를 활용한 등록 완료 리다이렉트
- 메모리 기반 상품 저장소와 테스트 데이터 구성
- 상품 저장, 전체 조회, 수정 기능 테스트
- [개념 정리 파일 보기](./src/main/docs)

## 디렉터리 구조

```text
form
├── gradle
│   └── wrapper
├── src
│   ├── main
│   │   ├── docs
│   │   │   └── thymeleaf-spring-form.md
│   │   ├── java
│   │   │   └── hello
│   │   │       └── itemservice
│   │   │           ├── domain
│   │   │           │   └── item
│   │   │           │       ├── DeliveryCode.java
│   │   │           │       ├── Item.java
│   │   │           │       ├── ItemRepository.java
│   │   │           │       └── ItemType.java
│   │   │           ├── web
│   │   │           │   └── form
│   │   │           │       └── FormItemController.java
│   │   │           ├── ItemServiceApplication.java
│   │   │           └── TestDataInit.java
│   │   └── resources
│   │       ├── static
│   │       │   ├── css
│   │       │   │   └── bootstrap.min.css
│   │       │   └── index.html
│   │       ├── templates
│   │       │   └── form
│   │       │       ├── addForm.html
│   │       │       ├── editForm.html
│   │       │       ├── item.html
│   │       │       └── items.html
│   │       └── application.properties
│   └── test
│       └── java
│           └── hello
│               └── itemservice
│                   ├── domain
│                   │   └── item
│                   │       └── ItemRepositoryTest.java
│                   └── ItemServiceApplicationTests.java
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## 학습 포인트

- 등록 화면에 빈 `Item` 객체를 전달하고 `th:object`와 `th:field`를 연결하여 입력값이 객체 필드에 바인딩되는 흐름을 확인했습니다.
- `th:field`가 일반 입력 필드의 `id`, `name`, `value`를 자동으로 생성하고, 폼 작성 코드를 단순화하는 방식을 학습했습니다.
- HTML 체크박스는 선택하지 않으면 해당 이름의 값 자체를 전송하지 않으며, Thymeleaf가 `_open`과 같은 hidden marker를 추가해 Spring MVC가 `false`로 바인딩하도록 처리하는 원리를 이해했습니다.
- 멀티 체크박스에서 동일한 `name`과 서로 다른 `id`가 생성되는 과정과 `#ids.prev()`를 사용해 각 `label`을 올바른 입력 요소에 연결하는 방법을 익혔습니다.
- Controller의 `@ModelAttribute` 메서드를 사용해 지역, 상품 종류, 배송 방식 데이터를 등록·상세·수정 요청의 Model에 공통으로 제공했습니다.
- `ItemType.values()`로 열거형 항목을 전달하고, 현재 바인딩된 값과 각 `th:value`를 비교하여 라디오 버튼의 `checked` 상태가 자동으로 결정되는 것을 확인했습니다.
- 셀렉트 박스에서 배송 코드와 화면 표시 이름을 분리하고, 저장된 배송 코드에 맞는 항목이 수정 및 상세 화면에 자동으로 선택되는 흐름을 학습했습니다.
- 상세 화면에서는 `th:object`를 사용하지 않으므로 `${item.open}`, `${item.regions}`, `${item.itemType}`, `${item.deliveryCode}`를 직접 지정하고 `disabled` 속성으로 조회 전용 상태를 구성했습니다.
- 상품 수정 시 기본 필드뿐만 아니라 판매 여부, 등록 지역, 상품 종류, 배송 방식도 저장소에 함께 반영하도록 수정했습니다.

## 실행 환경

- Java 11
- Spring Boot 2.4.4
- Spring MVC
- Thymeleaf
- Gradle
- Lombok
- Slf4j
- JUnit 5
- AssertJ
- HTML
- Bootstrap CSS
- IntelliJ IDEA

## 참고
- 코드 출처 : 스프링 MVC 2편 - 백엔드 웹 개발 활용 기술