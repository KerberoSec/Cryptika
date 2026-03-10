# HackSecure Relay Server - Docker Deployment Guide

This guide explains how to run the HackSecure relay server using Docker.

---

## Quick Start

### Prerequisites

- **Docker** installed ([Download Docker](https://www.docker.com/get-started))
- **Docker Compose** (included with Docker Desktop)

---

## Option 1: Using Docker Compose (Recommended)

### 1. Start the Server

From the **project root directory** (HackSecureMessenger):

```bash
docker-compose up -d
```

**What this does:**
- Builds the Docker image from `server/Dockerfile`
- Creates a container named `hacksecure-relay-server`
- Starts the server on port `8443`
- Runs in detached mode (background)

### 2. View Server Logs

```bash
# View all logs
docker-compose logs

# Follow logs in real-time
docker-compose logs -f

# View last 50 lines
docker-compose logs --tail=50
```

**Find the Server Public Key in the logs!**

```bash
docker-compose logs | grep "SERVER_PUBLIC_KEY_HEX"
```

### 3. Check Server Status

```bash
# Check if container is running
docker-compose ps

# Test server health
curl http://localhost:8443/api/v1/health
```

**Expected Output:**
```json
{"status":"ok","version":"1.0.0"}
```

### 4. Stop the Server

```bash
# Stop and remove containers
docker-compose down

# Stop, remove containers, and delete volumes
docker-compose down -v
```

---

## Option 2: Using Docker CLI

### 1. Build the Image

```bash
cd server
docker build -t hacksecure-relay-server:latest .
```

### 2. Run the Container

```bash
docker run -d \
  --name hacksecure-relay \
  -p 8443:8443 \
  --restart unless-stopped \
  hacksecure-relay-server:latest
```

### 3. View Logs

```bash
# View all logs
docker logs hacksecure-relay

# Follow logs in real-time
docker logs -f hacksecure-relay

# Extract server public key
docker logs hacksecure-relay | grep "SERVER_PUBLIC_KEY_HEX"
```

### 4. Stop and Remove Container

```bash
# Stop container
docker stop hacksecure-relay

# Remove container
docker rm hacksecure-relay
```

---

## Persistent Server Keys

By default, a new keypair is generated each time the container starts. To persist the server keypair across container restarts:

### Method 1: Environment Variable

1. **Get the private key** from first run:
   ```bash
   docker-compose logs | grep "Set SERVER_PRIVATE_KEY_HEX"
   ```

2. **Edit `docker-compose.yml`** and add:
   ```yaml
   environment:
     - SERVER_PRIVATE_KEY_HEX=your_64_char_private_key_here
   ```

3. **Restart:**
   ```bash
   docker-compose down
   docker-compose up -d
   ```

### Method 2: Volume Mount

The server can persist keys to a file if you mount a volume:

```yaml
volumes:
  - ./server-keys:/usr/src/app/data
```

---

## Network Configuration

### For Android Emulator

When running Docker on the **same machine** as the Android emulator:

**Windows/Mac (Docker Desktop):**
```
RELAY_BASE_URL: ws://host.docker.internal:8443
API_BASE_URL: http://host.docker.internal:8443
```

**Linux:**
```
RELAY_BASE_URL: ws://172.17.0.1:8443
API_BASE_URL: http://172.17.0.1:8443
```

Or use `10.0.2.2:8443` from the emulator (same as before if Docker exposes port to host).

### For Physical Devices

Use your **host machine's LAN IP**:

```bash
# Windows
ipconfig | findstr IPv4

# Mac/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1
```

Then configure:
```
RELAY_BASE_URL: ws://192.168.1.X:8443
API_BASE_URL: http://192.168.1.X:8443
```

---

## Docker Commands Reference

### Container Management

```bash
# Start container
docker-compose up -d

# Stop container
docker-compose stop

# Restart container
docker-compose restart

# Remove container
docker-compose down

# View container status
docker-compose ps

# View resource usage
docker stats hacksecure-relay-server
```

### Logs

```bash
# View logs
docker-compose logs

# Follow logs
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100

# Logs with timestamps
docker-compose logs --timestamps

# Save logs to file
docker-compose logs > server_logs.txt
```

### Debugging

```bash
# Execute command in running container
docker-compose exec hacksecure-server sh

# View container details
docker inspect hacksecure-relay-server

# View container processes
docker-compose top
```

### Images

```bash
# List images
docker images

# Remove image
docker rmi hacksecure-relay-server:latest

# Rebuild image
docker-compose build --no-cache
```

---

## Production Deployment

### 1. Enable TLS/SSL

For production, use a reverse proxy like **Nginx** or **Caddy** with TLS:

**docker-compose.yml with Nginx:**

```yaml
version: '3.8'

services:
  hacksecure-server:
    build: ./server
    container_name: hacksecure-relay
    networks:
      - backend
    # Don't expose port directly, only through nginx

  nginx:
    image: nginx:alpine
    ports:
      - "443:443"
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./certs:/etc/nginx/certs:ro
    depends_on:
      - hacksecure-server
    networks:
      - backend

networks:
  backend:
    driver: bridge
```

### 2. Use Environment Variables

Create `.env` file:

```env
NODE_ENV=production
PORT=8443
SERVER_PRIVATE_KEY_HEX=your_key_here
```

Update `docker-compose.yml`:

```yaml
env_file:
  - .env
```

### 3. Resource Limits

Add resource limits to `docker-compose.yml`:

```yaml
deploy:
  resources:
    limits:
      cpus: '0.5'
      memory: 512M
    reservations:
      cpus: '0.25'
      memory: 256M
```

### 4. Health Monitoring

Monitor health status:

```bash
# Check health
docker inspect --format='{{.State.Health.Status}}' hacksecure-relay-server

# View health logs
docker inspect --format='{{json .State.Health}}' hacksecure-relay-server | jq
```

---

## Troubleshooting

### Container Won't Start

```bash
# View detailed logs
docker-compose logs

# Check container status
docker-compose ps

# Inspect container
docker inspect hacksecure-relay-server
```

### Port Already in Use

```bash
# Find process using port 8443
# Windows
netstat -ano | findstr :8443

# Mac/Linux
lsof -i :8443

# Kill the process or change port in docker-compose.yml
```

### Cannot Connect from Android App

1. **Check container is running:**
   ```bash
   docker-compose ps
   ```

2. **Test from host machine:**
   ```bash
   curl http://localhost:8443/api/v1/health
   ```

3. **Check firewall rules** (allow port 8443)

4. **Use correct IP:**
   - Emulator: `10.0.2.2:8443` (or `host.docker.internal:8443`)
   - Physical device: Host LAN IP

### Image Build Fails

```bash
# Clean build
docker-compose build --no-cache

# Check Dockerfile syntax
docker-compose config
```

---

## Complete Workflow

### First Time Setup

```bash
# 1. Navigate to project root
cd /path/to/HackSecureMessenger

# 2. Start server with docker-compose
docker-compose up -d

# 3. Get server public key
docker-compose logs | grep "SERVER_PUBLIC_KEY_HEX"

# 4. Copy the public key (64-character hex string)

# 5. Update app/build.gradle.kts with the key

# 6. Build and run Android app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### After Code Changes

```bash
# Rebuild Docker image
docker-compose build

# Restart container
docker-compose up -d

# View logs
docker-compose logs -f
```

### Cleanup Everything

```bash
# Stop and remove containers, networks, volumes
docker-compose down -v

# Remove images
docker rmi hacksecure-relay-server:latest

# Remove all unused Docker resources
docker system prune -a
```

---

## Comparison: Docker vs Local

| Feature | Docker | Local (node index.js) |
|---------|--------|----------------------|
| **Setup** | Requires Docker installed | Requires Node.js installed |
| **Isolation** | Runs in isolated container | Runs directly on host |
| **Portability** | Easy to deploy anywhere | System-dependent |
| **Resource Usage** | ~50-100 MB overhead | Minimal overhead |
| **Logs** | `docker-compose logs` | Console output |
| **Restart** | Automatic with `--restart` | Manual or use PM2 |
| **Multi-instance** | Easy with docker-compose | Manual port configuration |

---

## Advanced: Multi-Container Setup

For load balancing with multiple server instances:

```yaml
version: '3.8'

services:
  server1:
    build: ./server
    environment:
      - SERVER_PRIVATE_KEY_HEX=${SERVER_KEY_1}

  server2:
    build: ./server
    environment:
      - SERVER_PRIVATE_KEY_HEX=${SERVER_KEY_2}

  nginx:
    image: nginx:alpine
    ports:
      - "8443:8443"
    volumes:
      - ./nginx-lb.conf:/etc/nginx/nginx.conf
    depends_on:
      - server1
      - server2
```

---

## Support

For Docker-specific issues:
- Check logs: `docker-compose logs`
- Inspect container: `docker inspect hacksecure-relay-server`
- Test connectivity: `curl http://localhost:8443/api/v1/health`

For server issues, see main `COMMANDS.md` troubleshooting section.

---

**Author:** HackSecure Messenger Team
**Version:** 1.0.0
**Last Updated:** 2026-03-08
