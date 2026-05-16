pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            args  '-v /var/run/docker.sock:/var/run/docker.sock' // Allow docker commands inside container
        }
    }

    // ─────────────────────────────────────────────────────────
    //  CONFIGURE THESE BEFORE RUNNING
    //  1. Add AWS credentials in Jenkins → Manage Jenkins → Credentials
    //     ID: "aws-credentials" (type: AWS Credentials)
    //  2. Add SonarQube server in Jenkins → Manage Jenkins → Configure System
    //     Name: "SonarQube" pointing to your SonarQube instance
    //  3. Update AWS_REGION and ECS_CLUSTER below if different
    // ─────────────────────────────────────────────────────────

    environment {
        AWS_REGION      = 'ap-south-1'                          // ← Your AWS region (e.g. ap-south-1 for Mumbai)
        ECS_CLUSTER     = 'medibook-cluster'                    // ← Your ECS cluster name
        IMAGE_TAG       = "${env.BUILD_NUMBER}"                 // Each build gets unique tag
        SONAR_PROJECT   = 'medibook'
    }

    // List of all microservices
    // Format: "folder-name:ecs-service-name"
    // Update ECS service names to match what you create in AWS Console
    parameters {
        string(name: 'SERVICES', defaultValue: 'eureka-server api-gateway auth-service provider-service schedule-service appointment-service review-service record-service notification-service payment-service', description: 'Space-separated list of services to build and deploy')
    }

    stages {

        // ─────────────────────────────────────────────────────────
        //  STAGE 1: CHECKOUT
        // ─────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "Checking out branch: ${env.BRANCH_NAME}"
                checkout scm
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 2: BUILD & TEST (all modules)
        // ─────────────────────────────────────────────────────────
        stage('Build & Test') {
            steps {
                echo 'Building all modules and running tests...'
                sh '''
                    mvn clean verify \
                        -Dmaven.test.failure.ignore=false \
                        --batch-mode \
                        --no-transfer-progress
                '''
            }
            post {
                always {
                    // Publish JUnit test results
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                    // Publish JaCoCo coverage reports
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 3: SONARQUBE ANALYSIS
        // ─────────────────────────────────────────────────────────
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT} \
                            -Dsonar.projectName="MediBook" \
                            --batch-mode \
                            --no-transfer-progress
                    '''
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 4: QUALITY GATE
        // ─────────────────────────────────────────────────────────
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 5: DOCKER BUILD (all services in parallel)
        // ─────────────────────────────────────────────────────────
        stage('Docker Build') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    script {
                        // Get AWS account ID dynamically
                        env.AWS_ACCOUNT_ID = sh(
                            script: "aws sts get-caller-identity --query Account --output text",
                            returnStdout: true
                        ).trim()
                        env.ECR_REGISTRY = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"

                        echo "Building images for ECR registry: ${env.ECR_REGISTRY}"

                        def services = params.SERVICES.split(' ')
                        def buildStages = [:]

                        services.each { service ->
                            def svc = service.trim()
                            buildStages[svc] = {
                                echo "Building Docker image for: ${svc}"
                                sh """
                                    docker build \
                                        -t ${env.ECR_REGISTRY}/medibook/${svc}:${env.IMAGE_TAG} \
                                        -t ${env.ECR_REGISTRY}/medibook/${svc}:latest \
                                        -f ${svc}/Dockerfile \
                                        .
                                """
                            }
                        }

                        // Build all services in parallel
                        parallel buildStages
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 6: PUSH TO ECR
        // ─────────────────────────────────────────────────────────
        stage('Push to ECR') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    script {
                        // Login to ECR
                        sh """
                            aws ecr get-login-password --region ${env.AWS_REGION} \
                                | docker login --username AWS --password-stdin ${env.ECR_REGISTRY}
                        """

                        def services = params.SERVICES.split(' ')
                        def pushStages = [:]

                        services.each { service ->
                            def svc = service.trim()
                            pushStages[svc] = {
                                echo "Pushing image: ${svc}"
                                sh """
                                    docker push ${env.ECR_REGISTRY}/medibook/${svc}:${env.IMAGE_TAG}
                                    docker push ${env.ECR_REGISTRY}/medibook/${svc}:latest
                                """
                            }
                        }

                        // Push all images in parallel
                        parallel pushStages
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 7: DEPLOY TO ECS
        //  Deployment order matters — eureka first, gateway last
        // ─────────────────────────────────────────────────────────
        stage('Deploy to ECS') {
            when {
                // Only deploy from main branch
                branch 'main'
            }
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    script {
                        // Ordered deployment — DO NOT parallelize this
                        def orderedServices = [
                            'eureka-server',
                            'auth-service',
                            'provider-service',
                            'schedule-service',
                            'appointment-service',
                            'review-service',
                            'record-service',
                            'notification-service',
                            'payment-service',
                            'api-gateway'   // api-gateway always last
                        ]

                        orderedServices.each { svc ->
                            echo "Deploying ${svc} to ECS..."
                            sh """
                                aws ecs update-service \
                                    --cluster ${env.ECS_CLUSTER} \
                                    --service ${svc} \
                                    --force-new-deployment \
                                    --region ${env.AWS_REGION}
                            """
                            // Wait for service to stabilize before deploying next
                            sh """
                                aws ecs wait services-stable \
                                    --cluster ${env.ECS_CLUSTER} \
                                    --services ${svc} \
                                    --region ${env.AWS_REGION}
                            """
                            echo "${svc} deployed and stable ✅"
                        }
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 8: CLEANUP (remove local images to save disk)
        // ─────────────────────────────────────────────────────────
        stage('Cleanup') {
            steps {
                script {
                    def services = params.SERVICES.split(' ')
                    services.each { service ->
                        def svc = service.trim()
                        sh """
                            docker rmi ${env.ECR_REGISTRY}/medibook/${svc}:${env.IMAGE_TAG} || true
                            docker rmi ${env.ECR_REGISTRY}/medibook/${svc}:latest || true
                        """
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  POST ACTIONS — Notifications after pipeline
    // ─────────────────────────────────────────────────────────
    post {
        success {
            echo """
            ╔══════════════════════════════════╗
            ║  ✅ MediBook Deployment SUCCESS  ║
            ║  Build: #${env.BUILD_NUMBER}     ║
            ║  Branch: ${env.BRANCH_NAME}      ║
            ╚══════════════════════════════════╝
            """
        }
        failure {
            echo """
            ╔══════════════════════════════════╗
            ║  ❌ MediBook Deployment FAILED   ║
            ║  Build: #${env.BUILD_NUMBER}     ║
            ║  Check logs above for details    ║
            ╚══════════════════════════════════╝
            """
        }
        always {
            // Clean workspace after build
            cleanWs()
        }
    }
}
