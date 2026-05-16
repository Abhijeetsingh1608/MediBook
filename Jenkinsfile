pipeline {
    agent any

    tools {
        jdk 'jdk-17'
        maven 'maven-3'
    }

    environment {
        AWS_REGION   = 'ap-south-1'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        DEPLOY_DIR   = '/var/lib/jenkins/medibook'
    }

    parameters {
        string(
            name: 'SERVICES',
            defaultValue: 'eureka-server api-gateway auth-service provider-service schedule-service appointment-service review-service record-service notification-service payment-service',
            description: 'Space-separated list of services to build and deploy'
        )
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
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 3: DOCKER BUILD (all services in parallel)
        // ─────────────────────────────────────────────────────────
        stage('Docker Build') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    script {
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
                                        ${svc}
                                """
                            }
                        }

                        parallel buildStages
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 4: PUSH TO ECR
        // ─────────────────────────────────────────────────────────
        stage('Push to ECR') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    script {
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
                                    # Auto-create ECR repo if it doesn't exist
                                    aws ecr describe-repositories --repository-names medibook/${svc} --region ${env.AWS_REGION} || \
                                    aws ecr create-repository --repository-name medibook/${svc} --region ${env.AWS_REGION}

                                    docker push ${env.ECR_REGISTRY}/medibook/${svc}:${env.IMAGE_TAG}
                                    docker push ${env.ECR_REGISTRY}/medibook/${svc}:latest
                                """
                            }
                        }

                        parallel pushStages
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 5: DEPLOY TO EC2 via docker-compose
        //  Jenkins runs on the same EC2, so we run docker compose directly
        // ─────────────────────────────────────────────────────────
        stage('Deploy to EC2') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                    sh """
                        # Setup deployment directory
                        mkdir -p ${env.DEPLOY_DIR}

                        # Copy docker-compose.yml to deployment directory
                        cp ${env.WORKSPACE}/docker-compose.yml ${env.DEPLOY_DIR}/docker-compose.yml

                        # Copy .env only if it doesn't already exist on EC2
                        if [ ! -f ${env.DEPLOY_DIR}/.env ]; then
                            echo "No .env found on EC2, copying from workspace..."
                            cp ${env.WORKSPACE}/.env ${env.DEPLOY_DIR}/.env
                        fi

                        # Login to ECR
                        aws ecr get-login-password --region ${env.AWS_REGION} \
                            | docker login --username AWS --password-stdin ${env.ECR_REGISTRY}

                        # Pull latest images and restart services
                        cd ${env.DEPLOY_DIR}
                        docker compose pull
                        docker compose up -d --remove-orphans

                        echo "=== Running Containers ==="
                        docker compose ps
                    """
                }
            }
        }

        // ─────────────────────────────────────────────────────────
        //  STAGE 6: CLEANUP (remove local build images to save disk)
        // ─────────────────────────────────────────────────────────
        stage('Cleanup') {
            steps {
                script {
                    def services = params.SERVICES.split(' ')
                    services.each { service ->
                        def svc = service.trim()
                        sh """
                            docker rmi ${env.ECR_REGISTRY}/medibook/${svc}:${env.IMAGE_TAG} || true
                        """
                    }
                    sh "docker image prune -f || true"
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  POST ACTIONS
    // ─────────────────────────────────────────────────────────
    post {
        success {
            echo """
            ╔══════════════════════════════════════╗
            ║  ✅ MediBook Deployment SUCCESS      ║
            ║  Build: #${env.BUILD_NUMBER}         ║
            ║  Branch: ${env.BRANCH_NAME}          ║
            ╚══════════════════════════════════════╝
            """
        }
        failure {
            echo """
            ╔══════════════════════════════════════╗
            ║  ❌ MediBook Deployment FAILED       ║
            ║  Build: #${env.BUILD_NUMBER}         ║
            ║  Check logs above for details        ║
            ╚══════════════════════════════════════╝
            """
        }
        always {
            cleanWs()
        }
    }
}
