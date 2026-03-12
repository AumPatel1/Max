# Max Backend - Stock Exchange Matching Engine

The backend service for Max stock exchange platform, built with Spring Boot. This service handles order matching, trade execution, and provides RESTful APIs for the frontend.

## 🏗️ Architecture

```
backend/
├── matching-engine/       # Main matching engine service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/example/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── journals/         # Engine logs
│   └── pom.xml
├── common/              # Shared utilities and models
│   ├── src/main/java/org/example/
│   └── pom.xml
└── src/                # Additional source files
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.2
- **Language**: Java 17+
- **Build Tool**: Maven
- **Parent**: Spring Boot Starter Parent 3.2.2

### Key Dependencies

- **Spring Boot Starter Web**: RESTful API development
- **Spring Boot Starter Validation**: Input validation
- **Lombok**: Reduce boilerplate code
- **JUnit Jupiter 5.10.0**: Unit testing

## 📋 Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

## 🚀 Getting Started

### 1. Install Dependencies

Navigate to the matching-engine directory:
```bash
cd backend/matching-engine
```

Install all dependencies:
```bash
mvn clean install
```

### 2. Configure Application

Edit `src/main/resources/application.properties`:
```properties
# Server Configuration
server.port=8080

# Application Name
spring.application.name=matching-engine

# Logging Configuration
logging.level.org.example=DEBUG
logging.file.name=journals/engine.log
```

### 3. Run the Application

**Using Maven:**
```bash
mvn spring-boot:run
```

**Using Java:**
```bash
mvn clean package
java -jar target/matching-engine-1.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=YourTestClassName
```

### Generate Test Coverage Report
```bash
mvn clean test jacoco:report
```

## 📁 Project Modules

### Matching Engine
The core service that handles:
- Order matching logic
- Trade execution
- Market data management
- RESTful API endpoints

### Common Module
Shared utilities and models used across services:
- Data models
- Utility classes
- Common configurations
- Shared constants

## 🔧 Development

### Build the Project
```bash
mvn clean install
```

### Run in Development Mode
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Package for Production
```bash
mvn clean package -DskipTests
```

## 📊 API Endpoints

### Health Check
```
GET /actuator/health
```

### Order Management
```
POST /api/orders          # Create new order
GET /api/orders/{id}      # Get order by ID
GET /api/orders           # List all orders
DELETE /api/orders/{id}   # Cancel order
```

### Market Data
```
GET /api/markets          # List all markets
GET /api/markets/{id}     # Get market details
```

## 🐛 Debugging

### Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.org.example=DEBUG
logging.level.org.springframework.web=DEBUG
```

### View Logs
```bash
tail -f journals/engine.log
```

## 🔒 Security Considerations

- Input validation using Spring Validation
- CORS configuration for frontend integration
- Request/Response logging for audit trails

## 📈 Performance

- Optimized order matching algorithm
- Efficient data structures for order books
- Asynchronous processing where applicable

## 🚀 Deployment

### Build Docker Image (Future)
```bash
docker build -t max-backend:latest .
docker run -p 8080:8080 max-backend:latest
```

### Environment Variables
```bash
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

## 📝 Code Style

- Follow Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Write unit tests for all business logic
- Document complex algorithms

## 🔄 CI/CD

### Maven Lifecycle
1. `mvn clean` - Clean build artifacts
2. `mvn compile` - Compile source code
3. `mvn test` - Run tests
4. `mvn package` - Create JAR file
5. `mvn install` - Install to local repository

## 🐞 Troubleshooting

### Port Already in Use
```bash
# Change port in application.properties
server.port=8081
```

### Maven Build Fails
```bash
# Clean and rebuild
mvn clean install -U
```

### Lombok Not Working
- Install Lombok plugin in your IDE
- Enable annotation processing

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Lombok Documentation](https://projectlombok.org/)

## 🤝 Contributing

1. Create a feature branch
2. Write tests for new features
3. Ensure all tests pass
4. Follow code style guidelines
5. Submit a pull request

## 📄 License

This project is private and proprietary.
