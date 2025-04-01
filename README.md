# Java Code Generator

A powerful code generation tool that creates Java applications from templates. The tool supports Spring Boot and Core Java templates with a flexible template syntax.

## Latest Changes

- Removed Maven support - all templates now use Gradle exclusively
- Updated Gradle configuration to use version 8.6
- Simplified template structure by removing redundant configurations
- Added support for variables in templates
- Improved file structure processing

## Template Syntax

### Variables Section

Each template can define variables at the beginning using the following syntax:

```
{!variables}
variableName=defaultValue
anotherVariable=anotherValue
{end_variables}
```

### File Sections

Files in the template are defined using the following syntax:

```
{!file}path/to/file{end_file}
file content goes here
```

### Variable Replacement

Variables can be used in both file paths and content using the syntax:

```
{!variableName}
```

Example:

```
{!variables}
basePackage=com.example.demo
applicationName=DemoApplication
{end_variables}

{!file}src/main/java/{!basePackage}/{!applicationName}.java{end_file}
package {!basePackage};

public class {!applicationName} {
    // class content
}
```

## Available Templates

### Spring Boot Templates

- `aws-template.template` - AWS services integration
- `base-app.template` - Basic Spring Boot application
- `cache-template.template` - Redis caching
- `cloud-template.template` - Cloud services integration
- `data-processing-template.template` - Data processing
- `docker-template.template` - Docker configuration
- `error-handling-template.template` - Error handling
- `integration-test-template.template` - Integration testing
- `messaging-template.template` - Message queue integration
- `monitoring-template.template` - Application monitoring
- `openapi-template.template` - OpenAPI/Swagger documentation
- `rest-api-template.template` - REST API
- `security-template.template` - Security configuration
- `testing-template.template` - Testing setup
- `webflux-template.template` - WebFlux reactive
- `batch-processing.template` - Spring Batch processing jobs
- `graphql-template.template` - GraphQL API implementation
- `elasticsearch-template.template` - Elasticsearch integration
- `websocket-template.template` - WebSocket real-time communication
- `multitenancy-template.template` - Multi-tenant application setup
- `kafka-template.template` - Apache Kafka integration
- `metrics-template.template` - Metrics collection with Micrometer

### Core Java Templates

- `hello-world.template` - Basic Java application
- Design Patterns templates in `design-patterns/` directory
- `cli-app.template` - Command-line interface application
- `desktop-app.template` - JavaFX desktop application
- `networking.template` - Network programming utilities
- `concurrency.template` - Multi-threading and parallel processing
- `database.template` - JDBC database connectivity
- `xml-processing.template` - XML processing utilities
- `json-processing.template` - JSON processing utilities

## Gradle Configuration

All templates now use a standardized Gradle configuration:

```gradle
plugins {
    id 'org.springframework.boot' version '3.4.4'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = '{!basePackage}'

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Template-specific dependencies
}

tasks.named('test') {
    useJUnitPlatform()
}

bootJar {
    layered {
        enabled = true
    }
}
```

## Usage

1. Select a template from the available options
2. Provide values for the template variables
3. The generator will create a complete project structure with:
   - Gradle build configuration
   - Source code files
   - Resource files
   - Test files
   - Docker configuration (if applicable)

## Directory Structure

Generated projects follow a standard Maven/Gradle directory structure:

```
project-root/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
└── docker-compose.yml (if applicable)
```

## Contributing

To add a new template:

1. Create a new `.template` file in the appropriate directory
2. Define variables using the `{!variables}` section
3. Add file sections using `{!file}` markers
4. Use `{!variableName}` for variable replacement
5. Include necessary Gradle dependencies
6. Add appropriate documentation