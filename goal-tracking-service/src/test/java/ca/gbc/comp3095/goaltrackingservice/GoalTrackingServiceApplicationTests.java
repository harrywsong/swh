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
                   "title": "Meditate Daily",
                   "description": "Practice meditation for 10 minutes every morning",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "mindfulness"
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
                .body("title", Matchers.equalTo("Meditate Daily"))
                .body("description", Matchers.equalTo("Practice meditation for 10 minutes every morning"))
                .body("status", Matchers.equalTo("in-progress"))
                .body("category", Matchers.equalTo("mindfulness"));
    }

    @Test
    void getAllGoalsTest() {
        String targetDate = LocalDate.now().plusWeeks(4).toString();
        String requestBody = """
                {
                   "title": "Exercise Weekly",
                   "description": "Go to gym 3 times a week",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "fitness"
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
                .body("title", Matchers.equalTo("Exercise Weekly"));

        // GET Goals
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/goals")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("[0].title", Matchers.equalTo("Exercise Weekly"))
                .body("[0].category", Matchers.equalTo("fitness"));
    }

    @Test
    void getGoalsByCategoryTest() {
        String targetDate = LocalDate.now().plusWeeks(2).toString();
        String requestBody = """
                {
                   "title": "Sleep Better",
                   "description": "Sleep 8 hours nightly",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "sleep"
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
                .get("/api/goals/category/sleep")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("[0].category", Matchers.equalTo("sleep"));
    }

    @Test
    void getGoalsByStatusTest() {
        String targetDate = LocalDate.now().plusDays(30).toString();
        String requestBody = """
                {
                   "title": "Healthy Eating",
                   "description": "Eat 5 servings of vegetables daily",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "nutrition"
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
                "Read Books",
                "Read 1 book per month",
                "personal-development",
                "in-progress"
        );

        String targetDate = LocalDate.now().plusMonths(2).toString();
        String updateBody = """
                {
                   "title": "Read More Books",
                   "description": "Read 2 books per month",
                   "targetDate": "%s",
                   "status": "in-progress",
                   "category": "personal-development"
                }
                """.formatted(targetDate);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/goals/{id}", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", Matchers.equalTo("Read More Books"))
                .body("description", Matchers.equalTo("Read 2 books per month"));
    }

    @Test
    void markGoalAsCompletedTest() {
        String id = createGoalAndReturnId(
                "Morning Walk",
                "Walk for 20 minutes every morning",
                "fitness",
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