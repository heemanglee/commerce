# 카테고리

## 카테고리 구조
- id (UUID)
  - 고유 식별자
- name
  - 카테고리명
- parent_id
  - 상위 카테고리 ID 
- position
  - 동일한 카테고리 내에서 상품 정렬 순서
- status
  - ACTIVE/INACTIVE
  - 부모 카테고리가 INACTIVE가 될 경우, 자식 카테고리도 INACTIVE가 되어야 함.
- create_at
- updated_at

