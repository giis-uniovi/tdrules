classDiagram
  Pet <--"*" Order
  Customer <--"*" Order
  Customer *--"*" Customer_address_xa
  Pet *--"*" Pet_photoUrls_xa
  Pet *--"*" Pet_tags_xa
  Pet *--"1" Pet_category_xt
  Customer_address_xa ..|> Address
  Pet_category_xt ..|> Category
  Pet_tags_xa ..|> Tag
  class User
  class ApiResponse
  Order: +post(/store/order)
  User: +post(/user)
  User: +put(/user/{username})
  Pet: +post(/pet)
  Pet: +put(/pet)