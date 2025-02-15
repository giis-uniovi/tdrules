classDiagram
  TestArrayInline *--"*" TestArrayInline_arrPrimInl_xa
  TestArrayInline *--"*" TestArrayInline_arrObjInl_xa
  TestArrayReference *--"*" TestArrayReference_arrObjRef_xa
  TestArrayReference_arrObjRef_xa ..|> ReferencedObject