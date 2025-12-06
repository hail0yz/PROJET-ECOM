# PROJET-ECOM

Authors:
- Victoria LAGRANGE
- Mohamed SOUID
- Mohamed AFKIR
- Déborah YANG

M2 GI - Classique\
2025-2026

# Requirements
- Java 11+
- Maven 3+
- Docker & Docker Compose
- PostgreSQL
- Node.js & npm
- Angular CLI

# Deployment Instructions

Below is the command to launch the application. You must be at the root of the project to run it.
```
./launchAppli.sh
```
This script will build and start all the services with their jar file.
It will then start a local development server for the frontend, which will be accessible at `http://localhost:4200/` in you browser.

It may take a few minutes for the application to be ready.

`launchAppli.sh` script does the following you can also do it manually if you prefer:
1. Package all backend services with Maven to generate their jar files using the script `./packageAll.sh`.
2. Start all backend services and their databases using Docker Compose with the script `./startAll.sh`.
3. Start the frontend development server with the script `./startFront.sh`.

Those scripts can also be run separately if needed:

- To compile and generate all backend jar files:
```
./packageAll.sh
```
- To start all backend services and databases as well as Keycloak server:
```
./startAll.sh
```
- To start the frontend development server:
```
./startFront.sh
```

A list of available ports for each service is provided in the "Backend" section below.

# Project Structure
The project is structured as follows:

- **_backend/_** : Contains all backend microservices
  - **_api-gateway/_** : API Gateway service
  - **_bookService/_** : Book Service
  - **_cart-service/_** : Cart Service
  - **_customer-service/_** : Customer Service
  - **_notification/_** : Notification Service
  - **_order/_** : Order Service
  - **_registry-server/_** : Eureka Service Registry
- **_frontend/_** : Contains the Angular frontend application
- **_keycloak/_** : Keycloak server configuration and setup files
- **_payment/_** : Payment Service
- **_out/_** : Output directory containing log files
- **_docker-compose.yml_** : Docker Compose configuration file for backend services and databases

# Backend

### Services Overview and their Ports

| **Service Name**         | **Port** | **Database** | **DB Port (locally)** | **DB Name**       | **DB username:password** | **Eureka Registration Name** |
|--------------------------|----------|--------------|-----------------------|-------------------|--------------------------|------------------------------|
| **eureka-server**        | `8761`   | —            | —                     | —                 | —                        | `eureka-server`              |
| **api-gateway**          | `8080`   | —            | —                     | —                 | —                        | `api-gateway`                |
| **customer-service**     | `0`      | PostgreSQL   | `5434`                | `customers_db`    | postgres:password        | `user-service`               |
| **cart-service**         | `0`      | PostgreSQL   | `5433`                | `carts_db`        | postgres:password        | `cart-service`               |
| **book-service**         | `0`      | PostgreSQL   | `5437`                | `books_db`        | postgres:password        | `bookService`                |
| **order-service**        | `0`      | PostgreSQL   | `5436`                | `orders_db`       | postgres:password        | `order-service`              |
| **payment-service**      | `0`      | PostgreSQL   | `5438`                | `payment_db`      |                          | `payment-service`            |
| **notification-service** | `8080`   | —            | —                     | `notification_db` |                          | `notification-service`       |


### Running Individual Services

You can run each backend service individually if needed by typing the following commands in directory _backend/<service_name>/_ :

```
mvn clean
mvn install
mvn spring-boot:run
```

Or you can type these commands in the same directory, if you want to generate the jar file:
```
mvn clean package
java -jar target/<service_name>-0.0.1-SNAPSHOT.jar
```

For the payment service, you have to set the Stripe API key in a `.env` file in the payment service directory before running it, either by writing it like this:
`STRIPE_API_KEY=sk_test_your_key_here`
or by exporting it in your terminal:
`export STRIPE_API_KEY=sk_test_your_key_here`.

You can also test the payment service API with curl commands as shown below:

```bash
curl -X POST http://localhost:8080/api/payments   -H "Content-Type: application/json"   -d '{
    "orderId": 1,
    "amount": 99.99,
    "customerEmail": "test@example.com",
    "paymentMethod": "CARD"
  }'
```
  which should return:
  ```bash
  {"paymentId":1,"orderId":1,"status":"PENDING","transactionId":"TXN-31D0248ABAF2481D","message":"Payment intent created successfully. Use client secret to complete payment.","failureReason":null,"amount":99.99,"paymentMethod":"CARD","stripePaymentIntentId":"xxx","clientSecret":"xxx"}
```
You can get all payments with:
```bash
curl http://localhost:8080/api/payments
```

# Extensions
TODO if any...
