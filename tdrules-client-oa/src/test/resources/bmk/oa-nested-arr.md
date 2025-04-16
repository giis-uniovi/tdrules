---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  TestChildOfObject1_obj_xt *--"*" TestChildOfObject1_obj_xt_arr_xa
  TestChildOfObject1 *--"1" TestChildOfObject1_obj_xt
  TestChildOfObject2_obj_xt *--"*" TestChildOfObject2_obj_xt_arr_xa
  TestChildOfObject2 *--"1" TestChildOfObject2_obj_xt
  TestChildOfObject1 <--"*" TestJoin12
  TestChildOfObject2 <--"*" TestJoin12
  TestChildOfObject4_obj0_xt_obj1_xt_arr_xa *--"1" TestChildOfObject4_obj0_xt_obj1_xt_arr_xa_obj2_xt
  TestChildOfObject4_obj0_xt_obj1_xt *--"*" TestChildOfObject4_obj0_xt_obj1_xt_arr_xa
  TestChildOfObject4_obj0_xt *--"1" TestChildOfObject4_obj0_xt_obj1_xt
  TestChildOfObject4 *--"1" TestChildOfObject4_obj0_xt
  TestChildOfArray1_arr_xa *--"*" TestChildOfArray1_arr_xa_arr2_xa
  TestChildOfArray1 *--"*" TestChildOfArray1_arr_xa
  TestChildOfArray2_arr_xa *--"*" TestChildOfArray2_arr_xa_arr2_xa
  TestChildOfArray2 *--"*" TestChildOfArray2_arr_xa