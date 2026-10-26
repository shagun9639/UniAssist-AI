# REST API Documentation & Integration Specification

## UniAssist AI – University Chatbot & Management API Reference

---

## 1. Overview & Base URIs

UniAssist AI provides a RESTful API architecture structured into two primary network services:

- **Spring Boot Backend (Port 8080):** Central API gateway, identity provider, business logic orchestrator, and database manager.
  - Base URL: `http://localhost:8080/api`
  - Interactive OpenAPI / Swagger UI: `http://localhost:8080/swagger-ui.html`
- **FastAPI AI & ML Service (Port 8000):** Natural language intent classification and FAQ retrieval engine.
  - Base URL: `http://localhost:8000`
  - Interactive FastAPI Swagger Docs: `http://localhost:8000/docs`

---

## 2. Authentication & Authorization

All secured endpoints require a valid JSON Web Token (JWT) transmitted via the standard HTTP Authorization header:

```http
Authorization: Bearer <jwt_token>
```

### Roles:
- `ROLE_STUDENT`: Default role granted upon student registration; access to personal chat history, courses, tickets, and profile.
- `ROLE_ADMIN`: Administrative role with privileges to manage FAQs, view student directories, update ticket statuses, triage unanswered queries, and inspect ML model analytics.

---

## 3. Spring Boot Backend Endpoints (`/api`)

### 3.1 Authentication Controller (`/api/auth`)

#### `POST /api/auth/login`
Authenticates a student or administrator and issues a JWT token.
- **Request Body:**
  ```json
  {
    "username": "student@uniassist.edu",
    "password": "Student@123"
  }
  ```
- **Response `200 OK`:**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 2,
    "email": "student@uniassist.edu",
    "fullName": "Aarav Sharma",
    "role": "ROLE_STUDENT",
    "studentId": "STU2026001",
    "department": "Computer Science & Engineering",
    "course": "B.Tech Computer Science",
    "semester": 4
  }
  ```

#### `POST /api/auth/register`
Registers a new university student account.
- **Request Body:**
  ```json
  {
    "fullName": "Priya Patel",
    "email": "priya.patel@uniassist.edu",
    "password": "Password@123",
    "studentId": "STU2026002",
    "mobileNumber": "+91 9876543210",
    "department": "Computer Science & Engineering",
    "course": "B.Tech Computer Science",
    "semester": 4
  }
  ```
- **Response `201 Created`:** User profile payload.

#### `GET /api/auth/me`
Retrieves the authenticated user's current session profile.
- **Headers:** `Authorization: Bearer <token>`
- **Response `200 OK`:** UserProfileDto.

#### `POST /api/auth/change-password`
Updates the authenticated user's password.
- **Request Body:** `{"currentPassword": "...", "newPassword": "..."}`
- **Response `200 OK`:** `{"message": "Password changed successfully."}`

---

### 3.2 Chatbot Controller (`/api`)

#### `POST /api/chat`
Sends a student query to the conversational pipeline, routes through the ML inference engine, logs message history, and returns a verified grounded response.
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "message": "When are the final semester exams scheduled?",
    "conversationId": 1
  }
  ```
- **Response `200 OK`:**
  ```json
  {
    "response": "End-semester examinations for the current academic session commence on November 15, 2026. The full date sheet is available under Academics.",
    "conversationId": 1,
    "intent": "exam_schedule",
    "confidence": 0.8421,
    "entities": {
      "semester": 4,
      "course": "btech"
    },
    "source": "FAQ_KNOWLEDGE_BASE",
    "isFallback": false
  }
  ```

#### `GET /api/conversations`
Retrieves all conversation sessions for the authenticated student.
- **Headers:** `Authorization: Bearer <token>`
- **Response `200 OK`:** Array of `ConversationDto`.

#### `GET /api/conversations/{id}`
Retrieves all historical message exchanges for a specific conversation session.
- **Headers:** `Authorization: Bearer <token>`
- **Response `200 OK`:** Array of `MessageDto`.

