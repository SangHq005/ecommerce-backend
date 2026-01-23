# Frontend-Backend Integration Guide

## Overview
This document provides a comprehensive guide for connecting the Next.js frontend with the Spring Boot backend for the e-commerce application.

## Architecture Overview

```
┌─────────────────────────────────────┐
│     Next.js Frontend (Port 3000)    │
│  ┌────────────────────────────────┐ │
│  │   Components & Pages           │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │   Services (API Calls)         │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │   Axios + API Client           │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
                    ↕ HTTP/HTTPS
┌─────────────────────────────────────┐
│   Spring Boot Backend (Port 8080)   │
│  ┌────────────────────────────────┐ │
│  │   REST Controllers              │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │   Security (JWT + OAuth2)       │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │   Services & Repositories       │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## Current Connection Status

### ✅ Working Components
1. **CORS Configuration**: Backend properly configured for localhost:3000
2. **API Client Setup**: Frontend has axios and API client with interceptors
3. **Authentication Flow**: JWT token management with refresh mechanism
4. **Response Format**: Standardized ApiResponse wrapper
5. **Error Handling**: Centralized error mapping and user feedback

### ⚠️ Issues Identified
1. **Missing Environment File**: Frontend lacks .env.local (now created)
2. **Hardcoded URLs**: Some services may have hardcoded localhost:8080
3. **Token Storage**: Using both localStorage and cookies (potential inconsistency)
4. **No API Documentation Link**: Swagger UI not linked in frontend

## Setup Instructions

### 1. Backend Setup

#### Start Backend Server
```bash
cd ecommerce-backend

# Install dependencies
./mvnw clean install

# Run with development profile
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Or with environment variables
export SERVER_PORT=8080
export JWT_SECRET_BASE64=<your-base64-encoded-secret>
export SMTP_USERNAME=<your-email>
export SMTP_PASSWORD=<your-email-password>
./mvnw spring-boot:run
```

#### Verify Backend
- Health Check: http://localhost:8080/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/v3/api-docs

### 2. Frontend Setup

#### Configure Environment
```bash
cd ../Meta-Shop-Web/Frontend

# Environment file already created at .env.local
# Update values if needed
```

#### Start Frontend Server
```bash
# Install dependencies
npm install

# Run development server
npm run dev
```

#### Verify Frontend
- Application: http://localhost:3000
- Check browser console for API connection errors

## API Integration Pattern

### Frontend Service Pattern
```typescript
// Example: services/[entity].service.ts
import api from "@/lib/axios";
import { apiCall, apiCallWithMeta } from "@/lib/api-client";

export const EntityService = {
  // GET request
  getAll: async (params?: any) => {
    return apiCallWithMeta<Entity[]>(
      api.get("/api/v1/entities", { params })
    );
  },

  // POST request
  create: async (data: CreateEntityDto) => {
    return apiCall<Entity>(
      api.post("/api/v1/entities", data)
    );
  },

  // PUT request
  update: async (id: number, data: UpdateEntityDto) => {
    return apiCall<Entity>(
      api.put(`/api/v1/entities/${id}`, data)
    );
  },

  // DELETE request
  delete: async (id: number) => {
    return apiCall<void>(
      api.delete(`/api/v1/entities/${id}`)
    );
  }
};
```

### Backend Controller Pattern
```java
@RestController
@RequestMapping("/api/v1/entities")
public class EntityController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<Entity>>> getAll(
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        // Implementation
        return ResponseHelper.ok(entities, meta, "Success");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Entity>> create(
        @Valid @RequestBody CreateEntityDto dto
    ) {
        // Implementation
        return ResponseHelper.created(entity, "Created");
    }
}
```

## Authentication Flow

### Login Process
1. User submits credentials to `/api/v1/auth/login`
2. Backend validates and returns JWT tokens
3. Frontend stores tokens:
   - Access token in localStorage and cookie
   - Refresh token in localStorage
4. Axios interceptor adds Bearer token to requests

### Token Refresh
1. When access token expires (401 error)
2. Axios interceptor catches error
3. Calls `/api/v1/auth/refresh` with refresh token
4. Updates stored tokens
5. Retries original request

### OAuth2 Flow
1. User clicks "Login with Google"
2. Redirects to `/oauth2/authorization/google`
3. Google OAuth process
4. Backend callback creates JWT tokens
5. Redirects to frontend with tokens

## Improvements Needed

### Frontend Improvements

1. **Environment Configuration**
   - ✅ Created .env.local file
   - ❌ Add .env.example for documentation
   - ❌ Add environment validation on startup

2. **API Error Handling**
   - ❌ Add retry logic for network failures
   - ❌ Implement offline detection
   - ❌ Add request/response logging in dev mode

3. **Authentication**
   - ❌ Fix token storage inconsistency (localStorage vs cookies)
   - ❌ Add token expiry countdown
   - ❌ Implement remember me functionality

4. **Type Safety**
   - ❌ Generate TypeScript types from OpenAPI spec
   - ❌ Add strict type checking for API responses
   - ❌ Create shared DTO interfaces

5. **Performance**
   - ❌ Implement request caching
   - ❌ Add request deduplication
   - ❌ Implement optimistic updates

6. **Developer Experience**
   - ❌ Add API mock server for testing
   - ❌ Create API client SDK
   - ❌ Add request/response interceptor logging

### Backend Improvements

1. **API Documentation**
   - ❌ Add more detailed Swagger annotations
   - ❌ Create API versioning strategy
   - ❌ Add example requests/responses

2. **Security**
   - ❌ Implement rate limiting per user
   - ❌ Add API key authentication option
   - ❌ Implement CSRF for stateful operations
   - ❌ Add request signing for sensitive operations

3. **Performance**
   - ❌ Add response compression
   - ❌ Implement caching headers
   - ❌ Add database query optimization
   - ❌ Implement pagination for all list endpoints

4. **Error Handling**
   - ❌ Standardize all error responses
   - ❌ Add more specific error codes
   - ❌ Implement error tracking (Sentry, etc.)

5. **Monitoring**
   - ❌ Add request logging
   - ❌ Implement metrics collection
   - ❌ Add health check endpoints for dependencies
   - ❌ Create admin dashboard for monitoring

6. **Data Validation**
   - ❌ Add more comprehensive input validation
   - ❌ Implement request sanitization
   - ❌ Add business rule validation layer

## Testing the Connection

### 1. Test Health Endpoint
```bash
# Backend health check
curl http://localhost:8080/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

