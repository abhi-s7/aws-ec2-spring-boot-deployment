# AWS EC2 Deployment Guide

This document outlines the end-to-end process for deploying the **AWS-EC2-Spring-Boot-Deployment** application on an Amazon EC2 instance.

---

## 1. IAM Role Setup (Identity)
We create an IAM Role to allow the EC2 instance to interact with other AWS services securely without hardcoding credentials.

*   **Role Name**: `AWS-EC2-Spring-Boot-Deployment-SSM-Role`
*   **Service**: EC2
*   **Permissions**: `AmazonSSMManagedInstanceCore`
    *   *Why?* Enables **AWS Session Manager** usage. This allows you to connect to the instance directly from the AWS Console browser, eliminating the need to manage SSH keys or open Port 22 if you choose not to.

**Steps**:
1.  Go to **IAM** -> **Roles** -> **Create role**.
2.  Select **EC2** as the trusted entity.
3.  Search and add `AmazonSSMManagedInstanceCore`.
4.  Name it and create.

---

## 2. Security Group Setup (Firewall)
This defines who is allowed to access your server. You can create this **before** launching the instance (in EC2 -> Security Groups) or **during** the launch wizard.

**Required Inbound Rules**:

1.  **SSH (Port 22)**
    *   **Source**: `My IP`
    *   **Why**: Allows ONLY your current machine to access the terminal.
    *   *Note*: If you restart your router and your IP changes, you will need to update this rule.

2.  **Custom TCP (Port 8080)**
    *   **Source**: `Anywhere` (`0.0.0.0/0`)
    *   **Why**: **Critical.** Spring Boot runs on port 8080 by default. If you do not explicitly open port 8080 (and only open HTTP port 80), **the application will not load**.

---

## 3. EC2 Instance Setup (Server)
We launch a virtual server to host the application.

*   **OS**: Amazon Linux 2023 (Optimized for AWS)
*   **Instance Type**: `t3.micro` (Free tier)
*   **User Data Script**: Automates Java installation on startup.

**Step-by-Step Configuration**:
1.  **Name and Tags**:
    *   **Name**: Enter `AWS-EC2-SpringBoot-Server`.
2.  **Application and OS Images (AMI)**:
    *   Select **Amazon Linux**.
    *   **AMI**: Amazon Linux 2023 AMI (Free tier eligible).
    *   **Architecture**: 64-bit (x86).
3.  **Instance Type**:
    *   Select **t3.micro** (Free tier eligible).
4.  **Key Pair (Login)**:
    *   Click **Create new key pair**.
    *   **Name**: `aws-ec2-spring-boot-deployment-key`
    *   **Key pair type**: `RSA`
    *   **Private key file format**: `.pem`
    *   Click **Create key pair** (The file will download automatically—keep it safe!).
5.  **Network Settings** (Firewall):
    *   **Option A (Existing)**: If you created the group in Step 2, click **Select existing security group** and choose it.
    *   **Option B (Create New - Recommended)**:
        *   Select **Create security group**.
        *   **SSH Rule**: Set Source to **My IP**.
        *   **App Rule (Crucial)**: You MUST click **Add security group rule**.
            *   **Type**: `Custom TCP`
            *   **Port**: `8080`
            *   **Source**: `Anywhere` (`0.0.0.0/0`)
6.  **Configure Storage**:
    *   **Keep Default** (8 GiB gp3). This is sufficient for this application.
7.  **Advanced Details**:
    *   Expand the **Advanced details** toggle.
    *   **IAM instance profile**: Select `AWS-EC2-Spring-Boot-Deployment-SSM-Role`.
        *   **Reason**: This acts as a fail-safe. If you lose your `.pem` key or get locked out of SSH (e.g., your IP changes), this role allows you to connect securely via **AWS Session Manager** in the browser without needing Port 22.
    *   **User Data**: Scroll to the very bottom and paste this script:
        ```bash
        #!/bin/bash
        dnf update -y
        dnf install java-21-amazon-corretto-devel -y
        java -version
        ```
8.  **Finalize**:
    *   Click **Launch instance**.

---

## 4. Step-by-Step Deployment Process

