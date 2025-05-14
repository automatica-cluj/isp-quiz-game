# ISP Quiz - Dockerized

## Build and Run with Docker

### 1. Build the JAR

```sh
./mvnw clean package
```

### 2. Build and Run with Docker Compose

```sh
docker compose up --build
```

The app will be available at [http://localhost:8888](http://localhost:8888).

## Deploy with HTTPS using Azure App Service

1. Push your code (with Dockerfile and docker-compose.yml) to GitHub.
2. In [Azure Portal](https://portal.azure.com), create a new **Web App** (Linux, Docker).
3. In the App Service, configure **Deployment Center** to connect to your GitHub repo.
4. Azure will build and deploy your app automatically.
5. Access your app securely at `https://<your-app-name>.azurewebsites.net` (HTTPS enabled by default).

**Tip:** You can add a custom domain and free SSL certificate in App Service settings.

## Alternative: Local HTTPS (for development)

For local HTTPS, consider using [ngrok](https://ngrok.com/) or [mkcert](https://github.com/FiloSottile/mkcert) to expose your app securely.

- Example with ngrok:
  ```sh
  ngrok http 8888
  ```

### Notes

- The leaderboard data is persisted in `leaderboard_data.txt` on your host.
- To rebuild after code changes, rerun `docker compose up --build`.
