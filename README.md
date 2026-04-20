# Product & Order Management System

A Spring Boot application for managing products and orders with two roles — ADMIN and USER.

Built with Spring Boot 3.5, Spring Security (session-based auth), Spring Data JPA, MySQL, and Thymeleaf for the UI.

---

## Setup

**Requirements:** Java 21, Maven, MySQL on port 3307

1. Create the database:

```sql
CREATE DATABASE product_order_db;
```

2. Update `src/main/resources/application.properties` with your MySQL credentials if they differ from the defaults (`root` / `root`).

3. Run:

```bash
mvn spring-boot:run
```

4. Visit `http://localhost:8080`

Tables are created automatically on first run.

---

## Roles

- **ADMIN** — any account registered with a `@jforce.com` email
- **USER** — everyone else

---

## Database Schema

```
users         id, username, email, password, role, enabled
products      id, name, description, price, quantity, enabled
cart          id, user_id
cart_items    id, cart_id, product_id, quantity
orders        id, user_id, placed_at, status, total_amount
order_items   id, order_id, product_id, quantity, price_at_order
```

---

## REST API

Authentication uses session cookies. Log in via `POST /login` with form fields `username` and `password` first, then reuse the session cookie for all subsequent requests.

**Admin — Products**

```
GET    /api/admin/products              list all products
POST   /api/admin/products              add a product
PUT    /api/admin/products/{id}         update a product
PATCH  /api/admin/products/{id}/toggle  enable or disable
```

**User — Products**

```
GET    /api/products        list active products
GET    /api/products/{id}   get a single product
```

**User — Cart**

```
GET    /api/cart                    view cart
POST   /api/cart/add                add item
PUT    /api/cart/update/{itemId}    update quantity
DELETE /api/cart/remove/{itemId}    remove item
```

**User — Orders**

```
POST   /api/orders/place    place order from cart
GET    /api/orders          list my orders
GET    /api/orders/{id}     get order detail
```

---