# PROJET-ECOM
# Compile and run project

-- Set api key in .env
STRIPE_API_KEY= sk_test_votre_cle_ici

in a terminal
```bash
mvn clean install
mvn springboot:run
```
# To test with curl
In another terminal
--
curl -X POST http://localhost:8080/api/payments   -H "Content-Type: application/json"   -d '{
    "orderId": 1,
    "amount": 99.99,
    "customerEmail": "test@example.com",
    "paymentMethod": "CARD"
  }'

  which should return
  {"paymentId":1,"orderId":1,"status":"PENDING","transactionId":"TXN-31D0248ABAF2481D","message":"Payment intent created successfully. Use client secret to complete payment.","failureReason":null,"amount":99.99,"paymentMethod":"CARD","stripePaymentIntentId":"xxx","clientSecret":"xxx"}

-- get all payments with

curl http://localhost:8080/api/payments
