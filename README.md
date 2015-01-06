play_api_example
================

Example of API with play framework 2.3 that use mongodb

# Dependencies and technology
- ReactiveMongo
- Vagrant
- Docker
- BCrypt
- Spracebook

# Commands :
## Run the app in test
    sbt run
    open http://localhost:9000
## Run in docker
    docker build . -tag "play_api_example"
    docker run -t -p 9000:9000 play_api_example
    open http://localhost:9000
## Run in a docker in vagrant
    vagrant up
    open http://localhost:9000