# Linkly — URL Shortener

Turn long URLs into clean, shareable short links with **Linkly**. This full-stack URL shortener provides a responsive web interface for creating short links, managing your links, and reviewing click analytics.

<p align="center">
  <a href="https://urlshortner-1-utkg.onrender.com/"><strong>🚀 Live Demo</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/akyd21/urlshortner"><strong>Source Code</strong></a>
</p>

## Screenshots

### Linkly homepage

<img width="947" height="437" alt="image" src="https://github.com/user-attachments/assets/039cddf2-4e4b-4e6b-a42e-db7f8f278391" />

## DataBase image
<img width="598" height="371" alt="image" src="https://github.com/user-attachments/assets/fe07be2a-e9f7-4aee-9d3c-bf4df96b366b" />

## Application workflow 
<img width="665" height="431" alt="image" src="https://github.com/user-attachments/assets/bdaeb22e-645c-4d01-931a-d430421f60c7" />


## Features

- **URL shortening:** Convert long URLs into compact, shareable links.
- **Custom aliases:** Create memorable short-link names where supported.
- **Bulk shortening:** Submit multiple URLs, one per line.
- **User authentication:** Register/sign in and manage links associated with your account.
- **Link dashboard:** View created links and their click counts.
- **Click analytics:** Open analytics for individual short links.
- **Expiry support:** Configure link expiry where enabled.
- **Redirect handling:** Visiting a short link redirects to its original destination.
- **Responsive dark UI:** A modern interface with gradient accents.

> Feature availability can depend on the current deployment configuration.

## Tech Stack

| Layer | Technologies |
|---|---|
| Frontend | React, JavaScript, Vite, CSS |
| Backend | Java 21, Spring Boot, Spring Web, Spring Data JPA, Spring Security |
| Authentication | JWT (JSON Web Tokens) |
| Database | MySQL |
| Validation | Spring Boot Validation |
| Build tools | Maven Wrapper, npm |
| Deployment | Render |

## Architecture

```text
User / Browser
      │
      ▼
React + Vite Frontend
      │  HTTP / REST API
      ▼
Spring Boot Backend
      ├── Authentication & JWT security
      ├── URL creation and short-code generation
      ├── Redirect handling
      ├── Click tracking / link analytics
      │
      ▼
MySQL Database
      ├── Users
      └── Short URLs and link metadata
```

## Live Deployment

- **Application:** https://urlshortner-1-utkg.onrender.com/
- **GitHub repository:** https://github.com/akyd21/urlshortner

The frontend and backend may be deployed as separate services. Ensure the frontend's API base URL points to the deployed backend and the backend's CORS configuration permits the frontend origin.

## Run Locally

### Prerequisites

- Java 21 or later
- Node.js and npm
- MySQL 8 or a compatible MySQL service
- Git

### 1. Clone the repository

```bash
git clone https://github.com/akyd21/urlshortner.git
cd urlshortner
```

### 2. Configure MySQL

Create a database in MySQL:

```sql
CREATE DATABASE urlshortener;
```

### 3. Configure backend environment variables

The backend reads its database and JWT configuration from environment variables (a local `backend-url-shortener/.env` file is supported). Set these values in your environment or local `.env` file:

```properties
DB_URL=jdbc:mysql://localhost:3306/urlshortener
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
JWT_SECRET=replace_with_a_secure_secret_at_least_32_characters_long
JWT_EXPIRATION_MS=86400000
SERVER_PORT=8081
```

Use your own credentials and a strong secret. **Do not commit real passwords, production tokens, or secrets to Git.**

### 4. Start the backend

Windows PowerShell:

```powershell
cd backend-url-shortener
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
cd backend-url-shortener
chmod +x mvnw
./mvnw spring-boot:run
```

The backend's default port is `8081`, unless overridden by `SERVER_PORT`.

### 5. Start the frontend

Open a second terminal from the repository root:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite in your terminal. Check the frontend configuration and set the API base URL to the backend address you are running locally.

## API and Short-Link Flow

At a high level, the application flow is:

1. A user submits a destination URL through the frontend.
2. The frontend sends the request to the Spring Boot API.
3. The backend validates the input and generates a unique short code.
4. The short code and destination URL are stored in MySQL.
5. When someone opens the short link, the backend looks up the original URL, records the click where tracking is enabled, and redirects the visitor.
6. Authenticated users can review their links and available analytics from the dashboard.

For exact endpoint paths and request payloads, see the REST controller in `backend-url-shortener/src/main/java/com/urlshortener/controller/UrlShortenerRestController.java` and the authentication controller in `backend-url-shortener/src/main/java/com/urlshortener/controller/AuthController.java`.

## Build and Test

Run backend tests:

```bash
cd backend-url-shortener
./mvnw test
```

Build the backend:

```bash
./mvnw clean package
```

Build the frontend:

```bash
cd frontend
npm run build
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Project Structure

```text
urlshortner/
├── backend-url-shortener/
│   ├── src/main/java/com/urlshortener/
│   │   ├── config/        # Web configuration
│   │   ├── controller/    # REST and authentication controllers
│   │   ├── dto/           # Request/response objects
│   │   ├── entity/        # JPA entities
│   │   ├── repository/    # Database access
│   │   ├── security/      # JWT and security configuration
│   │   ├── service/       # Business logic
│   │   └── util/          # Short-code utilities
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── pom.xml
└── frontend/
    ├── public/
    └── src/
        ├── components/
        ├── styles/
        ├── App.jsx
        └── main.jsx
```

## Future Improvements

- Add richer analytics, such as click trends over time and referrer summaries.
- Add rate limiting and abuse protection for link creation.
- Add automated integration tests for redirects, authentication, and analytics.
- Improve deployment observability with structured logs and health checks.

## Author

**Akash Raj**

- GitHub: [@akyd21](https://github.com/akyd21)
- Project repository: [akyd21/urlshortner](https://github.com/akyd21/urlshortner)
- Live application: [Linkly on Render](https://urlshortner-1-utkg.onrender.com/)

---

If you find this project useful, consider giving the repository a ⭐ on GitHub.
