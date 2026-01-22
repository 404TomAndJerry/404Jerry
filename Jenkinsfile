pipeline {
    agent any
    environment {
        // 본인의 ECR URI로 변경하세요 (예: 1234.dkr.ecr...)
        ECR_REGISTRY = '237245538374.dkr.ecr.ap-northeast-2.amazonaws.com/server-repo'
        ECR_REPO = 'server-repo'
        DB_URL = 'jdbc:mysql://mysql:3306/main_db?useSSL=false&allowPublicKeyRetrieval=true'
    }
    stages {

            stage('Checkout Code') {
                steps {
                    git branch: 'main', url: 'https://github.com/404TomAndJerry/404Jerry'
                }
            }

            stage('Deploy with Script') {
                steps {
                    script {
                        echo "Executing Deploy Script..."
                        withCredentials([usernamePassword(credentialsId: 'rds-auth-key', usernameVariable: 'DB_USER', passwordVariable: 'DB_PASSWORD')]) {
                            // 스크립트에 실행 권한 주고 실행
                            sh """
                                chmod +x ./scripts/cd-deploy.sh
                                ./scripts/cd-deploy.sh
                            """
                        }
                    }
                }
            }
    }
}