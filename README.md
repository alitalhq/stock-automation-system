# Stock Automation System

A desktop application for inventory and stock management developed with JavaFX and Java 17. The project implements core Object-Oriented Programming (OOP) principles and utilizes JSON for data persistence.

## Default Credentials

For demonstration and testing purposes, use the following administrator credentials:

* Username: admin
* Password: admin

## Features

* Role-Based Access Control: Specialized interfaces for Admin and User roles.
* Inventory Management: Full CRUD operations for products.
* Stock Tracking: Real-time stock level updates and transaction logging.
* Persistent Storage: Local data storage in JSON format using Jackson library.
* Custom Exception Handling: Specialized error management for business logic and system operations.
* Modular UI: Decoupled design using FXML and external CSS.

## Tech Stack

* Programming Language: Java 17 (LTS)
* UI Framework: JavaFX
* Build Management: Apache Maven
* Data Format: JSON (via Jackson Databind)
* Architecture: Layered Architecture

## Project Structure
```
src/
├── main/
│   ├── java/com/example/stockautomationsystem/
│   │   ├── controller/
│   │   │   ├── LoginController.java
│   │   │   └── MainController.java
│   │   ├── exception/
│   │   │   ├── AuthenticationException.java
│   │   │   ├── DataPersistenceException.java
│   │   │   ├── InsufficientStockException.java
│   │   │   ├── InvalidProductException.java
│   │   │   └── UserAlreadyExistsException.java
│   │   ├── model/
│   │   │   ├── BaseProduct.java
│   │   │   ├── LogEntry.java
│   │   │   ├── Loggable.java
│   │   │   ├── Product.java
│   │   │   ├── Stockable.java
│   │   │   └── User.java
│   │   ├── service/
│   │   │   └── JsonService.java
│   │   └── MainApp.java
│   └── resources/
│       ├── login-view.fxml
│       ├── main-view.fxml
│       └── style.css
```
## OOP Principles Applied

* Abstraction: Implemented via the BaseProduct abstract class to define core templates.
* Encapsulation: Restricted data access using private fields and public accessors.
* Inheritance: Used in model hierarchies and custom exception structures.
* Polymorphism: Leveraged through Loggable and Stockable interfaces for flexible object handling.

## Installation and Setup

Execute the following commands in your terminal to set up and run the project:

```bash
# Clone the repository
git clone [https://github.com/yourusername/stock-automation-system.git](https://github.com/yourusername/stock-automation-system.git)

# Navigate to the project directory
cd stock-automation-system

# Install dependencies and build
mvn clean install

# Launch the application
mvn javafx:run
