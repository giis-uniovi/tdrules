classDiagram
  TestUndefined1 *--"1" TestUndefined1_objRef_xt
  TestUndefined1_objRef_xt ..|> ExistingObject
  class TestUndefined0
  note for TestUndefined0 "Undefined $ref:<br/>NotExistingObject0"
  note for TestUndefined1 "Undefined $ref:<br/>NotExistingObject1<br/>NotExistingArray1[]"