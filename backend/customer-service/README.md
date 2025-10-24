# Customer Service

The **Customer Service** is responsible for managing customer profiles, preferences, and support tickets.

## Features

- Manage customer profiles and preferences
- Handle support tickets and ticket categories
- Secure endpoints with Spring Security
- Expose RESTful APIs for integration with other microservices

## Package Overview

| Package      | Responsibility                                    |
|--------------|---------------------------------------------------|
| `config`     | Security configuration                            |
| `controller` | REST controllers handling API requests            |
| `dto`        | Data Transfer Objects for API input/output        |
| `model`      | Domain entities representing customers & tickets. |
| `repository` | Data access layer interfaces                      |
| `service`    | Business logic                                    |

## API Endpoints

| Method   | Endpoint                                            | Description                                                         | Required Authorities                                          |
|----------|-----------------------------------------------------|---------------------------------------------------------------------|---------------------------------------------------------------|
| `GET`    | `/api/v1/customers`                                 | Get a paginated list of all customers                               | `ROLE_ADMIN`                                                  |
| `GET`    | `/api/v1/customers/{customerId}/profile`            | Retrieve the profile of a specific customer                         | `ROLE_CUSTOMER` (owner only) or `ROLE_ADMIN`                  |
| `GET`    | `/api/v1/customers/{customerId}/preferences`        | Retrieve customer preferences                                       | `ROLE_CUSTOMER` (owner only) or `ROLE_ADMIN`                  |
| `PUT`    | `/api/v1/customers/{id}/preferences`                | Update customer preferences                                         | `ROLE_CUSTOMER` (owner only)                                  |
| `DELETE` | `/api/v1/customers/{id}`                            | Delete a customer account                                           | `ROLE_ADMIN`                                                  |
| `GET`    | `/api/v1/customers/{customerId}/tickets`            | Get all tickets created by a customer (with pagination and sorting) | `ROLE_CUSTOMER` (owner only) or `ROLE_ADMIN`                  |
| `POST`   | `/api/v1/customers/{customerId}/tickets`            | Create a new support ticket                                         | `ROLE_CUSTOMER`                                               |
| `GET`    | `/api/v1/customers/{customerId}/tickets/{ticketId}` | Retrieve a specific ticket by ID                                    | `ROLE_CUSTOMER` (owner only), `ROLE_SUPPORT`, or `ROLE_ADMIN` |
| `GET`    | `/api/v1/tickets/categories`                        | List all available ticket categories                                | `ROLE_CUSTOMER`, `ROLE_SUPPORT`, or `ROLE_ADMIN`              |
