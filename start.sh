#!/bin/bash
echo "🚀 Starting Diploma Generator on Replit..."
mvn clean spring-boot:run -Dspring-boot.run.profiles=replit -Dspring-boot.run.arguments="--server.port=8080 --server.address=0.0.0.0"
