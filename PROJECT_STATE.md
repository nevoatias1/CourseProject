# Project State

## Project goal

Atias Express is an online electronics and gadgets store with a Spring Boot/JDBC backend and a React client. The completed application supports public catalog browsing, JWT accounts, favorites, cart and simulated checkout, customer order history, and an ADMIN dashboard for products, inventory, orders, and users.

## Stack and versions

- Java 21
- Spring Boot 3.3.4 and Maven Wrapper
- Spring Web, Spring Security, Spring JDBC, Actuator, H2, and JJWT 0.9.1
- MySQL connector remains in the reference dependency set; local development uses H2
- Spring Data JPA is present as a dependency for reference compatibility, but no JPA entities or repositories are used
- React 18.3.1, Vite 5.4.10, React Router 6.28.0
- Bootstrap 5.3.3, React-Bootstrap 2.10.5, Bootstrap Icons
- Client uses `fetch`; no Redux, Context API, or Axios

Deployment architecture: the React client is deployed on Netlify, the Dockerized Spring Boot server is deployed on Render, and production persistence uses Neon PostgreSQL. Local development continues to use H2, `data.sql`, port 9000, and the Vite client on port 3000.

Deployment environment variable names (values are intentionally not stored here): `SPRING_PROFILES_ACTIVE`, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`, `PORT`, and client-side `VITE_API_URL`.

## Architecture

The server is based on the lecturer's `spring-boot-security` project. Its package structure and security configuration are preserved with minimal changes. Requests follow controller → service → JDBC repository → RowMapper. Database tables and local seed data are defined in `server/src/main/resources/data.sql`.

The client uses route components and prop-passed auth state in `App`. API calls are centralized in `client/src/api.js`. `ProductVisual` is the shared category-icon visual for catalog, cart, checkout, confirmation, favorites, orders, and admin product views. `styles/theme.css` centralizes the visual theme.

## Folder structure

```text
CourseProject/
├── server/
│   ├── .mvn/, mvnw, mvnw.cmd, pom.xml
│   └── src/
│       ├── main/java/com/example/store/
│       │   ├── controller/
│       │   ├── model/
│       │   ├── repository/mapper/
│       │   ├── repository/
│       │   ├── security/
│       │   ├── service/
│       │   └── utils/
│       ├── main/resources/application.yaml
│       ├── main/resources/data.sql
│       └── test/java/com/example/store/
├── client/
│   ├── src/api.js
│   ├── src/components/
│   ├── src/pages/
│   ├── src/styles/
│   └── package.json
├── .gitignore
├── README.md
└── PROJECT_STATE.md
```

## Completed stages

- Stage 1 — security base, user model, roles, JWT authentication, H2, user endpoints, and initial admin endpoint.
- Stage 2 — JDBC models, repositories, mappers, schema, and seed data for categories, products, favorites, carts, orders, and order items.
- Stage 3 — public categories/products API with category filtering and case-insensitive search.
- Stage 4a — authenticated cart and favorites API with JWT-derived ownership, validation, and stock limits.
- Stage 4b — transactional checkout, conditional stock decrement, order history, order details, and rollback tests.
- Stage 5 — React/Vite client, sign-up/sign-in, JWT persistence, profile loading, logout, and responsive store shell.
- Stage 6 — product catalog and product details with URL-backed search/category filters and loading/error/empty states.
- Stage 7 — cart and simulated checkout UI, payment validation, shipping-address choice, confirmation page, cart badge, and token-expiration handling.
- Stage 8a — secured profile update behavior: username and role are immutable through `PUT /users`, optional BCrypt password replacement, and email conflict handling.
- Stage 8b — favorites UI across cards/product pages/account, guest sign-in return flow, and saved/different checkout address selection.
- Stage 9a — ADMIN API: product soft delete/restore, product validation, order administration/status transitions, stock restoration on cancellation, and user role/delete protections.
- Stage 9b — ADMIN dashboard UI for products, orders, statuses, and users.
- Stage 10a — client polish: responsive hamburger navigation, 404 page, footer, URL-backed catalog sorting, shared formatting, page titles, focus styles, and cleanup.

## Stage 11 — deployment complete

Deployment preparation and production hardening now include the PostgreSQL profile/schema, prod-only admin initialization, Render Dockerfile, Netlify SPA redirect, generated-key compatibility across H2/PostgreSQL, and deployment configuration for Netlify, Render, and Neon PostgreSQL.

## Database schema

`data.sql` creates and seeds:

- `users`: `username` is the String primary key; email is unique; roles are `USER` and `ADMIN`.
- `categories`: auto-increment ID and unique name.
- `products`: ID, name, description, price, `image_url`, stock, category foreign key, and `active` soft-delete flag.
- `favorites`: composite `(username, product_id)` key.
- `cart_items`: composite `(username, product_id)` key and quantity.
- `orders`: ID, username, timestamp, server-calculated total, status, and shipping address.
- `order_items`: composite `(order_id, product_id)` key, quantity, and snapshotted unit price.

User and order foreign keys cascade on delete as defined in the schema. Seed data includes two ADMIN users, six categories, and 24 products. `products.image_url` exists for schema compatibility but the client uses category icons instead.

## Auth/authorization state

`POST /authenticate` authenticates through `AuthenticationService`, `CustomUserDetailsService`, BCrypt, `JwtUtil`, and `JwtRequestFilter`. Sessions are stateless. Registration always saves `USER`, and passwords are never logged.

Identity for all user-scoped operations comes only from the JWT username. It is never accepted from a URL, query parameter, or request body. Public routes are added to the existing `permitAll` matcher; customer routes use the existing `hasAnyAuthority("USER", "ADMIN")` matcher; admin routes require `ADMIN`. CORS allows `http://localhost:3000`.

