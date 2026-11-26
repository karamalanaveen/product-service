# Product Service REST API (with Resilience4j Circuit Breaker)

A microservice built with **Java 17**, **Spring Boot 3**, **Spring Data JPA**, **MySQL**, and **Resilience4j Circuit Breaker**.

---

## 📌 Features

1. **Product CRUD APIs**:
   - Manage products (`name`, `description`, `price`, `quantity`, `category`).
2. **Resilience4j Circuit Breaker**:
   - Protects inter-service calls from `product-service` to `coupon-service1`.
   - Annotated with `@CircuitBreaker(name = "couponService", fallbackMethod = "getCouponFallback")`.
   - Handles `CallNotPermittedException` when circuit is OPEN, as well as downstream network timeouts or connection failures.
   - Graceful fallback: If `coupon-service1` fails or circuit is open, returns a safe default (0% discount) so customers can still purchase products without a 500 error.
3. **Resilience4j Configuration**:
   - `slidingWindowType`: `COUNT_BASED`
   - `slidingWindowSize`: `10`
   - `minimumNumberOfCalls`: `5`
   - `failureRateThreshold`: `50%`
   - `waitDurationInOpenState`: `10s`
   - `permittedNumberOfCallsInHalfOpenState`: `3`
   - `automaticTransitionFromOpenToHalfOpenEnabled`: `true`
4. **Spring Boot Actuator Monitoring**:
   - Exposes `/actuator/health` and `/actuator/circuitbreakers` for real-time circuit state observation (`CLOSED`, `OPEN`, `HALF_OPEN`).

---

## ⚙️ Configuration

The MySQL datasource uses `DB_URL` (defaults to the local `product_db` database), `DB_USERNAME`, and `DB_PASSWORD`. Set the username and password in the environment before starting the service; they have no source-controlled defaults.

---

## 📡 Endpoints

Base URL: `http://localhost:8081/api/products`

### Standard CRUD:
* `POST /api/products`: Create a product (`201 CREATED`)
* `GET /api/products`: List all products (`200 OK`)
* `GET /api/products/{id}`: Get product by ID (`200 OK` or `404 NOT FOUND`)
* `PUT /api/products/{id}`: Update product (`200 OK` or `404 NOT FOUND`)
* `DELETE /api/products/{id}`: Delete product (`200 OK` or `404 NOT FOUND`)

### Circuit Breaker Protected Endpoint:
* **Method:** `GET`
* **URL:** `/api/products/{id}/price?couponCode={code}`
* **Example:**
```bash
curl -X GET "http://localhost:8081/api/products/1/price?couponCode=SUMMER20"
```

#### Success Response (coupon-service UP):
```json
{
  "productId": 1,
  "productName": "Wireless Noise-Canceling Headphones",
  "originalPrice": 199.99,
  "discountApplied": 20.00,
  "finalPrice": 159.99,
  "couponCode": "SUMMER20",
  "fallbackApplied": false,
  "statusMessage": "Coupon 'SUMMER20' successfully applied with 20.00% discount."
}
```

#### Fallback Response (coupon-service DOWN or Circuit OPEN):
```json
{
  "productId": 1,
  "productName": "Wireless Noise-Canceling Headphones",
  "originalPrice": 199.99,
  "discountApplied": 0,
  "finalPrice": 199.99,
  "couponCode": "SUMMER20",
  "fallbackApplied": true,
  "statusMessage": "Coupon service is currently unavailable (Circuit Breaker OPEN). Safe fallback applied with 0% discount."
}
```

---

## 🔍 Observing Circuit Breaker State in Actuator

```bash
# Check circuit breaker health state (CLOSED, OPEN, HALF_OPEN)
curl -X GET http://localhost:8081/actuator/health

# Inspect all circuit breaker instances
curl -X GET http://localhost:8081/actuator/circuitbreakers
```