### A. Connect via SSH
Establish a secure connection to your server from your local terminal.

*   **Prerequisite**: Open terminal where you saved `aws-ec2-spring-boot-deployment-key.pem`.
*   **Secure the Key (Mac/Linux)**:
    ```bash
    chmod 400 aws-ec2-spring-boot-deployment-key.pem
    ```
*   **Connect**:
    ```bash
    ssh -i aws-ec2-spring-boot-deployment-key.pem ec2-user@<YOUR-EC2-PUBLIC-IP>
    ```

### B. Verify Environment
If you used the User Data script, Java 21 should be ready.

*   **Verify**:
    ```bash
    java -version
    ```
    *If not installed, run manually: `sudo yum install java-21-amazon-corretto-devel -y`*

### C. Build and Transfer the Application (SCP)

1.  **Build the JAR file**:
    Run this command in your local project root:
    ```bash
    ./mvnw clean package
    ```
    *(This creates the file inside the `target/` folder)*

2.  **Upload to EC2**:
    Upload your compiled `.jar` file from your local machine to the EC2 instance.
    ```bash
    scp -i aws-ec2-spring-boot-deployment-key.pem target/aws-ec2-spring-boot-deployment-0.0.1-SNAPSHOT.jar ec2-user@<YOUR-EC2-PUBLIC-IP>:/home/ec2-user
    ```

### D. Run the Application
Switch back to your **EC2 terminal** to launch the app.

1.  **Navigate**: `cd /home/ec2-user/`
2.  **Run**:
    ```bash
    java -jar aws-ec2-spring-boot-deployment-0.0.1-SNAPSHOT.jar
    ```
3.  **Access**: Open browser and go to `http://<YOUR-EC2-PUBLIC-IP>:8080`

### E. Run in Background (Optional)
If you close your terminal, the app will stop. To keep it running permanently:

1.  **Stop current process**: Press `Ctrl + C`.
2.  **Run with nohup**:
    ```bash
    nohup java -jar aws-ec2-spring-boot-deployment-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
    ```
3.  **View Logs**:
    ```bash
    tail -f app.log
    ```

### F. Stopping the App (Background Mode)
If you used `nohup` to run the app in the background, `Ctrl + C` will not stop it.

1.  **Find the Process ID (PID)**:
    ```bash
    pgrep -f java
    ```
    *(This returns a number, e.g., `12345`)*

2.  **Kill the Process**:
    ```bash
    kill <PID>
    ```
    *(Example: `kill 12345`)*

---

## 5. Elastic IP (Static Public IP)
By default, if you stop and start your EC2 instance, its Public IP address changes. To keep a permanent static IP, use an **Elastic IP**.

**Cost Warning**: AWS charges for all public IPv4 addresses (~$3.60/month), even if attached to a running instance. **Release it if you stop using it.**

### A. Allocate & Associate
1.  **Allocate**:
    *   Go to **EC2 Dashboard** -> **Network & Security** -> **Elastic IPs**.
    *   Click **Allocate Elastic IP address** -> Keep defaults -> Click **Allocate**.
2.  **Associate**:
    *   Select the new IP -> **Actions** -> **Associate Elastic IP address**.
    *   **Instance**: Select your instance (`AWS-EC2-SpringBoot-Server`).
    *   **Private IP address**: Select default from dropdown.
    *   Click **Associate**.
3.  **Verify**:
    *   Go to **Instances**. Your instance now has a permanent Public IP. Use this new IP for SSH and browser access.

### B. Cleanup (Crucial)
If you terminate the instance, the Elastic IP remains (and you keep paying for it).
1.  **Disassociate**: Actions -> **Disassociate Elastic IP address**.
2.  **Release**: Actions -> **Release Elastic IP address**.

---

## 6. Best Practices & Troubleshooting

*   **Security**: Never open Port 22 to `0.0.0.0/0`. Always restrict SSH to your specific IP address.
*   **Logs**: If the app crashes, check the output in the terminal or redirect logs to a file:
    ```bash
    java -jar aws-ec2-spring-boot-deployment-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
    tail -f app.log
    ```
