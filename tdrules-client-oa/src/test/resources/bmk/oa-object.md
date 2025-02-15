---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  ReferencedKObject <--"*" TestComposite_objKRef_xt
  TestComposite *--"1" TestComposite_objInl_xt
  TestComposite *--"1" TestComposite_objRef_xt
  TestComposite *--"1" TestComposite_objKRef_xt
  TestComposite_objRef_xt ..|> ReferencedObject
  TestComposite_objKRef_xt ..|> ReferencedKObject