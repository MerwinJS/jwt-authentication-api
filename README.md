# JWT Authentication API

A Spring Boot REST API that provides secure authentication and authorization using JSON Web Tokens (JWT). This application implements a complete authentication system with user registration, login, token refresh, and logout capabilities.

## Features

- **User Registration** - Create new user accounts with validation
- **User Login** - Authenticate users and generate JWT tokens
- **Token Refresh** - Refresh expired access tokens using refresh tokens
- **User Logout** - Invalidate refresh tokens on logout
- **Role-Based Access Control** - Support for different user roles
- **JWT-Based Security** - Secure token-based authentication
- **Database Persistence** - MySQL database for user and token storage
- **API Documentation** - OpenAPI/Swagger documentation
- **Exception Handling** - Global exception handling with meaningful error messages
- **Validation** - Request payload validation

## Tech Stack

- **Java 21** - Programming language
- **Spring Boot 3.5.4** - Framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **JJWT 0.12.6** - JWT token generation and validation
- **MySQL** - Primary database
- **H2 Database** - In-memory database for development/testing
- **Lombok** - Reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation
- **Gradle** - Build tool

## Prerequisites

Before running the application, ensure you have:

- **Java 21** or later installed
- **MySQL Server** running locally (or configure the datasource URL)
- **Gradle** (included via gradlew wrapper)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd jwt-authentication-api
```

### 2. Database Configuration

Update `src/main/resources/application.yml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_service?useSSL=false
    username: root
    password: root123  # Change to your password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**Or create a MySQL database:**

```sql
CREATE DATABASE auth_service;
```

### 3. Build the Application

```bash
./gradlew build
```

On Windows:
```powershell
.\gradlew.bat build
```

### 4. Run the Application

```bash
./gradlew bootRun
```

On Windows:
```powershell
.\gradlew.bat bootRun
```

The application will start on `http://localhost:8080`

## Configuration

### JWT Configuration

Configure JWT settings in `application.yml`:

```yaml
jwt:
  secret: SGVsbG9Xb3JsZFNlY3JldEtleU9mTWluZTI1NkJpdHNMb25nIQ==  # Change to your secret
  access-token-expiration: 900000    # 15 minutes in milliseconds
  refresh-token-expiration: 604800000  # 7 days in milliseconds
```

**Security Note:** Change the JWT secret to a strong, random value for production environments.

### Database Profiles

- **Development**: Uses H2 in-memory database (activate with `spring.profiles.active=h2`)
- **Production**: Uses MySQL database (default)

## API Endpoints

### Authentication Endpoints (`/auth`)

#### 1. Register User
```http
POST /auth/register
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "accessTokenExpiredInMs": 900000
}
```

#### 2. Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "accessTokenExpiredInMs": 900000
}
```

#### 3. Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "accessTokenExpiredInMs": 900000
}
```

#### 4. Logout
```http
POST /auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** 204 No Content

## Using the API

### Authentication Header

Include the access token in the `Authorization` header for protected endpoints:

```http
GET /users/info
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

```http
GET /users/admin
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Project Structure

```
src/
├── main/
│   ├── java/com/mejs/jwtapp/
│   │   ├── JwtAuthenticationApplication.java    # Main Spring Boot application
│   │   ├── auth/
│   │   │   ├── AuthController.java              # Authentication endpoints
│   │   │   ├── AuthService.java                 # Authentication business logic
│   │   │   └── dto/                             # Data transfer objects
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java      # Global exception handling
│   │   ├── security/
│   │   │   ├── SecurityConfig.java              # Spring Security configuration
│   │   │   ├── JwtAuthenticationFilter.java     # JWT validation filter
│   │   │   ├── JwtService.java                  # JWT token operations
│   │   │   ├── JwtProperties.java               # JWT configuration properties
│   │   │   ├── AppUserDetailsService.java       # User details service
│   │   │   ├── JwtAuthenticationEntryPoint.java # JWT entry point
│   │   │   └── JwtAccessDeniedHandler.java      # Access denied handler
│   │   ├── user/
│   │   │   ├── AppUser.java                     # User entity
│   │   │   ├── Role.java                        # User role entity
│   │   │   ├── UserRepository.java              # User data repository
│   │   │   ├── UserController.java              # User management endpoints
│   │   │   └── AdminBootstrapRunner.java        # Initialize admin user
│   │   └── token/
│   │       └── RefreshToken.java                # Refresh token entity
│   └── resources/
│       └── application.yml                      # Application configuration
└── test/
    └── java/com/mejs/jwtapp/                    # Test classes
```

## Authentication Flow

1. **User Registration**: User submits registration details → System creates new user → Returns access and refresh tokens
2. **User Login**: User submits credentials → System validates → Returns access and refresh tokens
3. **Access Protected Resources**: Client includes access token in Authorization header → Filter validates token → Request processed
4. **Token Refresh**: Client submits refresh token → System validates and generates new access token
5. **Logout**: Client submits refresh token → System invalidates the token

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - Authenticated but lacks permission
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Testing

Run the test suite:

```bash
./gradlew test
```

On Windows:
```powershell
.\gradlew.bat test
```

Test reports are generated in `build/reports/tests/test/`

## API Documentation

Access the interactive API documentation (Swagger UI):

```
http://localhost:8080/swagger-ui.html
```

## Running with Different Profiles

### Development (using H2 database)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Production (using MySQL)

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Security Considerations

⚠️ **Important for Production:**

1. **Change JWT Secret** - Update `jwt.secret` in configuration with a strong random value
2. **Use HTTPS** - Always use HTTPS in production
3. **Secure Credentials** - Never commit database passwords or secrets to version control
4. **Use Environment Variables** - Store sensitive config in environment variables
5. **Token Expiration** - Adjust token expiration times based on your security requirements
6. **CORS Configuration** - Configure CORS properly for your frontend domain

## Troubleshooting

### MySQL Connection Issues

Ensure MySQL is running and accessible:

```bash
# Test connection
mysql -u root -p -h localhost -D auth_service
```

### Port Already in Use

If port 8080 is already in use, change it in `application.yml`:

```yaml
server:
  port: 8081
```

### Database Not Created

The application automatically creates tables (ddl-auto: update), but you need to create the database:

```sql
CREATE DATABASE IF NOT EXISTS auth_service;
```

## Development

### Building for Production

```bash
./gradlew clean build -x test
```

This creates a JAR file in `build/libs/`

### Running the JAR

```bash
java -jar build/libs/jwt-authentication-api-0.0.1-SNAPSHOT.jar
```

## Support

For issues or questions, please refer to the Spring Security and JJWT documentation:

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JJWT GitHub Repository](https://github.com/jwtk/jjwt)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
