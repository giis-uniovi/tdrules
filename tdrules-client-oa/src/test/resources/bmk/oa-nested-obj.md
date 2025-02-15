---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  TestChildOfObject_obj_xt *--"1" TestChildOfObject_obj_xt_value_xt
  TestChildOfObject *--"1" TestChildOfObject_obj_xt
  TestChildOfArray_arr_xa *--"1" TestChildOfArray_arr_xa_value_xt
  TestChildOfArray *--"*" TestChildOfArray_arr_xa