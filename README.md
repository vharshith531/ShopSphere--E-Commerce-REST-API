# ShopSphere (E-Cart)

A Spring Boot REST API for an e-commerce catalog, product reviews, customer
accounts, and customer-owned orders.

## Technology

- Java 25 (latest LTS) and Spring Boot 4.1.2
- Maven
- Spring MVC, Spring Data JPA, Bean Validation, and Spring Security JWT
- MySQL 8.4.13 for the Docker Compose environment
- `dotenv-java` for a local `.env` file during direct application runs
- Spring Boot Actuator for application health

The source is under `src/main/java/com/harshith/E_Cart`, organized into
controllers, services, entities, DTOs, repositories, configuration, and
exception handling. Static catalog images are stored in
`src/main/resources/static/product_images/` and are packaged into the
executable JAR.

## Requirements

- JDK 25
- Docker Desktop with Docker Engine and Docker Compose (already available in
  the development environment)
- A MySQL database for running the application directly from IntelliJ or Maven

## Configure environment variables

In PowerShell, copy the template and replace every placeholder in `.env` with
private values:

```powershell
Copy-Item .env.example .env
```

Use a unique MySQL application password, a different root password, and a
random `JWT_SECRET` of at least 32 UTF-8 bytes. Never commit or share `.env`.
Keep `SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD` aligned with
`MYSQL_USER`/`MYSQL_PASSWORD`; Compose uses the latter pair to initialize
MySQL and configure the application connection.

The example datasource URL is for MySQL on the local machine. When running
directly from IntelliJ or Maven, use `localhost` and ensure the database, user,
and password exist in that local MySQL instance. The Compose configuration
supplies `jdbc:mysql://mysql:3306/e-cart` to the application container; `mysql`
is the Compose service hostname, not `localhost`.

Environment variables supplied by the operating system, IDE, or command line
take precedence over values loaded from `.env`.

## Run locally with Maven

After configuring `.env` and ensuring local MySQL is available:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Direct deployments default to Hibernate `validate`, which checks the database
schema without changing it. The sample local `.env` and Compose setup select
the existing non-destructive `update` behavior for local development and a
fresh local MySQL database. For production, apply a reviewed database migration
and keep `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`. This repository does not yet
include versioned database migrations; confirm the target schema before
deploying.

## Run with Docker Compose

1. Copy `.env.example` to `.env` and set real local credentials and a random
   JWT secret. For the bundled MySQL service, `MYSQL_USER` should be a
   non-root application user.
2. Validate the Compose file and build the application image:

   ```powershell
   docker compose config --quiet
   docker compose build
   ```

3. Start the services and inspect their health:

   ```powershell
   docker compose up -d
   docker compose ps
   docker compose logs --tail=100 app
   docker compose logs --tail=100 mysql
   ```

The API is available at `http://localhost:8080` (or `APP_PORT` from `.env`).
MySQL is only reachable on the private Compose network and is not published to
the host. Its data is kept in the named `mysql_data` volume. Do not use
`docker compose down -v` for routine shutdown; that removes the database
volume. Use `docker compose down` to stop and remove the containers while
retaining database data, or `docker compose restart` to restart them.

## Health and API checks

- Health: `GET /actuator/health` (only the health actuator endpoint is exposed;
  details are not returned).
- Public catalog: `GET /api/products?page=0&size=5`
- Product by ID: `GET /api/products/{id}`
- Catalog filters: `/api/products/category`, `/price`, `/keyword`, `/rating`,
  and `/search`
- Product reviews: `GET /api/products/{productId}/reviews`;
  `POST /api/products/{productId}/reviews` requires a bearer token.
- Register/login: `POST /api/auth/register` and `POST /api/auth/login`.
- Orders: `POST /api/orders` and `GET /api/orders/{orderId}` require a bearer
  token; customers can retrieve only their own orders.

Product images are public static resources. For example:

```text
GET http://localhost:8080/product_images/apple-iphone-16.png
```

Register or log in to obtain a bearer token for protected calls. Send it as
`Authorization: Bearer <access-token>`. The product catalog and image URLs
remain public.

### Postman examples

1. Register with `POST /api/auth/register` using JSON, for example:

   ```json
   {"email":"buyer@example.com","password":"a-long-private-password"}
   ```

2. Log in with `POST /api/auth/login` using the same JSON shape. Copy the
   returned access token into Postman's **Bearer Token** authorization setting.
3. Submit an authenticated review with
   `POST /api/products/1/reviews` and JSON:

   ```json
   {"rating":5,"comment":"Excellent product"}
   ```

4. Submit an authenticated order with `POST /api/orders` and JSON:

   ```json
   {"items":[{"productId":1,"quantity":2}]}
   ```

   Prices and totals are calculated by the server. Use a product ID that
   exists in the catalog.

## Database persistence and backups

The Compose MySQL service uses the named `mysql_data` volume, which survives
container restarts and ordinary container recreation. Back up the database
regularly using `mysqldump` or your managed database provider's backup feature.
Store backups outside the container and protect them as sensitive data. Test
restores separately before relying on a backup.

## Publish and deploy

Build and tag an image for a registry you control (replace the example registry
and namespace with yours):

```powershell
docker build -t your-registry/your-namespace/e-cart:1.0 .
docker push your-registry/your-namespace/e-cart:1.0
```

Configure the cloud service with the published image, a managed MySQL
connection, persistent managed database storage, and the required environment
variables (`SERVER_PORT`, `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, and
`JWT_EXPIRATION_SECONDS`). Keep secrets in the hosting provider's secret
manager, not in the image or repository. Set the database hostname to the
managed database endpoint, not `mysql`. Configure the provider's health check
to use `/actuator/health`.

Before deploying to production, apply a reviewed schema migration, set
`SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, confirm TLS and network access to the
managed database, and configure automated backups. No cloud image has been
published and no cloud service has been provisioned by this project.

## Troubleshooting

- **MySQL access denied:** verify the username and password in `.env`; Compose
  uses the non-root `MYSQL_USER` account for the application.
- **JWT startup error:** provide a `JWT_SECRET` of at least 32 UTF-8 bytes.
- **Compose reports a missing variable:** replace all placeholders in `.env`.
- **Port 8080 is already in use:** set a different `APP_PORT`, then recreate
  the app container with `docker compose up -d`.
- **Database schema validation fails:** apply the intended migration and
  verify the schema before using `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`.
- **Check service state and logs:** use `docker compose ps` and
  `docker compose logs --tail=100 app` or `mysql`.
