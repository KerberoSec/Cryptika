# HackSecure Relay Server - Dockerfile
# Build and run the blind relay server in Docker

# Use official Node.js LTS (Long Term Support) image
FROM node:20-alpine

# Set maintainer label
LABEL maintainer="HackSecure Messenger Team"
LABEL description="HackSecure Blind Relay Server - Routes encrypted messages without inspecting content"
LABEL version="1.0.0"

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
# A wildcard is used to ensure both package.json AND package-lock.json are copied
COPY package*.json ./

# Install dependencies
# Use --production flag in production to skip devDependencies
RUN npm ci --only=production

# Bundle app source
COPY . .

# Expose port 8443
EXPOSE 8443

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD node -e "require('http').get('http://localhost:8443/health', (r) => {process.exit(r.statusCode === 200 ? 0 : 1)})" || exit 1

# Set environment variables (can be overridden)
ENV NODE_ENV=production
ENV PORT=8443

# Run the server
CMD ["node", "index.js"]
