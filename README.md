# Online Bidding Platform – Auction System

## Overview

This project is a web-based online bidding platform developed using Spring Boot. It allows users to participate in auctions by placing bids on listed items. The system manages user interactions, bidding processes, cart operations, and order tracking through a structured backend and a simple web interface.

---

## Features

* User management functionality
* Auction listing and bid placement
* Highest bid tracking mechanism
* Cart and order management
* Wallet handling system
* Return management
* JSON-based data handling
* Basic web interface for user interaction

---

## Technology Stack

* Java
* Spring Boot
* Maven
* HTML, CSS, JavaScript
* JSON (data storage)

---

## Architecture

The application follows a layered architecture:

* **Presentation Layer**: Handles user interface and incoming requests
* **Application Layer**: Processes business logic such as bidding and order management
* **Data Layer**: Stores and retrieves data using JSON files

This structure improves modularity and maintainability.

---

## Project Structure

```text id="h2s8ka"
backend/
│── src/main/java/        Application source code
│── src/main/resources/   Configuration and static files
│── data/                 JSON data storage
│── pom.xml               Project dependencies
```

---

## Installation and Setup

### Clone the repository

```id="y6d2pr"
git clone https://github.com/Nasirin-banu/Online-Bidding-Platform-Auction-System-using-Spring-Boot-.git
```

### Navigate to the project directory

```id="q9x4mz"
cd Online-Bidding-Platform-Auction-System-using-Spring-Boot-
```

### Run the application

```id="m7k3bv"
mvn spring-boot:run
```

### Access the application

Open a browser and navigate to:

```id="t4p9lx"
http://localhost:8080
```

---

## Usage

Users can browse auction items, place bids, and manage their orders through the application interface. The system processes bidding activities and maintains data using structured backend logic.

---

## Future Enhancements

* Integration with relational or NoSQL databases
* Implementation of authentication and authorization
* Real-time bidding functionality
* Integration with payment gateways
* Improved user interface

---

## Author

Nasirin Banu

---

## License

This project is intended for educational and learning purposes.
