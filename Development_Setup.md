# 🚀 AlgoArena Backend - Development Setup

## 📋 Prerequisites
- Java 21+
- Maven 3.6+
- MongoDB Atlas account
- Upstash Redis account (optional for caching)

## 🔧 Local Development Setup

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd algoarena-backend
```

### 2. Create Local Configuration File
```bash
# Copy the template file
cp src/main/resources/application-local.properties.template src/main/resources/application-local.properties
```

### 3. Fill in Your Configuration
Edit `src/main/resources/application-local.properties` with your actual values:

**Required:**
- `MONGODB_URI` - Your MongoDB Atlas connection string
- `JWT_SECRET` - Generate with: `openssl rand -base64 32`

**Optional (for full functionality):**
- `REDIS_URL` - Your Upstash Redis URL
- `GOOGLE_CLIENT_ID` & `GOOGLE_CLIENT_SECRET` - For Google OAuth
- `GITHUB_CLIENT_ID` & `GITHUB_CLIENT_SECRET` - For GitHub OAuth  
- `CLOUDINARY_*` - For image uploads

### 4. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 5. Verify Setup
```bash
# Health check
curl http://localhost:8080/api/actuator/health

# Should return: {"status":"UP"}
```

## 🔐 Security Notes

- ⚠️ **NEVER commit** `application-local.properties` - it contains secrets
- ✅ **Always use** the template file for sharing configuration structure
- 🔒 **Keep your secrets safe** - they're in `.gitignore`

## 🌐 Environment Profiles

- **Local Development:** `spring.profiles.active=local`
- **Production:** `spring.profiles.active=prod`

## 📚 API Documentation

Once running, visit:
- Health: `http://localhost:8080/api/actuator/health`
- Info: `http://localhost:8080/api/actuator/info`

## 🆘 Need Help?

1. Make sure all required environment variables are set
2. Check MongoDB Atlas network access (allow your IP)
3. Verify Java 21+ is installed: `java --version`
4. Check Maven version: `mvn --version`








# application-local.properties.template
# Copy this file to application-local.properties and fill in your actual values
# This template file is safe to commit - it contains no real secrets

# MongoDB Atlas Configuration - Replace with your actual connection string
spring.data.mongodb.uri=mongodb+srv://USERNAME:PASSWORD@CLUSTER.mongodb.net/DATABASE_NAME?retryWrites=true&w=majority&appName=CLUSTER_NAME

# Upstash Redis Configuration - Replace with your actual Redis URL  
spring.data.redis.url=rediss://default:YOUR_PASSWORD@YOUR_REDIS_HOST.upstash.io:6380

# OAuth2 Configuration - Replace with your actual OAuth credentials
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

spring.security.oauth2.client.registration.github.client-id=YOUR_GITHUB_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_GITHUB_CLIENT_SECRET

# JWT Configuration - Generate a secure secret (use: openssl rand -base64 32)
app.jwt.secret=YOUR_SECURE_JWT_SECRET_AT_LEAST_32_CHARACTERS_LONG

# Cloudinary Configuration - Replace with your actual Cloudinary credentials
app.cloudinary.cloud-name=YOUR_CLOUDINARY_CLOUD_NAME
app.cloudinary.api-key=YOUR_CLOUDINARY_API_KEY
app.cloudinary.api-secret=YOUR_CLOUDINARY_API_SECRET

# Piston API Configuration (Public API - no secrets needed)
app.piston.api-url=https://emkc.org/api/v2/piston

# CORS for local development
app.cors.allowed-origins=http://localhost:3000

# Redis timeout
spring.data.redis.timeout=2000ms