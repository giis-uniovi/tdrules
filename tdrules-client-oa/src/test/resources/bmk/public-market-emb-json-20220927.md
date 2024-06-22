classDiagram
  CartDTO_cartItems_xa *--"1" CartDTO_cartItems_xa__links_xt
  CartDTO *--"1" CartDTO__links_xt
  CartItemDTOReq__links_xa *--"1" CartItemDTOReq__links_xa_rel_xt
  CartItemDTORes *--"1" CartItemDTORes__links_xt
  ContactsDTOReq__links_xa *--"1" ContactsDTOReq__links_xa_rel_xt
  ContactsDTORes *--"1" ContactsDTORes__links_xt
  Link *--"1" Link_rel_xt
  OrderDTO *--"1" OrderDTO__links_xt
  ProductDTO *--"1" ProductDTO__links_xt
  UserDTOReq__links_xa *--"1" UserDTOReq__links_xa_rel_xt
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
  class CartItemDTOReq
  class ContactsDTOReq
  class CreditCardDTO
  class UserDTOReq
  CartItemDTOReq: +put(/customer/cart)
  ContactsDTOReq: +put(/customer/contacts)
  CreditCardDTO: +post(/customer/cart/pay)
  UserDTOReq: +post(/register)