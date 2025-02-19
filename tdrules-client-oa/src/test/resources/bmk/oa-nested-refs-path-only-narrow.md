---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  TestObjOfArr_obj_xt *--"*" TestObjOfArr_obj_xt_arr_xa
  TestObjOfArr *--"1" TestObjOfArr_obj_xt
  TestArrOfObj_arr_xa *--"1" TestArrOfObj_arr_xa_obj_xt
  TestArrOfObj *--"*" TestArrOfObj_arr_xa
  TestObjOfArr_obj_xt_arr_xa ..|> Arr
  TestObjOfArr_obj_xt ..|> ObjOfArr
  TestArrOfObj_arr_xa_obj_xt ..|> Obj
  TestArrOfObj_arr_xa ..|> ArrOfObj
  TestObjOfArr: +post(/api/p1)
  TestArrOfObj: +post(/api/p1)