package ca.gbc.comp3095.goaltrackingservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.MongoDBContainer;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GoalTrackingServiceApplicationTests {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        mongoDBContainer.start();
    }

    @Test
    void createGoalTest() {
        String targetDate = LocalDate.now().plusMonths(1).toString();
        String requestBody = """
                {
                   "title": "Test Title",
                   "description": "Test Description",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "Test Category"
                }
                """.formatted(targetDate);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/goals")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("goalId", Matchers.notNullValue())
                .body("title", Matchers.equalTo("Test Title"))
                .body("description", Matchers.equalTo("Test Description"))
                .body("status", Matchers.equalTo("in-progress"))
                .body("category", Matchers.equalTo("Test Category"));
    }

    @Test
    void getAllGoalsTest() {
        String targetDate = LocalDate.now().plusWeeks(4).toString();
        String requestBody = """
                {
                   "title": "New Title",
                   "description": "New Description",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "New Category"
                }
                """.formatted(targetDate);

        // Create/Post Test Goal
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/goals")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("goalId", Matchers.notNullValue())
                .body("title", Matchers.equalTo("New Title"));

        // GET Goals
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/goals")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("title", Matchers.hasItem("New Title"))
                .body("category", Matchers.hasItem("New Category"));
    }

    @Test
    void getGoalsByCategoryTest() {
        String targetDate = LocalDate.now().plusWeeks(2).toString();
        String requestBody = """
                {
                   "title": "Second Title",
                   "description": "Second Description",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "Second Category"
                }
                """.formatted(targetDate);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/goals")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given()
                .when()
                .get("/api/goals/category/Second Category")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("category", Matchers.hasItem("Second Category"));
    }

    @Test
    void getGoalsByStatusTest() {
        String targetDate = LocalDate.now().plusDays(30).toString();
        String requestBody = """
                {
                   "title": "Third Title",
                   "description": "Third Description",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "Third Category"
                }
                """.formatted(targetDate);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/goals")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given()
                .when()
                .get("/api/goals/status/in-progress")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0));
    }

    private String createGoalAndReturnId(String title, String description, String category, String status) {
        String targetDate = LocalDate.now().plusWeeks(2).toString();
        String requestBody = """
                {
                   "title": "%s",
                   "description": "%s",
                   "targetDate": "%s",
                   "status": "%s",
                   "category": "%s"
                }
                """.formatted(title, description, targetDate, status, category);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/goals")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("goalId");
    }

    @Test
    void updateGoalTest() {
        String id = createGoalAndReturnId(
                "New Goal",
                "New Description",
                "New Category",
                "in-progress"
        );

        String targetDate = LocalDate.now().plusMonths(2).toString();
        String updateBody = """
                {
                   "title": "Updated Title",
                   "description": "Updated Description",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "Updated Category"
                }
                """.formatted(targetDate);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/goals/{id}", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", Matchers.equalTo("Updated Title"))
                .body("description", Matchers.equalTo("Updated Description"));
    }

    @Test
    void markGoalAsCompletedTest() {
        String id = createGoalAndReturnId(
                "Completed Goal",
                "Completed Description",
                "Completed Category",
                "in-progress"
        );

        RestAssured.given()
                .when()
                .patch("/api/goals/{id}/complete", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("status", Matchers.equalTo("completed"));
    }

    @Test
    void deleteGoalTest() {
        // Create Test Goal
        String id = createGoalAndReturnId(
                "Temp Goal",
                "Disposable goal",
                "test",
                "in-progress"
        );

        // Get Verify Goal
        RestAssured.given()
                .when()
                .get("/api/goals")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("goalId", Matchers.hasItem(id));

        // Delete Test Goal
        RestAssured.given()
                .when()
                .delete("/api/goals/{id}", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify Deletion
        RestAssured.given()
                .when()
                .get("/api/goals")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("goalId", Matchers.not(Matchers.hasItem(id)));
    }
}