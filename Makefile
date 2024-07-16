.PHONY: help build intg-test redis down clean redis_certs server-local server

help: ## Display this help message
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Compile the project
	./scripts/build.sh

intg-test: ## Build and start services in docker and run integration tests
	sudo docker compose up --build

down: ## Stop services
	sudo docker compose down --remove-orphans

docker-deploy: ## Deploy in docker
	./scripts/docker-run.sh && sudo docker compose up --build --scale client=0

redis_certs: ## Generate redis certs
	./scripts/redis_certs.sh

onyx-install: ## Install services locally
	./scripts/onyx-install.sh

onyx-kafka: ## Start kafka locally
	./scripts/onyx-start-kafka.sh

onyx-zookeeper: ## Start zookeeper locally
	./scripts/onyx-start-zookeeper.sh

onyx-idserver: ## Start idserver locally
	./scripts/onyx-start-idserver.sh

clean: ## Remove compiled files
	./scripts/clean.sh