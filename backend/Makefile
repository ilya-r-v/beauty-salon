.PHONY: up db logs down
up:
	docker compose --env-file ../.env up --build -d --wait
db:
	docker compose --env-file ../.env up -d --wait postgres
logs:
	docker compose --env-file ../.env logs -f backend
down:
	docker compose --env-file ../.env down