### 2. Test Authentication
```bash
# Register new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123456",
    "fullName": "Test User"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123456"
  }'
```

### 3. Test Authenticated Request
```bash
# Get user profile (replace TOKEN with actual token)
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Common Issues & Solutions

### Issue 1: CORS Error
**Error**: `Access to XMLHttpRequest blocked by CORS policy`

**Solution**:
1. Check backend CORS configuration includes frontend URL
2. Ensure credentials are included in requests
3. Verify allowed methods and headers

### Issue 2: 401 Unauthorized
**Error**: All requests return 401

**Solution**:
1. Check token is being sent in Authorization header
2. Verify token is not expired
3. Ensure token format is correct: `Bearer <token>`

### Issue 3: Network Error
**Error**: `Network Error` or `ERR_CONNECTION_REFUSED`

**Solution**:
1. Verify backend is running on correct port
2. Check firewall settings
3. Ensure backend URL in frontend config is correct

### Issue 4: Invalid Response Format
**Error**: Frontend cannot parse backend response

**Solution**:
1. Check if endpoint returns ApiResponse format
2. Verify content-type is application/json
3. Check for response interceptor issues

## Development Workflow

### 1. API-First Development
1. Define API contract in Swagger/OpenAPI
2. Implement backend endpoint with tests
3. Generate TypeScript types from OpenAPI
4. Implement frontend service and UI

### 2. Local Development Setup
```bash
# Terminal 1: Backend
cd ecommerce-backend
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Terminal 2: Frontend
cd ../Meta-Shop-Web/Frontend
npm run dev

# Terminal 3: Database (if using Docker)
docker-compose up -d mysql
```

### 3. Testing Integration
1. Use Postman/Insomnia for API testing
2. Check browser DevTools for network requests
3. Use backend Swagger UI for quick tests
4. Implement integration tests

## Production Deployment

### Backend Deployment
1. Update CORS origins for production domain
2. Set secure JWT secret
3. Configure SSL/TLS
4. Set up reverse proxy (Nginx/Apache)
5. Configure environment variables

### Frontend Deployment
1. Update API URL for production
2. Build optimized production bundle
3. Configure CDN for static assets
4. Set up SSL certificate
5. Configure environment variables

### Security Checklist
- [ ] HTTPS enabled on both frontend and backend
- [ ] Secure JWT secret (min 256 bits)
- [ ] CORS restricted to specific domains
- [ ] Rate limiting enabled
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention
- [ ] XSS protection headers
- [ ] CSRF protection (if using cookies)

## Monitoring & Maintenance

### Key Metrics to Monitor
1. API response times
2. Error rates by endpoint
3. Authentication failures
4. Database query performance
5. Frontend bundle size
6. Client-side errors

### Maintenance Tasks
1. Regular dependency updates
2. Security patch application
3. Database optimization
4. Log rotation and cleanup
5. Performance profiling

## Resources

### Documentation
- [Next.js Documentation](https://nextjs.org/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Axios Documentation](https://axios-http.com/docs/intro)
- [JWT.io](https://jwt.io/)

### Tools
- [Postman](https://www.postman.com/) - API testing
- [Redux DevTools](https://github.com/reduxjs/redux-devtools) - State debugging
- [React Developer Tools](https://react.dev/learn/react-developer-tools) - Component debugging
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) - Monitoring

### Troubleshooting
- Check `/health` endpoint for backend status
- Review browser console for frontend errors
- Check network tab for API calls
- Review backend logs for detailed errors
- Use correlation IDs for request tracking