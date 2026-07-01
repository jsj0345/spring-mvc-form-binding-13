package hello.itemservice.domain.item;

public enum ItemType {

  BOOK("도서"), FOOD("음식"), ETC("기타");

  /*
  [추가]
  static final ItemType BOOK = new ItemType("도서");
  static final ItemType FOOD = new ItemType("음식");
  static final ItemType ETC = new ItemType("기타");

  이런식으로 만들어진다.
   */

  private final String description;

  ItemType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

}