Role changes and account deletion take effect for authorization only after the affected user's JWT expires or the user logs in again, because existing tokens retain their original claims.

## Endpoints

### Public

`POST /authenticate`, `POST /users/register`, `GET /categories`, `GET /products`, `GET /products/{id}`, and `/h2-console/**`.

`GET /products` accepts optional `categoryId` and case-insensitive `search`. Only active products are public; inactive products return 404.

### USER or ADMIN

`GET /users`, `PUT /users`, `DELETE /users`; `GET /cart`, `POST /cart`, `PUT /cart/{productId}`, `DELETE /cart/{productId}`, `DELETE /cart`; `GET /favorites`, `POST /favorites/{productId}`, `DELETE /favorites/{productId}`; `POST /orders`, `GET /orders`, and `GET /orders/{id}`.

Checkout accepts only a required shipping address from the client. Prices, totals, and stock changes are server-side. Checkout is transactional and rejects inactive products or insufficient stock with a full rollback.

### ADMIN only

`GET /admin/all-users`, `DELETE /admin/delete-user/{username}`, `PUT /admin/users/{username}/role`, `GET/POST /admin/products`, `PUT /admin/products/{id}`, `DELETE /admin/products/{id}`, `PUT /admin/products/{id}/restore`, `GET /admin/orders`, `GET /admin/orders/{id}`, and `PUT /admin/orders/{id}/status`.

Products are never hard-deleted through the admin API. Cancellation restores each order item's stock. Admins cannot demote or delete themselves.

## Tests and verification status

The server has Spring context and checkout/security integration coverage, including successful checkout, insufficient-stock rollback, user-order isolation, role protection, soft-deleted product behavior, cancellation stock restoration, and admin self-protection. The expected verification command is `server/.\mvnw.cmd clean verify`.

The client verification commands are `npm run lint` and `npm run build` from `client/`. The application is designed to run on port 9000 (server) and port 3000 (client).

The verified concurrency result for the last available stock unit is: two users attempted checkout concurrently; exactly one order was created, the other request returned 400, and there was no oversell or partial data change.

## Known limitations

- Role changes and account deletion affect authorization only after the user's JWT expires or they log in again.
- H2 is in-memory locally; all local data resets when the server restarts.
- Order history shows live product names from the products table; prices are snapshotted in `order_items`.
- `products.image_url` exists but is unused in the client; category icons are used instead.
- Payment is simulated; no real payment provider is integrated.
- The local JWT secret and demo credentials are development values and must be replaced for deployment.

## Next step

Stage 11 is complete. Next steps are operational: configure production variables, deploy the Docker server to Render with Neon PostgreSQL, deploy the client to Netlify, and run a real production checkout smoke test.

Stage 11 — deployment: PostgreSQL via Spring profiles, the server on Render using Docker, and the client on Netlify.

## AI handoff

Preserve the lecturer-reference package structure, JDBC/data.sql persistence approach, and existing security matcher organization. Keep usernames derived from JWT claims and keep prices/totals server-side. New client product displays should reuse `ProductVisual`; do not reintroduce manufacturer image dependencies unless the product-image strategy is intentionally redesigned.

## Open questions

- Which PostgreSQL schema/profile values, Render service settings, and Netlify environment variables should be finalized for deployment?
- Should historical order items eventually snapshot product names as well as prices?
- Should JWT refresh/revocation be added so role changes and account deletion can invalidate existing tokens immediately?
