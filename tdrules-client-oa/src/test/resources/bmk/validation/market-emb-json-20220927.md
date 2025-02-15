---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  CartDTO_cartItems_xa *--"1" CartDTO_cartItems_xa__links_xt
  CartDTO *--"1" CartDTO__links_xt
  CartDTO *--"*" CartDTO_cartItems_xa
  CartItemDTOReq__links_xa *--"1" CartItemDTOReq__links_xa_rel_xt
  CartItemDTOReq *--"*" CartItemDTOReq__links_xa
  CartItemDTORes *--"1" CartItemDTORes__links_xt
  ContactsDTOReq__links_xa *--"1" ContactsDTOReq__links_xa_rel_xt
  ContactsDTOReq *--"*" ContactsDTOReq__links_xa
  ContactsDTORes *--"1" ContactsDTORes__links_xt
  Link *--"1" Link_rel_xt
  OrderDTO *--"1" OrderDTO__links_xt
  ProductDTO *--"1" ProductDTO__links_xt
  UserDTOReq__links_xa *--"1" UserDTOReq__links_xa_rel_xt
  UserDTOReq *--"*" UserDTOReq__links_xa
  UserDTORes *--"1" UserDTORes__links_xt
  CartDTO__links_xt ..|> Links
  CartItemDTORes__links_xt ..|> Links
  CartDTO_cartItems_xa__links_xt ..|> Links
  CartDTO_cartItems_xa ..|> CartItemDTORes
  Link_rel_xt ..|> LinkRelation
  CartItemDTOReq__links_xa_rel_xt ..|> LinkRelation
  CartItemDTOReq__links_xa ..|> Link
  ContactsDTOReq__links_xa_rel_xt ..|> LinkRelation
  ContactsDTOReq__links_xa ..|> Link
  ContactsDTORes__links_xt ..|> Links
  OrderDTO__links_xt ..|> Links
  ProductDTO__links_xt ..|> Links
  UserDTOReq__links_xa_rel_xt ..|> LinkRelation
  UserDTOReq__links_xa ..|> Link
  UserDTORes__links_xt ..|> Links
  class CreditCardDTO
  CartDTO: +put(/customer/cart)
  CartDTO: +put(/customer/cart/delivery)
  CartItemDTOReq: +put(/customer/cart)
  ContactsDTOReq: +put(/customer/contacts)
  ContactsDTORes: +put(/customer/contacts)
  CreditCardDTO: +post(/customer/cart/pay)
  OrderDTO: +post(/customer/cart/pay)
  UserDTOReq: +post(/register)
  UserDTORes: +post(/register)