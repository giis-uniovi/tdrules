classDiagram
  EntityReq .. EntityRes : post
  namespace post__my__param__postreqres {
    class EntityReq
    class EntityRes
  }
  namespace post__my_postreqreq {
    class EntityReqRes
  }
  EntityReq0 .. EntityRes0 : post
  namespace post__my_postreqres0 {
    class EntityReq0
    class EntityRes0
  }
  EntityReq0 .. EntityRes0 : post
  namespace post__rep0_postreqres0 {
    class EntityReq0_r1
    class EntityRes0_r1
  }
  style EntityReq0_r1 fill:#fff,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5;
  style EntityRes0_r1 fill:#fff,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5;
  EntityReq1 .. EntityRes1 : post
  namespace post__my_postreqres1 {
    class EntityReq1
    class EntityRes1
  }
  EntityReq1 .. EntityRes1other : post
  namespace post__rep1_postreqres1 {
    class EntityReq1_r1
    class EntityRes1other
  }
  style EntityReq1_r1 fill:#fff,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5;
  EntityReq2 .. EntityRes2 : post
  namespace post__my_postreqres2 {
    class EntityReq2
    class EntityRes2
  }
  EntityRes2 .. EntityReq2other : post
  namespace post__rep2_postreqres2 {
    class EntityRes2_r1
    class EntityReq2other
  }
  style EntityRes2_r1 fill:#fff,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5;
  EntityReq3 .. EntityRes3 : post
  namespace post__my_postreqres3 {
    class EntityReq3
    class EntityRes3
  }
  namespace post__rep3_postreqreq3 {
    class EntityRes3_r1
  }
  style EntityRes3_r1 fill:#fff,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5;
  EntityReqPut .. EntityResPut : put
  namespace put__my_postreqres {
    class EntityReqPut
    class EntityResPut
  }
  class EntityReqPut
  class EntityResPut
  class EntityReq
  class EntityRes
  class EntityReqRes
  class EntityReq0
  class EntityRes0
  class EntityReq1
  class EntityRes1
  class EntityRes1other
  class EntityReq2
  class EntityRes2
  class EntityReq2other
  class EntityReq3
  class EntityRes3
  EntityReqPut: +put(/my/postreqres)
  EntityResPut: +put(/my/postreqres)
  EntityReq: +post(/my/{param}/postreqres)
  EntityRes: +post(/my/{param}/postreqres)
  EntityReqRes: +post(/my/postreqreq)
  EntityReq0: +post(/my/postreqres0)
  EntityReq0: +post(/rep0/postreqres0)
  EntityRes0: +post(/my/postreqres0)
  EntityRes0: +post(/rep0/postreqres0)
  EntityReq1: +post(/my/postreqres1)
  EntityReq1: +post(/rep1/postreqres1)
  EntityRes1: +post(/my/postreqres1)
  EntityRes1other: +post(/rep1/postreqres1)
  EntityReq2: +post(/my/postreqres2)
  EntityRes2: +post(/my/postreqres2)
  EntityRes2: +post(/rep2/postreqres2)
  EntityReq2other: +post(/rep2/postreqres2)
  EntityReq3: +post(/my/postreqres3)
  EntityRes3: +post(/my/postreqres3)
  EntityRes3: +post(/rep3/postreqreq3)