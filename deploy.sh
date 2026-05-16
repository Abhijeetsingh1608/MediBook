#!/bin/bash
set -e

echo "========================================"
echo "   MediBook EC2 Deployment Script"
echo "========================================"

AWS_REGION="ap-south-1"
ECR_REGISTRY="629054268853.dkr.ecr.ap-south-1.amazonaws.com"

echo ""
echo "Step 1: Logging in to AWS ECR..."
aws ecr get-login-password --region $AWS_REGION | sudo docker login --username AWS --password-stdin $ECR_REGISTRY
echo "✅ ECR Login Successful"

echo ""
echo "Step 2: Pulling latest images from ECR..."
sudo docker compose pull
echo "✅ All images pulled"

echo ""
echo "Step 3: Stopping old containers (if any)..."
sudo docker compose down --remove-orphans
echo "✅ Old containers stopped"

echo ""
echo "Step 4: Starting all services..."
sudo docker compose up -d
echo "✅ All services started"

echo ""
echo "Step 5: Checking container status..."
sleep 5
sudo docker ps

echo ""
echo "========================================"
echo "✅ MediBook Deployment Complete!"
echo "========================================"
echo ""
echo "Service URLs:"
echo "  Eureka Dashboard : http://$(curl -s ifconfig.me):8761"
echo "  API Gateway      : http://$(curl -s ifconfig.me):8080"
echo "  RabbitMQ Console : http://$(curl -s ifconfig.me):15672"
echo ""
echo "To check logs: sudo docker compose logs -f <service-name>"
echo "Example      : sudo docker compose logs -f auth-service"
