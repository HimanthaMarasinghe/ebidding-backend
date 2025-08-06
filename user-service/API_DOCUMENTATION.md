# User Service API Documentation

## Overview
This document describes the GET endpoints implemented in the User Service for retrieving user details by ID and getting users by role.

## Endpoints

### 1. Get User by ID
- **URL**: `GET /us/v1/user/{id}`
- **Description**: Retrieves user details by user ID
- **Parameters**: 
  - `id` (path parameter): Integer - The user ID
- **Response**: 
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "primaryPhone": "+1234567890",
      "secondaryPhone": "+0987654321",
      "firstName": "John",
      "lastName": "Doe",
      "dateOfBirth": "1990-01-01",
      "role": "Bidder"
    },
    "message": "User retrieved successfully"
  }
  ```

### 2. Get Users by Role
- **URL**: `GET /us/v1/users/role/{role}`
- **Description**: Retrieves all users with the specified role (case insensitive)
- **Parameters**: 
  - `role` (path parameter): String - The user role (e.g., "Bidder", "auction_manager", "yard_manager")
- **Response**: 
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john@example.com",
        "primaryPhone": "+1234567890",
        "secondaryPhone": "+0987654321",
        "firstName": "John",
        "lastName": "Doe",
        "dateOfBirth": "1990-01-01",
        "role": "Bidder"
      }
    ],
    "message": "Users retrieved successfully"
  }
  ```

### 3. Get All Users
- **URL**: `GET /us/v1/users`
- **Description**: Retrieves all users in the system
- **Response**: Same format as "Get Users by Role" but includes all users

### 4. Health Check
- **URL**: `GET /us/v1/hello`
- **Description**: Simple health check endpoint
- **Response**: "Hello from User Service!"

## User Types and DTOs

The service supports different user types with specific additional fields:

### BidderDTO (extends UserProfileDTO)
- Additional fields:
  - `userImageUrl`: String
  - `nicImageUrl`: String

### AuctionManagerDTO (extends UserProfileDTO)
- Additional fields:
  - `auctionCenter`: String
  - `designation`: String

### YardManagerDTO (extends UserProfileDTO)
- Additional fields:
  - `yardName`: String
  - `licenseNumber`: String

## Error Responses
- **404 Not Found**: When a user with the specified ID doesn't exist
- **500 Internal Server Error**: When there's an error processing the request

## Testing Examples

### Using curl:

1. Get user by ID:
```bash
curl -X GET http://localhost:8083/us/v1/user/1
```

2. Get users by role:
```bash
curl -X GET http://localhost:8083/us/v1/users/role/Bidder
```

3. Get all users:
```bash
curl -X GET http://localhost:8083/us/v1/users
```

### Note:
- Replace `localhost:8083` with the actual host and port where your user service is running (default port is 8083)
- The service uses case-insensitive role matching, so "bidder", "Bidder", and "BIDDER" will all work
