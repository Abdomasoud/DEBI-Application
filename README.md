# DEBI-Application

## Overview

DEBI-Application is a JavaFX-based social media application named "Fakebook". It allows users to sign up, log in, and navigate between different views.

## Features

- User Signup
- User Login
- Navigation between primary, secondary, and home views

## Prerequisites

- Java 11 or higher
- Maven
- MySQL

## Setup

1. **Clone the repository:**

   ```sh
   git clone https://github.com/yourusername/DEBI-Application.git
   cd DEBI-Application
   ```

2. **Configure the database:**

   - Create a MySQL database named `fakebook`.
   - Update the `DatabaseUtil.java` file with your MySQL username and password.

3. **Build the project:**

   ```sh
   mvn clean install
   ```

4. **Run the application:**
   ```sh
   mvn javafx:run
   ```

## Usage

- **Signup:** Navigate to the signup view and create a new account.
- **Login:** Use your credentials to log in.
- **Navigation:** Switch between different views using the provided buttons.

## Project Structure

- `src/main/java/com/example/`: Contains Java source files.
- `src/main/resources/com/example/`: Contains FXML files for the UI.
- `target/`: Contains compiled classes and build artifacts.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License.
