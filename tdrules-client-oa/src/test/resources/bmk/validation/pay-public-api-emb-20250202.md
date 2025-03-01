---
  config:
    class:
      hideEmptyMembersBox: true
---
classDiagram
  direction LR
  Agreement .. CreateAgreementRequest : post
  namespace post__v1_agreements {
    class Agreement
    class CreateAgreementRequest
  }
  namespace post__v1_auth {
    class AuthorisationRequest
  }
  CreateCardPaymentRequest .. CreatePaymentResult : post
  namespace post__v1_payments {
    class CreateCardPaymentRequest
    class CreatePaymentResult
  }
  PaymentRefundRequest .. Refund : post
  namespace post__v1_payments__paymentId__refunds {
    class PaymentRefundRequest
    class Refund
  }
  Agreement_payment_instrument_xt_CardDetails_xt *--"1" Agreement_payment_instrument_xt_CardDetails_xt_billing_address_xt
  Agreement_payment_instrument_xt *--"1" Agreement_payment_instrument_xt_CardDetails_xt
  Agreement *--"1" Agreement_payment_instrument_xt
  CreateCardPaymentRequest_prefilled_cardholder_details_xt *--"1" CreateCardPaymentRequest_prefilled_cardholder_details_xt_billing_address_xt
  Agreement <--"*" CreateCardPaymentRequest
  CreateCardPaymentRequest *--"1" CreateCardPaymentRequest_metadata_xt
  CreateCardPaymentRequest *--"1" CreateCardPaymentRequest_prefilled_cardholder_details_xt
  CreatePaymentResult_card_details_xt *--"1" CreatePaymentResult_card_details_xt_billing_address_xt
  CreatePaymentResult *--"1" CreatePaymentResult_card_details_xt
  CreatePaymentResult *--"1" CreatePaymentResult_metadata_xt
  CreatePaymentResult *--"1" CreatePaymentResult_refund_summary_xt
  CreatePaymentResult *--"1" CreatePaymentResult_settlement_summary_xt
  CreatePaymentResult *--"1" CreatePaymentResult_state_xt
  Refund *--"1" Refund_settlement_summary_xt
  Agreement_payment_instrument_xt_CardDetails_xt_billing_address_xt ..|> Address
  Agreement_payment_instrument_xt_CardDetails_xt ..|> CardDetailsFromResponse
  Agreement_payment_instrument_xt ..|> PaymentInstrument
  CreateCardPaymentRequest_metadata_xt ..|> ExternalMetadata
  CreateCardPaymentRequest_prefilled_cardholder_details_xt_billing_address_xt ..|> Address
  CreateCardPaymentRequest_prefilled_cardholder_details_xt ..|> PrefilledCardholderDetails
  CreatePaymentResult_card_details_xt_billing_address_xt ..|> Address
  CreatePaymentResult_card_details_xt ..|> CardDetailsFromResponse
  CreatePaymentResult_metadata_xt ..|> ExternalMetadata
  CreatePaymentResult_refund_summary_xt ..|> RefundSummary
  CreatePaymentResult_settlement_summary_xt ..|> PaymentSettlementSummary
  CreatePaymentResult_state_xt ..|> PaymentState
  Refund_settlement_summary_xt ..|> RefundSettlementSummary
  class AuthorisationRequest
  class CreateAgreementRequest
  class PaymentRefundRequest
  Agreement: +post(/v1/agreements)
  AuthorisationRequest: +post(/v1/auth)
  CreateAgreementRequest: +post(/v1/agreements)
  CreateCardPaymentRequest: +post(/v1/payments)
  CreatePaymentResult: +post(/v1/payments)
  PaymentRefundRequest: +post(/v1/payments/{paymentId}/refunds)
  Refund: +post(/v1/payments/{paymentId}/refunds)