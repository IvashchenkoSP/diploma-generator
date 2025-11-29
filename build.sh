#!/bin/bash
mvn clean package -DskipTests
java -jar target/diploma-generator-1.0.0.jar
