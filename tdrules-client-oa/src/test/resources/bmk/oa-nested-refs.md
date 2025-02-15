---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  Master <--"*" ObjOfArr_arr_xa
  Master <--"*" TestObjOfArr_obj_xt_arr_xa
  Master <--"*" TestObjOfArr_obj_xt
  TestObjOfArr_obj_xt *--"*" TestObjOfArr_obj_xt_arr_xa
  TestObjOfArr *--"1" TestObjOfArr_obj_xt
  Master <--"*" ObjOfArr
  ObjOfArr *--"*" ObjOfArr_arr_xa
  Master <--"*" Arr
  Master <--"*" ArrOfObj_obj_xt
  Master <--"*" TestArrOfObj_arr_xa_obj_xt
  Master <--"*" TestArrOfObj_arr_xa
  TestArrOfObj_arr_xa *--"1" TestArrOfObj_arr_xa_obj_xt
  TestArrOfObj *--"*" TestArrOfObj_arr_xa
  Master <--"*" ArrOfObj
  ArrOfObj *--"1" ArrOfObj_obj_xt
  Master <--"*" Obj
  ObjOfArr_arr_xa ..|> Arr
  TestObjOfArr_obj_xt_arr_xa ..|> Arr
  TestObjOfArr_obj_xt ..|> ObjOfArr
  ArrOfObj_obj_xt ..|> Obj
  TestArrOfObj_arr_xa_obj_xt ..|> Obj
  TestArrOfObj_arr_xa ..|> ArrOfObj