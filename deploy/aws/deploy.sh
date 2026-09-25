#!/usr/bin/env bash
# Builds and (re)starts the API on the EC2 instance. Run it as ec2-user after creating ~/mutuals.env.
set -euo pipefail

REPO_URL="${REPO_URL:-https://github.com/inkarrime/mutuals.git}"
BRANCH="${BRANCH:-master}"
APP_DIR="${APP_DIR:-$HOME/mutuals}"
ENV_FILE="${ENV_FILE:-$HOME/mutuals.env}"
CONTAINER=mutuals-backend

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing $ENV_FILE. Copy deploy/aws/production.env.example there and fill in the RDS values." >&2
  exit 1
fi

if [ -d "$APP_DIR/.git" ]; then
  git -C "$APP_DIR" fetch origin "$BRANCH"
  git -C "$APP_DIR" checkout "$BRANCH"
  git -C "$APP_DIR" reset --hard "origin/$BRANCH"
else
  git clone --branch "$BRANCH" "$REPO_URL" "$APP_DIR"
fi

docker build -t "$CONTAINER:latest" "$APP_DIR"
docker rm -f "$CONTAINER" >/dev/null 2>&1 || true
docker run -d \
  --name "$CONTAINER" \
  --restart unless-stopped \
  --env-file "$ENV_FILE" \
  -e SPRING_PROFILES_ACTIVE=prod \
  -p 80:8080 \
  -v mutuals-uploads:/app/uploads \
  "$CONTAINER:latest"

echo "Waiting for the API to become healthy..."
for _ in $(seq 1 60); do
  if curl -fs http://localhost/actuator/health >/dev/null; then
    echo "API is up: http://$(curl -fs http://checkip.amazonaws.com)/swagger-ui.html"
    exit 0
  fi
  sleep 5
done

echo "The API did not become healthy. Last logs:" >&2
docker logs --tail 80 "$CONTAINER" >&2
exit 1
