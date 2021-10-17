pipeline{
    agent any
    tools{
        terraform 'terraform-15'
    }
    stages{
        stage('Git checkout'){
            steps{
                git branch: 'master', credentialsId: 'sojnar-github', url: 'https://github.com/sojnar/senai-monitor-infra.git'
            }
        }
        stage('Terraform init'){
            steps{
                sh ''' cd iac-azure-infra/terraform-iaas && 
                terraform init'''
            }
        }
        stage('Terraform plan'){
            steps{
                sh '''cd iac-azure-infra/terraform-iaas &&
                terraform plan'''
            }
        }

		stage('Terraform apply'){
			when {
				expression {
					return(
						TIPO_EXECUCAO == 'apply'
					)
				}
			}
			steps{
				sh '''cd iac-azure-infra/terraform-iaas &&
				terraform "apply" --auto-approve'''
			}
		}
		stage('Terraform destroy'){
			when {
				expression {
					return(
						TIPO_EXECUCAO == 'destroy'
					)
				}
			}
			steps{
				sh '''cd iac-azure-infra/terraform-iaas &&
				terraform "destroy" --auto-approve'''
			}
		}
        stage('Execucao Ansible'){
            when {
                expression {
                    return(
                        TIPO_EXECUCAO == 'apply'
                    )
                }
            }
            steps{
                sh 'rm -f /var/jenkins_home/.ssh/known_hosts'
                sh '''cd iac-azure-infra/ansible &&
                    export ANSIBLE_HOST_KEY_CHECKING=False
                    ansible-playbook -i hosts -u sojnar playbook.yml'''
            }
			
        }
    }
}