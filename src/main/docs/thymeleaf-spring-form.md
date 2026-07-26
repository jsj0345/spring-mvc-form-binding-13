# Thymeleaf 폼 바인딩 복습

> 상품 등록·조회·수정 화면을 만들면서 `th:object`, `th:field`, 체크박스, 라디오 버튼, 셀렉트 박스가 서버 객체와 연결되는 방식을 정리했다.

## 1. 폼과 객체를 연결하는 기본 구조

등록 화면에서 입력값을 받을 객체를 모델에 먼저 넣는다.

```java
@GetMapping("/add")
public String addForm(Model model) {
    model.addAttribute("item", new Item());
    return "form/addForm";
}
```

뷰에서는 폼이 사용할 대상을 `th:object`로 지정한다.

```html
<form
    th:action
    th:object="${item}"
    method="post">

    <input
        type="text"
        th:field="*{itemName}">
</form>
```

내가 이해한 연결 관계는 다음과 같다.

```text
th:object="${item}"
→ 폼 전체가 item 객체를 기준으로 동작

*{itemName}
→ item.itemName을 가리킴

th:field
→ id, name, value를 바인딩 규칙에 맞게 생성
```

예를 들어 다음 코드는:

```html
<input type="text" th:field="*{itemName}">
```

렌더링 후 대략 다음과 같은 속성을 갖는다.

```html
<input
    type="text"
    id="itemName"
    name="itemName"
    value="">
```

입력 요소마다 `id`, `name`, `value`를 따로 맞추지 않아도 폼 객체의 프로퍼티 이름을 기준으로 연결된다.

---

## 2. 상품 폼에 추가된 값

기본 상품 정보 외에 선택형 입력을 추가했다.

```java
public class Item {

    private Long id;
    private String itemName;
    private Integer price;
    private Integer quantity;

    private Boolean open;
    private List<String> regions;
    private ItemType itemType;
    private String deliveryCode;
}
```

각 필드는 화면 요소와 다음처럼 연결된다.

| 필드 | 화면 요소 |
|---|---|
| `open` | 단일 체크박스 |
| `regions` | 다중 체크박스 |
| `itemType` | 라디오 버튼 |
| `deliveryCode` | 셀렉트 박스 |

상품 종류는 정해진 값만 사용하므로 enum으로 표현한다.

```java
public enum ItemType {
    BOOK("도서"),
    FOOD("식품"),
    ETC("기타");

    private final String description;

    ItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

배송 방식은 서버에 전달할 코드와 화면에 보여 줄 이름을 분리한다.

```java
public class DeliveryCode {

    private String code;
    private String displayName;

    public DeliveryCode(
            String code,
            String displayName
    ) {
        this.code = code;
        this.displayName = displayName;
    }
}
```

```text
FAST
→ 서버에서 사용하는 값

빠른 배송
→ 사용자 화면에 표시할 문구
```

---

## 3. 단일 체크박스의 전송 방식

기본 HTML 체크박스는 선택했을 때만 값을 보낸다.

```html
<input
    type="checkbox"
    name="open">
```

선택했을 때:

```text
open=on
```

선택하지 않았을 때:

```text
open 자체가 요청에 포함되지 않음
```

Spring은 `on` 값을 `Boolean true`로 바꿀 수 있다. 문제는 미선택 상태다.

```text
값이 오지 않음
→ 체크를 해제한 것인지
→ 입력 요소 자체가 없었던 것인지
→ 서버가 바로 구분하기 어려움
```

수정 화면에서 기존 `true` 값을 `false`로 바꾸려 할 때 특히 문제가 될 수 있다.

---

## 4. 체크 해제를 전달하는 히든 마커

Spring MVC는 체크박스 이름 앞에 `_`를 붙인 히든 값을 이용해 미선택 상태를 구분한다.

```html
<input
    type="checkbox"
    name="open">

<input
    type="hidden"
    name="_open"
    value="on">
