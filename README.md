# A Digital Twin Exemplar for a Fischertechnik Production Line

## Development

For the commands to work you have to enter the /codebase directory

Start the docker compose:
`docker compose -p digitaltwin up -d`

Stop the docker compose:
`docker compose -p digitaltwin down`

Stop the docker compose and delete the volume:
`docker compose -p digitaltwin down -v`

Enter the container environment (for example the frontend):
`docker exec -it digitaltwin-frontend-1 sh`

Start/Rebuild the docker compose (for example when the DOCKERFILE has changed):
`docker compose -p digitaltwin up --build -d`

## Deployment

Before you start you have to copy the .env-template file and create a .env file.
This file will not be tracked by github and contains your sensitive information.
Adjust the database password (& have fun)!

Start the docker compose:
`docker compose -f docker-compose.dep.yaml -p digitaltwin up -d`

Stop the docker compose:
`docker compose -f docker-compose.dep.yaml -p digitaltwin down`

Stop the docker compose and delete the volume:
`docker compose -f docker-compose.dep.yaml -p digitaltwin down -v`

Enter the container environment (for example the frontend):
`docker exec -it digitaltwin-frontend-1 sh`

Start/Rebuild the docker compose (for example when the DOCKERFILE has changed):
`docker compose -f docker-compose.dep.yaml -p digitaltwin up --build -d`
