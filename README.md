# Smart Classroom Management System with Intelligent Study Planner

## 📌 Overview

Smart Classroom is a full-stack web application designed to manage classrooms, students, faculty, and study planning efficiently. It supports role-based access and provides tools for administration, teaching, and student productivity.

---

## 🚀 Features

### 👨‍💼 Admin

* Manage users (create, delete, enable/disable)
* Create and manage classrooms
* Assign faculty to classrooms
* View reports and analytics
* Post announcements

### 👨‍🏫 Faculty

* Manage assigned classrooms
* Track student attendance
* View enrolled students

### 👨‍🎓 Student

* View enrolled classrooms
* Track attendance
* Manage study tasks (study planner)

---

## 🛠️ Tech Stack

* Backend: Spring Boot (Java)
* Frontend: Thymeleaf (HTML, CSS)
* Database: MySQL
* ORM: Hibernate (JPA)
* Security: Spring Security
* Build Tool: Maven

---

## ⚙️ Installation & Setup

### 1. Clone Repository

git clone https://github.com/your-username/smart-classroom.git
cd smart-classroom

---

### 2. Setup Database (MySQL)

Run in MySQL:
CREATE DATABASE smartclassroom;

---

### 3. Configure Application

Go to:
src/main/resources/application.properties

Update:
spring.datasource.url=jdbc:mysql://localhost:3306/smartclassroom
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

---

### 4. Build Project

mvn clean install

---

### 5. Run Application

mvn spring-boot:run

OR run:
SmartClassroomApplication.java

---

### 6. Access Application

http://localhost:8080

---

## 🔐 Login Credentials (Sample)

Admin
Username: myadmin
Password: admin123

User
Username: Angel1
Password: angel1

User
Username: Mahi10
Password: Mahi10

User
Username: Scott21
Password: scott123

---

## 📂 Project Structure

src/main/java/com/smartclassroom
├── controller
├── service
├── repository
├── entity
└── config

src/main/resources
├── templates
├── static
└── application.properties

---

## ⚠️ Common Issues

Database Connection Error

* Ensure MySQL is running
* Check username/password

Port Already in Use
Change in application.properties:
server.port=8081

Maven Errors
Right click project → Maven → Update Project

---

## 🚀 Future Enhancements

* 🔹 Intelligent Study Planner

  * Auto-generate study schedules based on deadlines and priorities
  * Personalized task recommendations

* 🔹 Attendance Analytics Dashboard

  * Visual reports (charts/graphs)
  * Low attendance alerts for students

* 🔹 Email Notification System

  * Alerts for announcements
  * Attendance warnings
  * Task reminders

* 🔹 REST API Development

  * Expose backend services for mobile/web apps
  * Enable integration with external systems

* 🔹 Frontend Upgrade

  * Replace Thymeleaf with React.js for modern UI

* 🔹 Cloud Deployment

  * Host application on platforms like Render or AWS
  * Enable public access and scalability

---

## 👨‍💻 Author

Aishwerya 
Final Year Project - Smart Classroom Management System
