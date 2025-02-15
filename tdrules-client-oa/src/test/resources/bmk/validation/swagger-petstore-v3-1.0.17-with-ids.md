---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  Pet <--"*" Order
  Customer <--"*" Order
  Customer *--"*" Customer_address_xa
  Pet *--"1" Pet_category_xt
  Pet *--"*" Pet_photoUrls_xa
  Pet *--"*" Pet_tags_xa
  Customer_address_xa ..|> Address
  Pet_category_xt ..|> Category
  Pet_tags_xa ..|> Tag
  class User
  class ApiResponse
  Order: +post(/store/order)
  User: +post(/user)
  User: +post(/user/createWithList)
  User: +put(/user/{username})
  Pet: +post(/pet)
  Pet: +put(/pet)
  ApiResponse: +post(/pet/{petId}/uploadImage)