```

체크했을 때:

```text
open=on
_open=on
→ open=true
```

체크하지 않았을 때:

```text
_open=on
→ open=false
```

히든 값은 체크박스가 화면에 존재했다는 사실을 알려 주고, 실제 체크 값이 없으면 해제 상태로 판단하게 한다.

이 코드를 폼마다 직접 작성하는 것은 번거롭다.

---

## 5. `th:field`가 단일 체크박스를 처리하는 방식

Thymeleaf 폼 기능을 사용하면 히든 마커를 직접 추가하지 않아도 된다.

```html
<input
    type="checkbox"
    th:field="*{open}"
    class="form-check-input">
```

이 한 줄에서 처리되는 내용은 다음과 같다.

```text
id 생성
name 생성
체크됐을 때 보낼 value 생성
현재 item.open에 따른 checked 결정
미선택 처리를 위한 _open 히든 마커 생성
```

여기서 `value="true"`는 현재 값이 무조건 참이라는 의미가 아니다.

```text
체크된 경우
→ open=true 전송

체크되지 않은 경우
→ open은 전송되지 않음
→ _open을 보고 false로 바인딩
```

등록 화면과 수정 화면에서는 `th:object="${item}"`가 있으므로 다음처럼 선택 변수 표현식을 사용한다.

```html
th:field="*{open}"
```

---

## 6. 조회 화면에서 체크박스 표시하기

상품 상세 화면은 값을 입력하는 화면이 아니라 저장된 상태를 보여 주는 화면이다.

```html
<input
    type="checkbox"
    th:field="${item.open}"
    disabled>
```

상세 화면에 `th:object`가 없다면 `*{open}`이 아니라 `${item.open}`처럼 객체를 직접 지정한다.

```text
item.open == true
→ checked 속성이 붙은 상태로 렌더링

item.open == false
→ checked 속성 없이 렌더링
```

`disabled`를 사용해 사용자가 조회 화면에서 값을 변경하지 못하도록 한다.

수정 화면에서는 다시 입력 가능한 형태로 사용한다.

```html
<form th:object="${item}">
    <input
        type="checkbox"
        th:field="*{open}">
</form>
```

---

## 7. 새 필드를 추가했는데 수정 결과가 반영되지 않는 이유

화면에서 `open`, `regions`, `itemType`, `deliveryCode`를 입력받더라도 저장소의 수정 코드가 기존 세 필드만 복사하면 새 값은 저장되지 않는다.

```java
public void update(
        Long itemId,
        Item source
) {
    Item target = findById(itemId);

    target.setItemName(source.getItemName());
    target.setPrice(source.getPrice());
    target.setQuantity(source.getQuantity());

    target.setOpen(source.getOpen());
    target.setRegions(source.getRegions());
    target.setItemType(source.getItemType());
    target.setDeliveryCode(source.getDeliveryCode());
}
```

화면 바인딩이 정상이어도 저장 로직이 새 필드를 반영하지 않으면 수정 결과는 유지되지 않는다.

```text
폼 입력 성공
≠ 저장 성공

바인딩
→ 요청값을 객체에 넣는 단계

저장소 update
→ 객체의 값을 실제 저장 대상에 옮기는 단계
```

---

## 8. 여러 화면에서 공통 선택지를 제공하기

등록·상세·수정 화면에서 같은 지역 목록을 사용한다고 가정한다.

각 컨트롤러 메서드마다 `model.addAttribute()`를 반복할 수 있지만, 별도의 `@ModelAttribute` 메서드를 두면 해당 컨트롤러 요청에 공통으로 모델 데이터를 추가할 수 있다.

```java
@ModelAttribute("regions")
public Map<String, String> regions() {
    Map<String, String> values =
            new LinkedHashMap<>();

    values.put("SEOUL", "서울");
    values.put("BUSAN", "부산");
    values.put("JEJU", "제주");

    return values;
}
```

```text
Map key
→ 서버에 전송할 값

