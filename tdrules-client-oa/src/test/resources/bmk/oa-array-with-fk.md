---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  Master <--"*" TestArrayInlinePrim_arrPrimInl_xa
  TestArrayInlinePrim *--"*" TestArrayInlinePrim_arrPrimInl_xa
  Master <--"*" TestArrayInlineObj_arrObjInl_xa
  TestArrayInlineObj *--"*" TestArrayInlineObj_arrObjInl_xa
  Master <--"*" TestArrayReference_arrObjRef_xa
  TestArrayReference *--"*" TestArrayReference_arrObjRef_xa
  Master <--"*" ReferencedObject
  TestArrayReference_arrObjRef_xa ..|> ReferencedObject