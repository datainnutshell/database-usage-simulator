# Product Stock Simulation

This project is a Scala application that simulates product stock transactions and interactions with a MySQL database. It uses the Akka framework for concurrency and communication and interacts with a MySQL database for storing product stock information.

## Project Overview

The project consists of the following main components:

- **ProductStockSimulation**: Simulates product stock changes and transactions using Akka actors. It communicates with the MySQL database to store and retrieve product stock information.

## Quick Start

1. **Building the Project:**

   To build the project, you can use the following command:

   ```sh
   ./gradlew build
   ```

2. **Running the Application:**

   The application can be run using the following command:

   ```sh
   ./gradlew run
   ```

3. **Environment Variables:**

   The application uses the following environment variables:

   - `MYSQL_HOST`: Hostname of the MySQL database.
   - `MYSQL_PORT`: Port number of the MySQL database.
   - `MYSQL_DB`: Name of the MySQL database.
   - `MYSQL_USER`: MySQL database username.
   - `MYSQL_PASSWORD`: MySQL database password.

   Example:

   ```sh
   export MYSQL_HOST=localhost
   export MYSQL_PORT=3306
   export MYSQL_DB=product_stock_db
   export MYSQL_USER=dbuser
   export MYSQL_PASSWORD=dbpassword
   ```

4. **Docker Compose:**

   You can also use Docker Compose to run the application along with a MySQL database. Use the provided `docker-compose.yml` file.

   ```sh
   docker-compose up
   ```

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests for improvements, bug fixes, or new features.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