Map value
→ 화면에 표시할 이름
```

등록, 조회, 수정 화면에서 모두 `${regions}`를 사용할 수 있다.

---

## 9. 다중 체크박스

상품이 여러 지역을 선택할 수 있으므로 `Item.regions`는 문자열 목록을 사용한다.

```html
<div
    th:each="region : ${regions}"
    class="form-check form-check-inline">

    <input
        type="checkbox"
        th:field="*{regions}"
        th:value="${region.key}"
        class="form-check-input">

    <label
        th:for="${#ids.prev('regions')}"
        th:text="${region.value}">
    </label>
</div>
```

각 요소의 역할:

```text
th:each
→ 지역 목록 반복

th:field="*{regions}"
→ item.regions와 연결

th:value="${region.key}"
→ 선택 시 서버에 보낼 값

th:text="${region.value}"
→ 사용자에게 보여 줄 이름
```

여러 체크박스는 모두 같은 `name="regions"`를 사용하지만 `id`는 서로 달라야 한다.

렌더링 결과는 다음처럼 번호가 붙을 수 있다.

```text
regions1
regions2
regions3
```

라벨은 `#ids.prev('regions')`를 이용해 바로 앞에 생성된 입력 요소의 ID와 연결한다.

---

## 10. 다중 체크박스의 선택 상태 복원

상품이 다음 값을 저장하고 있다고 가정한다.

```text
item.regions = [SEOUL, BUSAN]
```

Thymeleaf는 각 체크박스의 `value`와 현재 목록 값을 비교한다.

```text
value=SEOUL
→ 목록에 있음
→ checked

value=BUSAN
→ 목록에 있음
→ checked

value=JEJU
→ 목록에 없음
→ checked 없음
```

사용자가 선택한 값은 요청에서 같은 이름으로 여러 번 전달될 수 있다.

```text
regions=SEOUL
regions=BUSAN
```

Spring은 이를 `Item.regions` 목록에 바인딩한다.

상세 화면에서는 폼 객체 선택이 없으므로 다음처럼 직접 객체를 지정한다.

```html
<input
    type="checkbox"
    th:field="${item.regions}"
    th:value="${region.key}"
    disabled>
```

---

## 11. 라디오 버튼

라디오 버튼은 여러 값 가운데 하나를 고르는 입력이다.

선택지는 enum 전체 값으로 준비한다.

```java
@ModelAttribute("itemTypes")
public ItemType[] itemTypes() {
    return ItemType.values();
}
```

뷰에서는 enum을 반복한다.

```html
<div
    th:each="type : ${itemTypes}"
    class="form-check form-check-inline">

    <input
        type="radio"
        th:field="*{itemType}"
        th:value="${type.name()}"
        class="form-check-input">

    <label
        th:for="${#ids.prev('itemType')}"
        th:text="${type.description}">
    </label>
</div>
```

예를 들어 서버에 전달할 값과 표시 문구는 다음처럼 나뉜다.

```text
BOOK → 도서
FOOD → 식품
ETC  → 기타
```

`th:field`는 현재 `item.itemType`과 각 버튼의 `value`를 비교해 같은 항목에 `checked`를 붙인다.

```text
item.itemType == ETC
→ value="ETC"인 라디오 버튼 선택
```

---

## 12. 라디오 버튼에 히든 마커가 없는 이유

체크박스는 선택하지 않았다는 상태 자체를 `false`로 전달해야 할 수 있다. 그래서 미선택을 구분하는 히든 마커가 필요하다.

라디오는 같은 이름을 가진 여러 값 중 하나를 선택하는 구조다.

```text
체크박스
→ 선택 여부 자체가 값
→ 해제 상태를 구분해야 함

라디오 버튼
→ 여러 값 가운데 하나를 전달
→ 체크박스식 false 마커가 필요하지 않음
```

처음부터 아무 항목도 선택하지 않으면 `itemType` 값은 전달되지 않아 `null`로 바인딩될 수 있다.

상세 화면에서는 저장된 enum 값에 해당하는 버튼만 선택 상태로 표시하고 입력을 막는다.

