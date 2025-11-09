# PROJET-ECOM
___

# Backend

| **Service Name**         | **Port** | **Database** | **DB Port (locally)** | **DB Name**       | **DB username:password** | **Eureka Registration Name** |
|--------------------------|----------|--------------|-----------------------|-------------------|--------------------------| ---------------------------- |
| **eureka-server**        | `8761`   | —            | —                     | —                 | —                        | `eureka-server`              |
| **api-gateway**          | `8080`   | —            | —                     | —                 | —                        | `api-gateway`                |
| **customer-service**     | `0`      | PostgreSQL   | `5434`                | `customers_db`    | postgres:password        | `user-service`               |
| **cart-service**         | `0`      | PostgreSQL   | `5433`                | `carts_db`        | postgres:password        | `cart-service`               |
| **product-service**      | `8080`   | H2           | —                     | `product_db`      |                          | `product-service`            |
| **order-service**        | `0`      | PostgreSQL   | `5436`                | `orders_db`       | postgres:password        | `order-service`              |
| **payment-service**      | `8080`   | H2           | —                     | `payment_db`      |                          | `payment-service`            |
| **notification-service** | `8080`   | —            | —                     | `notification_db` |                          | `notification-service`       |


### Book Service
    - How to run

Type the following commands in a terminal while being at `backend/bookService/` directory:
```
mvn clean
mvn install
mvn spring-boot:run
```

Else, you can type these commands, still in `backend/bookService/` directory, if you want to generate the jar file:
```
mvn clean package
java -jar target/bookService-0.0.1-SNAPSHOT.jar
```

The application now running you can type `http://localhost:8080/api/v1/books` on the research bar of your navigator to display all the books.

### Payment Service

-- Set api key in `.env` `STRIPE_API_KEY=sk_test_votre_cle_ici`

in a terminal
```bash
mvn clean install
mvn springboot:run
```

#### To test with curl in another terminal

---
```bash
curl -X POST http://localhost:8080/api/payments   -H "Content-Type: application/json"   -d '{
    "orderId": 1,
    "amount": 99.99,
    "customerEmail": "test@example.com",
    "paymentMethod": "CARD"
  }'
```
  which should return
  ```bash
  {"paymentId":1,"orderId":1,"status":"PENDING","transactionId":"TXN-31D0248ABAF2481D","message":"Payment intent created successfully. Use client secret to complete payment.","failureReason":null,"amount":99.99,"paymentMethod":"CARD","stripePaymentIntentId":"xxx","clientSecret":"xxx"}
```
-- get all payments with
```bash
curl http://localhost:8080/api/payments
```
