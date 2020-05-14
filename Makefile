.DEFAULT_GOAL := help

up: ## Start
	docker-compose pull server
	docker-compose up -d
	docker-compose logs -f

down: ## Stop and destroy
	docker-compose down

dev: ## For development (server + client)
	docker-compose up -d kms && \
	cd emoi-client && yarn hot & \
	cd emoi-server && make dev-only || \
	cd ../ ; jobs -p | xargs kill && \
	docker-compose kill kms

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'