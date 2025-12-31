# 💰 Split Tracker - Distributed Microservices Expense Manager

> A scalable, event-driven microservices application for splitting expenses with friends. Built with **Spring Boot 3**, **Apache Kafka**, **PostgreSQL**, and **Flutter**.

## 📖 Overview

**Split Tracker** is a full-stack solution that solves the problem of tracking shared expenses. Unlike a monolithic app, this project demonstrates a **fully distributed architecture** designed for high availability and fault tolerance.

It uses **Apache Kafka** for asynchronous communication (notifications & auditing) to ensure the main user flow remains fast and non-blocking, even under load.

---

## 🏗️ Architecture

The system is composed of **5 core microservices** and independent infrastructure components.

| Service | Port | Description |
| --- | --- | --- |
| **API Gateway** | `8080` | Entry point. Handles routing and load balancing. |
| **Service Registry** | `8761` | Eureka Server for dynamic service discovery. |
| **Identity Service** | `8081` | Manages users and authentication. |
| **Expense Service** | `8082` | Core logic. Handles splits and produces Kafka events. |
| **Notification Service** | Random | Consumes Kafka events to send push notifications (FCM). |
| **Audit Service** | `8084` | Consumes Kafka events to log user activity for compliance. |
| **Zipkin** | `9411` | Distributed tracing UI. |

---

## 🚀 Key Features

* **Microservices Architecture:** Fully decoupled services using REST and Feign Clients.
* **Event-Driven Design:** Uses **Kafka** to decouple high-latency tasks (Notifications, Auditing) from critical user paths.
* **Service Discovery:** Dynamic scaling using **Netflix Eureka**.
* **Distributed Tracing:** Full observability with **Micrometer** and **Zipkin** to track requests across service boundaries.
* **Mobile First:** Native Android/iOS app built with **Flutter**.
* **Resilience:** Dockerized environment ensuring consistent behavior across Dev and Prod.

---

## 🛠️ Tech Stack

### Backend

* **Language:** Java 17
* **Framework:** Spring Boot 3 (Web, Data JPA, Cloud)
* **Messaging:** Apache Kafka, Zookeeper
* **Database:** PostgreSQL
* **Observability:** Micrometer Tracing, Zipkin
* **Build Tool:** Maven

### Frontend

* **Framework:** Flutter (Dart)
* **State Management:** `setState` (Clean & Simple) / Provider
* **Integrations:** Firebase Cloud Messaging (FCM)

### DevOps & Infrastructure

* **Containerization:** Docker & Docker Compose
* **Gateway:** Spring Cloud Gateway
* **Registry:** Netflix Eureka

---

## ⚡ Getting Started (The "One-Command" Setup)

You can run the entire backend infrastructure (Database, Kafka, Zookeeper, and all 5 Java Services) with a single command.

### Prerequisites

* Docker Desktop installed & running.
* Java 17 (optional, if running locally).
* Flutter SDK (for the mobile app).

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/split-tracker.git
cd split-tracker

```

### 2. Run with Docker Compose

```bash
docker-compose up --build -d

```

*Wait for a few minutes for the images to build and services to register.*

### 3. Verify Deployment

* **Service Registry (Eureka):** Visit `http://localhost:8761`. You should see `IDENTITY-SERVICE`, `EXPENSE-SERVICE`, etc., registered.
* **Zipkin (Tracing):** Visit `http://localhost:9411` to view trace dashboards.
* **API Gateway:** The backend is accessible at `http://localhost:8080`.

---

## 📱 Running the Mobile App

1. Navigate to the flutter folder:
```bash
cd expense_tracker_app

```


2. Install dependencies:
```bash
flutter pub get

```


3. **Important:** Update the `baseUrl` in `lib/main.dart` or `lib/utils/api_client.dart` to your machine's local IP address (e.g., `192.168.1.5`). **Do not use `localhost**` if testing on a physical device.
4. Run the app:
```bash
flutter run

```



---

## 🧪 API Endpoints (Quick Reference)

You can test these via Postman or Curl.

**1. Create User (Identity Service)**

```http
POST http://localhost:8080/users/invite
Content-Type: application/json

{
  "name": "Rupesh",
  "email": "rupesh@example.com"
}

```

**2. Add Expense (Expense Service)**

```http
POST http://localhost:8080/expenses
Content-Type: application/json

{
  "description": "Lunch at Taco Bell",
  "amount": 50.0,
  "userId": 1,
  "splits": [
    { "userId": 2, "amount": 25.0 }
  ]
}

```

*(This triggers a Kafka event → Notification Service → Mobile Push)*

**3. View Audit Log (Audit Service)**

```http
GET http://localhost:8080/audit/1

```

---

## 🔮 Future Improvements

* [ ] **Graphs:** Visual spending analytics in Flutter.

---

## 👤 Author

**Rupesh**

* [LinkedIn](https://linkedin.com/in/rupesh-kankrej-20228a21a)
* [GitHub](https://github.com/RupeshKankrej)

---

### ⭐ Show your support

Give a ⭐️ if this project helped you!