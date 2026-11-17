#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
if [ -f .env ]; then
  echo 'Корневой .env уже существует.'
  exit 0
fi
umask 077
set -C
cat > .env <<ENV
DB_NAME=beauty_salon
DB_USER=postgres
DB_PASSWORD=$(openssl rand -hex 24)
JWT_SECRET=$(openssl rand -hex 48)
BOOTSTRAP_ADMIN_PASSWORD=Aa1$(openssl rand -hex 16)
DB_PORT=5432
APP_PORT=8080
ENV
echo 'Создан .env. Начальный пароль администратора находится в BOOTSTRAP_ADMIN_PASSWORD.'
