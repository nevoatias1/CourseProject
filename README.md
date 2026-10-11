# Electronics Store

An online electronics and gadgets store backend, with a React client planned for a later stage. The backend is a Spring Boot REST API supporting JWT authentication, public product browsing, user carts and favorites, and transactional order checkout.

## Repository structure

```text
CourseProject/
├── server/          # Spring Boot backend
├── client/           # React frontend (planned for Stage 5)
├── .gitignore
└── README.md
```

The `client/` directory is not created yet. The backend base package is `com.example.store` and is organized into controllers, models, services, JDBC repositories, repository mappers, security, and utilities.

## Run the server

From `server/`:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on [http://localhost:9000](http://localhost:9000). The in-memory H2 database is initialized from `src/main/resources/data.sql` on startup.

The H2 console is available at [http://localhost:9000/h2-console](http://localhost:9000/h2-console).

Use these H2 connection values:

```text
JDBC URL: jdbc:h2:mem:mydb
User:     sa
Password: password
```

## API

Send JWT-protected requests with `Authorization: Bearer <token>`. Usernames are always taken from the authenticated JWT.

### Public endpoints

- `POST /authenticate` — authenticate with `{ "username", "password" }`; returns a JWT.
- `POST /users/register` — register a user. Registration always stores role `USER`.
- `GET /categories` — list all categories.
- `GET /products` — list products with category names. Optional query parameters: `categoryId`, `search` (case-insensitive product-name search).
- `GET /products/{id}` — get one product with its category name; returns 404 when missing.
- `GET /h2-console/**` — H2 database console resources.

### Authenticated USER or ADMIN endpoints

- `GET /users` — get the current user.
- `PUT /users` — update the current user profile.
- `DELETE /users` — delete the current user.
- `GET /cart` — get the current user's cart, line totals, and cart total.
- `POST /cart` — add `{ "productId", "quantity" }`; existing items increase quantity.
- `PUT /cart/{productId}` — set `{ "quantity" }` for a cart item.
- `DELETE /cart/{productId}` — remove one cart item.
- `DELETE /cart` — clear the current user's cart.
- `GET /favorites` — list the current user's favorite products.
- `POST /favorites/{productId}` — add a favorite (idempotent).
- `DELETE /favorites/{productId}` — remove a favorite.
- `POST /orders` — checkout with required `{ "shippingAddress" }`; creates a `PLACED` order and clears the cart transactionally.
- `GET /orders` — list the current user's orders, newest first.
- `GET /orders/{id}` — get one of the current user's orders with product names, quantities, unit prices, and line totals.

### ADMIN-only endpoints

- `GET /admin/all-users` — list all users.
- `DELETE /admin/delete-user/{username}` — delete another user.

The security configuration is stateless, uses JWT filtering, permits the public routes in its existing `permitAll` matcher, and protects user/cart/favorites/order routes in its existing `hasAnyAuthority("USER", "ADMIN")` matcher. CORS is configured for `http://localhost:3000`.

## Build and test

```powershell
.\mvnw.cmd clean verify
```

The project currently builds successfully, including the checkout integration tests.
