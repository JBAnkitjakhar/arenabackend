
# application-local.properties
// and i already add real secrets so dont worry

# MongoDB Atlas Configuration - Replace with your actual connection string
spring.data.mongodb.uri=mongodb+srv://USERNAME:PASSWORD@CLUSTER.mongodb.net/DATABASE_NAME?retryWrites=true&w=majority&appName=CLUSTER_NAME

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







.env file 
# MongoDB Atlas Configuration
MONGODB_URI= 

# Piston API (default is fine) i dont it , i hardcoded that 
# PISTON_API_URL=https://emkc.org/api/v2/piston

# OAuth2 - Google
GOOGLE_CLIENT_ID= 
GOOGLE_CLIENT_SECRET= 

# OAuth2 - GitHub
GITHUB_CLIENT_ID= 
GITHUB_CLIENT_SECRET= 

# JWT Configuration
JWT_SECRET= 

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME= 
CLOUDINARY_API_KEY= 
CLOUDINARY_API_SECRET= 

# CORS (for development)
ALLOWED_ORIGINS=http://localhost:3000
 






































 # src/main/resources/application-local.properties
# Local Development Configuration

# MongoDB Atlas Configuration
spring.data.mongodb.uri=

# OAuth2 Configuration - Replace with your actual values
spring.security.oauth2.client.registration.google.client-id= 
spring.security.oauth2.client.registration.google.client-secret= 

spring.security.oauth2.client.registration.github.client-id= 
spring.security.oauth2.client.registration.github.client-secret= 

# JWT Configuration
app.jwt.secret=

# Cloudinary Configuration - Replace with your actual values
app.cloudinary.cloud-name= 
app.cloudinary.api-key= 
app.cloudinary.api-secret=

# CORS for local development
app.cors.allowed-origins=http://localhost:3000

# Piston API
app.piston.api-url=https://emkc.org/api/v2/piston