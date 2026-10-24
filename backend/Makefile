.PHONY: migrate-up migrate-down migrate-status migrate-create

migrate-up:
	docker compose run --rm migrate up

migrate-down:
	docker compose run --rm migrate down 1

migrate-version:
	docker compose run --rm migrate version

migrate-create:
	docker compose run --rm --entrypoint migrate migrate -path=/migrations -database="" create -ext sql -dir /migrations $(name)

up:
	docker compose up --build -d

db:
	docker compose up -d postgres

logs:
	docker compose logs -f backend

down:
	docker compose down