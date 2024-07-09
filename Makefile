.PHONY: build run test deploy

build:
	./gradlew build

run:
	./gradlew run

test:
	./gradlew test

deploy:
	./gradlew deploy
