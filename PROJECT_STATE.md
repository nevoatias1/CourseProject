# Project State

## Project goal

Build an online electronics and gadgets store with a Spring Boot backend and a React frontend. The backend provides authentication, product/category browsing, user shopping data, and order checkout; the React client will be added in Stage 5.

## Stack and versions

- Java 21
- Spring Boot 3.3.4
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Spring Web, Security, JDBC, Actuator, and H2
- JJWT 0.9.1
- H2 in-memory database for local development
- MySQL connector 8.0.28 is present in the Maven dependencies for compatibility with the reference project
- The reference dependency set still includes Spring Data JPA, but application persistence is implemented with JDBC repositories and `data.sql`; no JPA entities or repositories are used

## Architecture

The server follows the lecturer's `spring-boot-security` project structure and keeps its security configuration with minimal changes. Requests flow through controller → service → JDBC repository → RowMapper. `data.sql` creates and seeds the database.

The base package is `com.example.store` with these subpackages:

- `controller`
- `model`
- `security`
- `service`
- `repository`
- `repository.mapper`
- `utils`

## Folder structure

```text
CourseProject/
├── server/
│   ├── .mvn/
│   ├── mvnw, mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/store/
│       │   ├── controller/
│       │   ├── model/
│       │   ├── repository/
│       │   │   └── mapper/
│       │   ├── security/
│       │   ├── service/
│       │   └── utils/
│       ├── main/resources/
│       │   ├── application.yaml
│       │   └── data.sql
│       └── test/java/com/example/store/
├── .gitignore
├── README.md
└── client/ (planned; not created yet)
```

## Completed stages

- Stage 1: security base, JWT authentication, user model, roles, H2 setup, and user/admin endpoints.
- Stage 2: JDBC database layer for categories, products, favorites, carts, orders, and order items; seeded catalog data.
- Stage 3: public read-only category and product API with category filtering and case-insensitive name search.
- Stage 4a: authenticated cart and favorites APIs with JWT-derived ownership, quantity validation, and stock limits.
- Stage 4b: transactional authenticated order checkout, stock decrement, order history, and order detail retrieval.
- Stage 5: next; create the React app in `client/` and connect login/register.

## Database schema

`src/main/resources/data.sql` creates and seeds:

- `users`: username (`VARCHAR`) is the primary key; email is unique; role is `USER` or `ADMIN`.
- `categories`: auto-increment integer primary key and unique name.
- `products`: auto-increment primary key, name, description, decimal price, image URL, stock, and category foreign key.
- `favorites`: composite primary key `(username, product_id)`.
- `cart_items`: composite primary key `(username, product_id)` and quantity.
- `orders`: auto-increment primary key, username, creation timestamp, server-calculated total, status, and shipping address.
- `order_items`: composite primary key `(order_id, product_id)`, quantity, and snapshotted unit price.

User and order foreign keys cascade on delete as defined in the schema. The seed contains two users, six categories, and 24 products (four per category).

## Auth/authorization state

Authentication uses `POST /authenticate`, `AuthenticationService`, `CustomUserDetailsService`, `JwtUtil`, and `JwtRequestFilter`. Sessions are stateless. The JWT username is the sole source of identity for user-scoped operations; it is never accepted from a URL, query parameter, or request body.

`CustomUser` keeps `username` as the String primary key; there is no user ID. Roles are `USER` and `ADMIN`. Registration ignores a client-supplied role and always saves `USER`. Passwords are BCrypt encoded and are not logged.

Public routes go in the existing `permitAll` line. Authenticated user routes go in the existing `hasAnyAuthority("USER", "ADMIN")` line. Admin routes use `hasAuthority("ADMIN")`. CORS allows `http://localhost:3000`.

## Endpoints

### Public

- `POST /authenticate`
- `POST /users/register`
- `GET /categories`
- `GET /products`
- `GET /products/{id}`
- `/h2-console/**`

`GET /products` supports optional `categoryId` and case-insensitive `search` query parameters.

### USER or ADMIN

- `GET /users`
- `PUT /users`
- `DELETE /users`
- `GET /cart`
- `POST /cart`
- `PUT /cart/{productId}`
- `DELETE /cart/{productId}`
- `DELETE /cart`
- `GET /favorites`
- `POST /favorites/{productId}`
- `DELETE /favorites/{productId}`
- `POST /orders`
- `GET /orders`
- `GET /orders/{id}`

Checkout takes only a required shipping address from the client. Product prices, order totals, line totals, and stock changes are computed or validated on the server.

Future cart, checkout, and order pages should reuse the client `ProductVisual` component for consistent category icons instead of introducing page-specific product imagery.

### ADMIN only

- `GET /admin/all-users`
- `DELETE /admin/delete-user/{username}`

## Tests and verification status

`./mvnw.cmd clean verify` currently passes. The suite includes the context test plus three checkout integration tests covering:

- successful order creation, order items, stock reduction, and cart clearing;
- insufficient stock returning 400 with order, stock, and cart changes rolled back;
- one user receiving 404 when attempting to read another user's order.

The packaged application starts on port 9000, and anonymous `GET /products` returns the seeded 24-product catalog. The temporary verification server was stopped after testing.

## Known issues

- Order history displays live product names from `products`; prices are snapshotted in `order_items`.
- Local development uses an in-memory H2 database, so data disappears on restart.
- The current reference dependency set includes JPA even though the application intentionally uses JDBC and no JPA persistence model.
- Products are currently represented by hard deletes in the repository layer. The planned policy is to never hard-delete products; Stage 9 will introduce an admin-controlled soft delete.

## Next step

Stage 5: create the React application in `client/` and connect login and registration first, then public catalog and authenticated store flows.

## AI handoff

Continue preserving the lecturer-reference package structure and the minimal-change security configuration. Do not introduce JPA repositories or move user identity into request data. Keep public routes in the existing `permitAll` matcher and secured USER/ADMIN routes in its existing authority matcher. All cart, favorite, and order ownership must be derived from the JWT. Keep pricing and totals server-side.

The planned deployment is PostgreSQL via Spring profiles, with the server deployed on Render using Docker and the React client deployed on Netlify. Payment is a simple simulation only; no real payment provider is planned.

## Open questions

- Which product fields should be retained in historical order items beyond the current snapshotted price (for example, a product-name snapshot)?
- What exact soft-delete field and admin workflow should Stage 9 use?
- Which PostgreSQL profile values and Render/Netlify environment variables should be finalized before deployment?
