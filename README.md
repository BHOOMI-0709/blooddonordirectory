# 🩸 RaktSetu – Blood Donor Directory

RaktSetu is a full-stack blood donor directory web application built with **Spring Boot (backend) and HTML, CSS, and JavaScript (frontend)**. It features a clean, light-themed interface.

The frontend is served directly from Spring Boot's `static` directory, allowing the entire application to run on a single server and be deployed as one service.

## Features

* Donor registration with validation, duplicate phone number checks, and consent.
* Search and filter donors by name, blood group, city, and availability.
* Pagination and live donor counts by blood group.
* Donor cards with availability status and Call and WhatsApp buttons.
* Blood request management with Normal, Urgent, and Critical priorities.
* Filtering of Open and Fulfilled blood requests.
* Admin functionality for managing donors and blood requests.
* Blood group compatibility chart, donation eligibility information, and FAQs.
* Responsive design with a light color theme.

## Project Structure

```text
blood-donor-directory/
├── pom.xml
├── Dockerfile
├── .dockerignore
├── .gitignore
├── README.md
└── src/main/
    ├── java/com/blooddonor/
    │   ├── BloodDonorApplication.java
    │   ├── model/
    │   ├── repository/
    │   ├── service/
    │   ├── controller/
    │   ├── exception/
    │   └── config/
    └── resources/
        ├── application.properties
        ├── application-prod.properties
        └── static/
            ├── index.html
            ├── css/style.css
            └── js/
                ├── config.js
                └── app.js
```

### Request Flow

`Browser → Controller → Service → Repository → Database`

## 1. Run the Application Locally

### Prerequisites

* Java 17 or later
* Maven, or an IDE such as IntelliJ IDEA, Eclipse, or VS Code with Java support

### Start the Application

```bash
mvn spring-boot:run
```

Open your browser and visit:

`http://localhost:8080`

### Additional Information

* Demo donors and blood requests may be created automatically when demo seeding is enabled.
* The local database uses H2, according to the application configuration.
* The local admin key is `admin123`. This key must be changed before production deployment.

To run the application from an IDE, execute the `main` method in `BloodDonorApplication.java`.

### Build the Application

```bash
mvn clean package
```

Run the generated JAR file using the actual filename created in the `target` directory:

```bash
java -jar target/<generated-file-name>.jar
```

## 2. API Reference

| Method | Endpoint                    | Access | Description                 |
| ------ | --------------------------- | ------ | --------------------------- |
| GET    | `/api/donors`               | Public | Search and filter donors    |
| GET    | `/api/donors/stats`         | Public | Retrieve donor statistics   |
| GET    | `/api/donors/{id}`          | Public | Retrieve a donor by ID      |
| POST   | `/api/donors`               | Public | Register a donor            |
| PUT    | `/api/donors/{id}`          | Admin  | Update donor information    |
| DELETE | `/api/donors/{id}`          | Admin  | Delete a donor              |
| GET    | `/api/requests`             | Public | Retrieve blood requests     |
| POST   | `/api/requests`             | Public | Create a blood request      |
| PATCH  | `/api/requests/{id}/fulfil` | Admin  | Mark a request as fulfilled |
| DELETE | `/api/requests/{id}`        | Admin  | Delete a blood request      |
| GET    | `/api/admin/verify`         | Admin  | Verify the admin key        |

### Search Example

```text
/api/donors?bloodGroup=A%2B&city=meerut&q=aarav&available=true&page=0&size=9
```

For blood groups containing a plus sign, encode `+` as `%2B` in URL parameters.

Admin requests must include the following HTTP header:

```text
X-Admin-Key: <your-admin-key>
```

## 3. Configuration

The application supports the following environment variables. Confirm their exact defaults in the application configuration files before deployment.

| Variable                 | Purpose                                           |
| ------------------------ | ------------------------------------------------- |
| `PORT`                   | Server port                                       |
| `ADMIN_KEY`              | Admin access key                                  |
| `SEED_DEMO`              | Enables or disables demo data                     |
| `CORS_ORIGINS`           | Permitted frontend origins when hosted separately |
| `SPRING_PROFILES_ACTIVE` | Selects the active Spring profile                 |
| `DB_URL`                 | Production database JDBC URL                      |
| `DB_USER`                | Database username                                 |
| `DB_PASSWORD`            | Database password                                 |

