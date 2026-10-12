# Atias Express

Atias Express is a full-stack online electronics and gadgets store. Guests can browse the catalog and search or filter products. Registered customers can manage their profile, favorites, cart, checkout, and order history. Administrators can manage products, inventory, order statuses, and users through an admin dashboard.

## Features

- Guest catalog browsing with category filters, search, sorting, product details, and responsive Bootstrap UI.
- JWT sign-in and registration with `USER` and `ADMIN` roles.
- Customer profile updates, password changes, account deletion, favorites, cart management, and transactional checkout.
- Simulated payment flow; card details remain client-side and are never sent to the server.
- Admin product creation/editing, soft delete/restore, order review/status changes, stock restoration on cancellation, and user role management.
- Category-based product visuals using Bootstrap Icons; product image URLs are retained in the schema but not displayed.

## Technology and structure

Server: Java 21, Spring Boot 3.3.4, Spring Security, Spring JDBC, H2, JJWT 0.9.1, Maven Wrapper. Persistence uses JDBC repositories, mappers, and `data.sql`; the application does not use JPA repositories or entities.

Client: React 18.3.1, Vite 5.4.10, React Router 6.28.0, React-Bootstrap 2.10.5, Bootstrap 5.3.3, Bootstrap Icons, and `fetch` (no Redux, Context API, or Axios).

```text
CourseProject/
├── server/                         Spring Boot REST API
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/store/
│       │   ├── controller/          HTTP endpoints
│       │   ├── model/               Request and response models
│       │   ├── repository/          JDBC repositories and mapper/
│       │   ├── security/            JWT filter and security configuration
│       │   ├── service/             Business and transaction logic
│       │   └── utils/               JWT utility classes
│       └── main/resources/
│           ├── application.yaml
│           └── data.sql
├── client/                         React/Vite application
│   ├── src/api.js                   All client API calls
│   ├── src/components/              Navbar, footer, ProductVisual, favorites
│   ├── src/pages/                   Catalog, auth, account, cart, checkout, admin
│   └── src/styles/                  Central theme
├── .gitignore
├── README.md
└── PROJECT_STATE.md
```

## Run locally

Prerequisites: Java 21, Node.js/npm, and a PowerShell terminal on Windows.

1. Start the server:

   ```powershell
   cd server
   .\mvnw.cmd spring-boot:run
   ```

   The API runs at `http://localhost:9000`. The local database is an in-memory H2 database initialized from `server/src/main/resources/data.sql`, so its data resets whenever the server restarts.

2. In a second terminal, start the client:

   ```powershell
   cd client
   npm install
   npm run dev
   ```

   Vite runs at `http://localhost:3000`, the origin allowed by the server CORS configuration. The client defaults to `http://localhost:9000`; set `VITE_API_URL` if the API uses another URL.

3. Optional H2 console: open `http://localhost:9000/h2-console` and use JDBC URL `jdbc:h2:mem:mydb`, user `sa`, password `password`.

Demo admin account seeded locally:

- `nevoatias1` / `nevo1234` (Nevo Atias, `ADMIN`)

To create a regular customer, use **Sign up** in the client or `POST /users/register`. Registration always stores the new account as `USER`, regardless of any role value supplied by a client.

## API

Protected requests use `Authorization: Bearer <jwt>`. Usernames for user-scoped operations always come from the authenticated JWT, never from a URL, query parameter, or request body. Product prices, cart totals, order totals, and checkout stock changes are computed or validated by the server.

### Public

- `POST /authenticate` — authenticate with `{ "username", "password" }`; returns a JWT.
- `POST /users/register` — register a user; role is always `USER`.
- `GET /categories` — list categories.
- `GET /products` — list active products with category names. Optional query parameters: `categoryId`, `search` (case-insensitive product-name search).
- `GET /products/{id}` — get one active product with its category name; inactive or missing products return 404.
- `GET /h2-console/**` — H2 console resources.

### USER or ADMIN

- `GET /users` — current user profile.
- `PUT /users` — update current profile fields; username and role cannot be changed; a blank password keeps the existing password.
- `DELETE /users` — delete the current account.
- `GET /cart` — current cart with product details, line totals, and cart total.
- `POST /cart` — add `{ "productId", "quantity" }` or increase an existing item.
- `PUT /cart/{productId}` — set `{ "quantity" }`.
- `DELETE /cart/{productId}` — remove an item.
- `DELETE /cart` — clear the cart.
- `GET /favorites` — current user's favorite products.
- `POST /favorites/{productId}` — add a favorite (idempotent).
- `DELETE /favorites/{productId}` — remove a favorite.
- `POST /orders` — checkout with required `{ "shippingAddress" }`; creates a `PLACED` order transactionally.
- `GET /orders` — current user's orders, newest first.
- `GET /orders/{id}` — current user's order details and items.

### ADMIN only

- `GET /admin/all-users` — list users without password data.
- `DELETE /admin/delete-user/{username}` — delete another user; an admin cannot delete themselves.
- `PUT /admin/users/{username}/role` — set role to `USER` or `ADMIN`; an admin cannot change their own role.
- `GET /admin/products` — list all products, including inactive products.
- `POST /admin/products` — create a product; validates name, positive price, non-negative stock, and category.
- `PUT /admin/products/{id}` — update a product with the same validation.
- `DELETE /admin/products/{id}` — soft-delete/deactivate a product and remove it from carts and favorites.
- `PUT /admin/products/{id}/restore` — restore a soft-deleted product.
- `GET /admin/orders` — list all orders, newest first, including usernames.
- `GET /admin/orders/{id}` — view any order and its items.
- `PUT /admin/orders/{id}/status` — valid transitions are `PLACED → SHIPPED → DELIVERED` and `PLACED → CANCELLED`; cancellation restores stock transactionally.

## Security summary

Authentication is stateless JWT authentication through `JwtRequestFilter`, `CustomUserDetailsService`, and `JwtUtil`. `SecurityConfigure` permits the public routes, protects customer routes with `USER` or `ADMIN`, and protects `/admin/**` with `ADMIN`. CORS permits `http://localhost:3000`.

The username is the String primary key and JWT subject; there is no user ID. Passwords are BCrypt encoded and never logged. Registration ignores client roles. Product removal is a soft delete (`active = false`), so historical order items remain available while inactive products disappear from the public catalog.

## Verification

Server verification:

```powershell
cd server
.\mvnw.cmd clean verify
```

Client verification:

```powershell
cd client
npm run lint
npm run build
```