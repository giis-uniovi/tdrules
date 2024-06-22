classDiagram
  TestArrayInlinePrim *--"*" TestArrayInlinePrim_arrPrimInl_xa
  Master *--"*" TestArrayInlinePrim_arrPrimInl_xa
  TestArrayInlineObj *--"*" TestArrayInlineObj_arrObjInl_xa
  Master *--"*" TestArrayInlineObj_arrObjInl_xa
  TestArrayReference *--"*" TestArrayReference_arrObjRef_xa
  Master *--"*" TestArrayReference_arrObjRef_xa
  Master <--"*" ReferencedObject
  TestArrayReference_arrObjRef_xa ..|> ReferencedObject