The production profile is intended for PostgreSQL, while local development uses the database configured in `application.properties`.

## 4. Deployment

### Option A: Deploy the Full Application Together

This option serves the frontend and backend from the same Spring Boot application.

#### Step 1: Upload the Code to GitHub

Create a GitHub repository and push the project code to it.

Repository: [RaktSetu Blood Donor Directory](https://github.com/BHOOMI-0709/blooddonordirectory)

#### Step 2: Create a PostgreSQL Database

Create a managed PostgreSQL database using a suitable hosting provider.

Keep the following details available privately:

* Database host
* Database name
* Database username
* Database password

Configure the JDBC connection URL in this format:

```text
jdbc:postgresql://<HOST>:5432/<DATABASE_NAME>?sslmode=require
```

Use the connection details supplied by your database provider.

#### Step 3: Deploy on Render

1. Sign in to Render.
2. Create a new Web Service.
3. Connect the GitHub repository.
4. Select Docker as the runtime if using the included Dockerfile.
5. Configure the required environment variables.

| Variable                 | Value                    |
| ------------------------ | ------------------------ |
| `SPRING_PROFILES_ACTIVE` | `prod`                   |
| `DB_URL`                 | Your PostgreSQL JDBC URL |
| `DB_USER`                | Your database username   |
| `DB_PASSWORD`            | Your database password   |
| `ADMIN_KEY`              | A strong, unique secret  |
| `SEED_DEMO`              | `false` for production   |

6. Start the deployment and review the build logs.
7. Open the public URL provided by Render.

**Important:** Verify the Dockerfile, production database configuration, and environment variable names before deploying.

Free hosting services may suspend inactive applications or impose resource limits. Check the provider's current pricing and service limitations.

#### Step 4: Verify the Deployment

* Confirm that the homepage loads.
* Register a test donor.
* Verify that donor information is saved and retrieved correctly.
* Test donor search and filtering.
* Verify admin access and blood request functionality.
* Check application logs if any feature fails.

### Option B: Deploy the Frontend Separately

If you prefer separate frontend hosting:

1. Deploy the Spring Boot backend.
2. Configure `src/main/resources/static/js/config.js` to use the deployed backend URL:

```javascript
window.APP_CONFIG = {
    API_BASE: "https://your-backend.onrender.com"
};
```

3. Deploy the frontend files to a suitable static hosting provider.
4. Configure `CORS_ORIGINS` on the backend with the actual frontend origin.
5. Redeploy the backend and test the frontend-to-backend connection.

Confirm the existing `config.js` format before replacing its contents.

### Option C: Deploy to a VPS

Build the application:

```bash
mvn clean package
```

Configure the production environment variables and run the generated JAR file.

For a public production deployment, configure HTTPS, a reverse proxy such as Nginx, and appropriate server security.

## 5. Troubleshooting

| Issue                              | Possible Solution                                                      |
| ---------------------------------- | ---------------------------------------------------------------------- |
| Missing `DB_URL` configuration     | Check the production environment variables                             |
| Port already in use                | Change the local port or stop the conflicting process                  |
| Donors do not appear               | Check browser console errors, API responses, and database connectivity |
| Database connection fails          | Verify the JDBC URL, credentials, and SSL requirements                 |
| Admin authentication fails         | Check that the configured admin key matches the supplied key           |
| Data disappears after redeployment | Check whether the application uses persistent database storage         |
| Maven command is unavailable       | Install Maven or use the IDE's Maven integration                       |

## 6. Security and Privacy

* Replace the default admin key before production deployment.
* Never commit database passwords, admin secrets, or other credentials to GitHub.
* Use HTTPS for public deployments.
* Obtain appropriate consent before displaying donor contact information.
* Review applicable privacy and data protection requirements before collecting real user information.
* Consider rate limiting, CAPTCHA, and stronger authentication for production use.
* Blood donation eligibility and medical screening must be handled by qualified healthcare professionals and blood banks.

## 7. Future Enhancements

* User authentication with Spring Security and JWT.
* Email or SMS notifications for matching blood requests.
* Location-based donor discovery.
* Automated tests using JUnit and MockMvc.
* Database migration management using Flyway.

---

**RaktSetu – Connecting Blood Donors with Those in Need.**
