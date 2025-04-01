package com.jcg.template.service;

import com.google.inject.Inject;

public class DescriptionService {
    @Inject
    public DescriptionService() {}
    
    public String getDefaultDescription(String pattern) {
        if (pattern.startsWith("spring-boot/")) {
            return getSpringBootDescription(pattern);
        } else if (pattern.startsWith("core-java/")) {
            return getCoreJavaDescription(pattern);
        }
        return "A Java-based application generated from template";
    }
    
    private String getSpringBootDescription(String pattern) {
        String templateName = pattern.substring("spring-boot/".length());
        switch (templateName) {
            case "aws-template":
                return "A Spring Boot application with AWS integration, including S3, SQS, DynamoDB, and Secrets Manager support";
            case "base-app":
                return "A basic Spring Boot application with a simple REST endpoint and standard project structure";
            case "batch-template":
                return "A Spring Boot Batch application for processing large-scale data with job scheduling capabilities";
            case "cache-template":
                return "A Spring Boot application demonstrating caching mechanisms with Redis/Caffeine integration";
            case "cloud-template":
                return "A Spring Cloud application with service discovery, config server, and circuit breaker patterns";
            case "data-processing-template":
                return "A Spring Boot application for ETL operations and data processing workflows";
            case "docker-template":
                return "A containerized Spring Boot application with Docker and Docker Compose configuration";
            case "error-handling-template":
                return "A Spring Boot application showcasing global error handling and exception management";
            case "gradle-base-template":
                return "A Gradle-based Spring Boot application with essential build configurations";
            case "integration-test-template":
                return "A Spring Boot application with comprehensive integration testing setup";
            case "messaging-template":
                return "A Spring Boot application with message queue integration (RabbitMQ/Kafka)";
            case "monitoring-template":
                return "A Spring Boot application with monitoring setup using Actuator, Prometheus, and Grafana";
            case "openapi-template":
                return "A Spring Boot REST API with OpenAPI/Swagger documentation";
            case "rest-api-template":
                return "A RESTful API built with Spring Boot, including CRUD operations and data validation";
            case "security-template":
                return "A secured Spring Boot application with JWT authentication and role-based authorization";
            case "webflux-template":
                return "A reactive Spring Boot application using WebFlux for non-blocking operations";
            default:
                return "A Spring Boot application with modern Java features and best practices";
        }
    }
    
    private String getCoreJavaDescription(String pattern) {
        String templateName = pattern.substring("core-java/".length());
        switch (templateName) {
            case "singleton":
                return "A Java implementation of the Singleton design pattern with thread-safety and serialization handling";
            case "hello-world":
                return "A simple Java application demonstrating basic project structure and Gradle build setup";
            default:
                return "A Java application showcasing core programming concepts and best practices";
        }
    }
} 