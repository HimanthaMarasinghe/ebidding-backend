# User Service Startup Checklist

## Before Starting the Application:

### 1. Check PostgreSQL Database
- ✅ PostgreSQL server is running
- ✅ Database `user_service_db` exists
- ✅ Username: `postgres`, Password: `himesh`

### 2. Check if Port 8083 is Free
- No other application using port 8083

### 3. Start the Application
- Right-click on `UserServiceApplication.java`
- Select "Run UserServiceApplication"

## Expected Startup Logs:
```
Starting UserServiceApplication...
CSRF disabled; All requests permitted for testing
Tomcat initialized with port(s): 8083 (http)
Started UserServiceApplication in X.XXX seconds
```

## Test After Startup:
1. First test: `GET http://localhost:8083/us/v1/hello`
2. Should return: `"Hello from User Service!"`

## If Application Fails to Start:

### Common Error Solutions:

**Database Connection Error:**
```
Check if PostgreSQL is running on localhost:5432
Verify database name: user_service_db
Verify credentials: postgres/himesh
```

**Port Already in Use:**
```
Change port in application.properties:
server.port=8084
Then use: http://localhost:8084/us/v1/hello
```

**Maven Dependencies:**
```
Try: mvn clean install
Or in IDE: Build > Rebuild Project
```
