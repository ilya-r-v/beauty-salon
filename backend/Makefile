.PHONY: up db logs down
up:
	docker compose up --build -d --wait
db:
	docker compose up -d --wait postgres
logs:
	docker compose logs -f backend
down:
	docker compose down
