# AWS EC2 Spring Boot Deployment

A Spring Boot application configured for deployment on Amazon EC2. This project demonstrates the infrastructure setup and deployment workflow for Java web applications on AWS.

## Deployment Results
Once deployed, the application is accessible via the EC2 instance's Public IP or Elastic IP:
*   **Public URL**: `http://<EC2-Public-IP>:8080`
*   **Expected Result**: A styled HTML welcome page confirming the server is active.
*   **Infrastructure**: Runs on Amazon Linux 2023 with Java 21, managed via systemd or background processes.

## Documentation
Full deployment instructions are available in the **[Deployment Guide](AWS-EC2-Deployment-Guide.md)**.

**The guide covers:**
*   IAM Role configuration (for Session Manager access).
*   EC2 instance provisioning (Amazon Linux 2023).
*   Security Group setup (Firewall rules for SSH/HTTP).
*   Manual deployment via SSH and SCP.
*   Elastic IP configuration.

## Project Specifications
*   **Framework**: Spring Boot 3.5.x
*   **Language**: Java 17 / 21
*   **Build Tool**: Maven
*   **Template Engine**: Thymeleaf

## Local Development

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    ```

2.  **Build the application**:
    ```bash
    ./mvnw clean package
    ```

3.  **Run locally**:
    ```bash
    ./mvnw spring-boot:run
    ```
    Access the application at `http://localhost:8080`.