#### `POST /api/chat/feedback`
Submits student satisfaction feedback on a chatbot message.
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "messageId": 12,
    "isHelpful": true,
    "comments": "Very fast and accurate answer!"
  }
  ```
- **Response `200 OK`:** `{"message": "Feedback recorded successfully."}`

---

### 3.3 FAQ Knowledge Base Controller (`/api/faqs`)

#### `GET /api/faqs`
Retrieves all active, verified university FAQs.
- **Optional Query Params:** `?category=Admissions`

#### `GET /api/faqs/search`
Searches FAQs via keyword or natural question string.
- **Query Params:** `?q=attendance%20policy`

#### `GET /api/faqs/categories`
Returns distinct FAQ categories (Admissions, Academics, Finance, Facilities, etc.).

---

### 3.4 Academic Hub Controller (`/api/academics`)

#### `GET /api/academics/courses`
Lists available university degree courses, codes, departments, and credit allocations.

#### `GET /api/academics/events`
Lists chronological university academic calendar events, semester milestones, and deadlines.

#### `GET /api/academics/announcements`
Lists public campus announcements and urgent institutional notices.

---

### 3.5 Support Ticket Controller (`/api/tickets`)

#### `GET /api/tickets`
Lists all support tickets submitted by the authenticated student.

#### `POST /api/tickets`
Submits a new support ticket to university administrative staff.
- **Request Body:**
  ```json
  {
    "subject": "Discrepancy in Semester 3 Grade Card",
    "category": "EXAMINATION",
    "priority": "HIGH",
    "description": "My marksheet shows absent for Database Systems lab despite having full attendance."
  }
  ```

#### `GET /api/tickets/{id}`
Retrieves complete ticket metadata and the response discussion thread.

#### `POST /api/tickets/{id}/responses`
Appends a response message to a ticket thread.

---

### 3.6 Administration Controller (`/api/admin`) *(Requires `ROLE_ADMIN`)*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | Aggregated analytics (queries, feedback, tickets, top intents) |
| `GET` | `/api/admin/users` | Directory of registered student profiles |
| `GET` | `/api/admin/tickets` | Support ticket queue with optional `?status=OPEN` filter |
| `PUT` | `/api/admin/tickets/{id}/status` | Updates ticket status (`IN_PROGRESS`, `RESOLVED`, `CLOSED`) and assignee |
| `POST` | `/api/admin/tickets/{id}/responses` | Posts an official staff reply to a student ticket |
| `POST` | `/api/admin/faqs` | Creates a new verified FAQ entry |
| `PUT` | `/api/admin/faqs/{id}` | Updates an existing FAQ entry |
| `DELETE` | `/api/admin/faqs/{id}` | Deletes an FAQ entry from knowledge base |
| `GET` | `/api/admin/unanswered-queries` | Lists low-confidence queries requiring human staff triage |
| `PUT` | `/api/admin/unanswered-queries/{id}` | Resolves or dismisses an unanswered query |
| `GET` | `/api/admin/model-info` | Returns active ML model metadata and hyperparameters |
| `GET` | `/api/admin/model-metrics` | Returns test accuracy, macro F1, and per-intent breakdown |
| `GET` | `/api/admin/model-comparison` | Returns benchmark metrics between Logistic Regression and Linear SVC |

---

## 4. Python FastAPI AI Service Endpoints (Port 8000)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/health` | Health probe returning model status, version, and uptime |
| `POST` | `/predict` | Raw intent classification and posterior probability distribution |
| `POST` | `/chat` | End-to-end chat resolution, entity extraction, and grounded retrieval |
| `POST` | `/feedback` | Records inline user feedback for active learning dataset curation |
| `GET` | `/model-info` | Returns model architecture, hyperparameters, and class labels |
| `GET` | `/model-metrics` | Returns held-out test evaluation report and per-class stats |
| `GET` | `/model-comparison` | Returns benchmark metrics between Model A and Model B |
| `GET` | `/confusion-matrix` | Streams the PNG confusion matrix image directly |