```html
<input
    type="radio"
    th:field="${item.itemType}"
    th:value="${type.name()}"
    disabled>
```

---

## 13. 셀렉트 박스

배송 방식은 여러 선택지 중 하나를 고르므로 셀렉트 박스로 표현한다.

컨트롤러에서 선택 목록을 모델에 넣는다.

```java
@ModelAttribute("deliveryCodes")
public List<DeliveryCode> deliveryCodes() {
    return List.of(
        new DeliveryCode("FAST", "빠른 배송"),
        new DeliveryCode("NORMAL", "일반 배송"),
        new DeliveryCode("SLOW", "느린 배송")
    );
}
```

뷰:

```html
<select
    th:field="*{deliveryCode}"
    class="form-select">

    <option value="">
        배송 방식을 선택하세요
    </option>

    <option
        th:each="delivery : ${deliveryCodes}"
        th:value="${delivery.code}"
        th:text="${delivery.displayName}">
    </option>
</select>
```

렌더링된 선택지는 다음 구조가 된다.

```html
<option value="FAST">빠른 배송</option>
<option value="NORMAL">일반 배송</option>
<option value="SLOW">느린 배송</option>
```

`th:field`는 현재 `item.deliveryCode`와 각 옵션 값을 비교해 저장된 항목을 선택 상태로 만든다.

---

## 14. 셀렉트 박스에 히든 필드가 필요 없는 이유

선택하지 않은 상태를 표현하는 빈 옵션도 `name`과 함께 전송될 수 있다.

```html
<option value="">
    배송 방식을 선택하세요
</option>
```

```text
deliveryCode=""
```

체크박스처럼 입력 이름 자체가 사라지는 방식이 아니므로 체크 해제 확인용 히든 마커가 필요하지 않다.

---

## 15. 입력 요소별 동작 비교

| 입력 요소 | 서버에 전달되는 값 | 미선택 상태 | 히든 마커 |
|---|---|---|---|
| 단일 체크박스 | `open=true` | 값 자체가 빠질 수 있음 | 사용 |
| 다중 체크박스 | `regions=SEOUL` 등 | 선택값이 빠질 수 있음 | 사용 |
| 라디오 버튼 | 선택한 한 값 | 아무것도 고르지 않으면 값 없음 | 사용하지 않음 |
| 셀렉트 박스 | 선택한 옵션 값 | 빈 옵션의 `value=""` 전달 가능 | 사용하지 않음 |

---

## 16. 전체 처리 흐름

```text
1. Controller가 빈 Item과 선택지 데이터를 Model에 추가
2. th:object가 폼과 Item을 연결
3. th:field가 입력 요소의 id, name, value 등을 생성
4. 사용자가 체크박스·라디오·셀렉트 값을 선택
5. Form 요청을 Spring MVC가 Item 객체에 바인딩
6. Repository가 상품을 저장하거나 기존 상품을 수정
7. 상세 또는 수정 화면에서 저장된 값을 다시 렌더링
8. th:field가 현재 값에 맞춰 checked 또는 selected 상태 결정
```

## 핵심 정리

- `th:object`는 폼이 사용할 객체를 지정한다.
- `*{...}`는 선택된 폼 객체를 기준으로 프로퍼티에 접근한다.
- `th:field`는 입력 요소와 객체 필드를 연결하고 관련 HTML 속성을 자동 생성한다.
- 체크박스는 미선택 시 값이 전송되지 않아 히든 마커가 필요하다.
- 다중 체크박스는 같은 이름으로 여러 값을 보내며 현재 목록을 기준으로 체크 상태가 복원된다.
- 라디오 버튼은 여러 값 가운데 하나를 선택하므로 체크박스용 히든 마커를 사용하지 않는다.
- 셀렉트 박스는 코드와 화면 문구를 나누어 구성할 수 있다.
- 화면에 필드를 추가한 뒤 저장소의 수정 로직에도 해당 필드를 반영해야 실제 값이 유지된다.
