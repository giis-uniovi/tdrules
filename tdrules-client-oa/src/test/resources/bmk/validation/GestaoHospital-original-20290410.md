---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  Location_location_xt *--"*" Location_location_xt_coordinates_xa
  Location_position_xt *--"*" Location_position_xt_coordinates_xa
  Patient_location_xt_location_xt *--"*" Patient_location_xt_location_xt_coordinates_xa
  Patient_location_xt_position_xt *--"*" Patient_location_xt_position_xt_coordinates_xa
  Location <--"*" Patient_location_xt
  Patient_location_xt *--"1" Patient_location_xt_location_xt
  Patient_location_xt *--"1" Patient_location_xt_position_xt
  Patient *--"1" Patient_location_xt
  GeoJsonPoint *--"*" GeoJsonPoint_coordinates_xa
  Location *--"1" Location_location_xt
  Location *--"1" Location_position_xt
  Location_location_xt ..|> GeoJsonPoint
  Location_position_xt ..|> GeoJsonPoint
  Patient_location_xt_location_xt ..|> GeoJsonPoint
  Patient_location_xt_position_xt ..|> GeoJsonPoint
  Patient_location_xt ..|> Location
  class HospitalDTO
  class LocationDTO
  class ProductDTO
  HospitalDTO: +post(/v1/hospitais/)
  HospitalDTO: +put(/v1/hospitais/{hospital_id})
  Patient: +post(/v1/hospitais/{hospital_id}/pacientes/checkin)
  Patient: +post(/v1/hospitais/{hospital_id}/pacientes/checkout)
  Patient: +put(/v1/hospitais/{hospital_id}/pacientes/{patientId})
  ProductDTO: +post(/v1/hospitais/{hospital_id}/estoque)
  ProductDTO: +put(/v1/hospitais/{hospital_id}/estoque/{produto_id})