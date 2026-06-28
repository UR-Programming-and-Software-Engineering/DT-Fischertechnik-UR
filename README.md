# A Digital Twin Exemplar for a Fischertechnik Production Line

This application was developed as a Digital Twin exemplar for a modular Fischertechnik production line.

The core idea of the project is to provide a digital representation of a small-scale manufacturing system. The system consists of several machines, such as a conveyor belt, vacuum grippers, a high-bay storage unit, a multi-processing station and a sorting line. The Digital Twin makes it possible to monitor the current state of the production line, replay recorded production data and simulate different production scenarios.

The application allows users to observe the behaviour of the physical system, analyze production data and perform what-if analyses based on configurable simulation parameters. The goal is not to directly control the physical production line, but to provide a monitoring and analysis environment that helps users understand, simulate and optimize the behaviour of the system.

## Features

This application allows you to:

- Monitor the state of a Fischertechnik production line
- Receive and process MQTT messages from the production system
- Replay previously recorded MQTT data
- Visualize the current state of machines and components
- Run simulations of the production process
- Adjust simulation parameters such as load factors and package arrival rates
- Analyze the impact of parameter changes on throughput, energy consumption and machine behaviour
- Use an optimization component to identify improved parameter settings for selected objectives

## System Overview

The Digital Twin represents a modular production line consisting of the following main components:

- **Conveyor Belt**  
  Transports packages between different parts of the system

- **Vacuum Grippers**  
  Move packages between machines and route them through the production line

- **High-Bay Storage**  
  Stores and retrieves packages automatically

- **Multi-Processing Station**  
  Processes packages during the production workflow

- **Sorting Line**  
  Sorts packages based on their color, for example red, blue or white

Communication between the production system and the Digital Twin is based on MQTT. Messages are structured by machine, signal type and signal name. The Digital Twin receives data from the physical system or from recorded MQTT data and uses it to update the state of the simulated production line.

## Architecture

The application consists of several Docker containers:

- **Frontend**  
  React-based user interface for visualizing and interacting with the Digital Twin

- **Backend**  
  Spring Boot application that processes data, manages the system state and provides the API

- **MQTT Broker**  
  Mosquitto broker used for MQTT communication

- **Database**  
  Stores relevant application and simulation data

During development, an additional container can be used for development-specific tasks.

## Setup

### Needed Technology

The following technologies are required to run the application:

- Docker

## Development

For the commands to work, enter the `/codebase` directory first.

### Start the Docker Compose setup

```bash
docker compose -p digitaltwin up -d
````

### Stop the Docker Compose setup

```bash
docker compose -p digitaltwin down
```

### Stop the Docker Compose setup and delete the volume

```bash
docker compose -p digitaltwin down -v
```


### Rebuild and start the Docker Compose setup

Use this command when the Dockerfile or dependencies have changed:

```bash
docker compose -p digitaltwin up --build -d
```
## Usage

After starting the application, open the frontend in the browser.

Depending on the local configuration, the application can usually be accessed via:

```text
localhost:8080
```

or the configured frontend port.

The user can then monitor the Digital Twin, replay MQTT data and run simulations of the Fischertechnik production line.

## Links
* [This projects' Wiki](https://github.com/UR-Programming-and-Software-Engineering/DT-Fischertechnik-UR/wiki) with further explanations about the system architecture and MQTT data analysis results
* Description of the [Fischertechnik maschines](https://www.fischertechnik.de/de-de/produkte/industrie-und-hochschulen) from the Fischertechnik company

