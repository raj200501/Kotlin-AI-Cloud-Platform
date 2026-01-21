.PHONY: build run test deploy

build:
	./gradlew build

run:
	./gradlew run

test:
	./gradlew test

deploy:
	./gradlew deploy

setup:
	./scripts/bootstrap_gradle_wrapper.sh

lint:
	./scripts/lint.sh

fmt:
	@echo "No formatter configured; skipping."

verify:
	./scripts/verify.sh
