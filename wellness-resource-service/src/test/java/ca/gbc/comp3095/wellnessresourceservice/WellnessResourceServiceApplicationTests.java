package ca.gbc.comp3095.wellnessresourceservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WellnessResourceServiceApplicationTests {

    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        postgreSQLContainer.start();
    }

    @Test
    void createResourceTest() {
        String requestBody = """
                {
                   "title": "Meditation Guide",
                   "description": "A comprehensive guide to meditation",
                   "category": "mindfulness",
                   "url": "https://example.com/meditation"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/resources")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("resourceId", Matchers.notNullValue())
                .body("title", Matchers.equalTo("Meditation Guide"))
                .body("description", Matchers.equalTo("A comprehensive guide to meditation"))
                .body("category", Matchers.equalTo("mindfulness"))
                .body("url", Matchers.equalTo("https://example.com/meditation"));
    }

    @Test
    void getAllResourcesTest() {
        String requestBody = """
                {
                   "title": "Yoga for Beginners",
                   "description": "Learn basic yoga poses",
                   "category": "exercise",
                   "url": "https://example.com/yoga"
                }
                """;

        // Create/Post Test Resource
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/resources")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("resourceId", Matchers.notNullValue())
                .body("title", Matchers.equalTo("Yoga for Beginners"));

        // GET Resources
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/resources")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("[0].title", Matchers.equalTo("Yoga for Beginners"))
                .body("[0].category", Matchers.equalTo("exercise"));
    }

    @Test
    void getResourcesByCategoryTest() {
        String requestBody = """
                {
                   "title": "Counseling Services",
                   "description": "Professional counseling support",
                   "category": "counseling",
                   "url": "https://example.com/counseling"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/resources")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given()
                .when()
                .get("/api/resources/category/counseling")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("[0].category", Matchers.equalTo("counseling"));
    }

    private Long createResourceAndReturnId(String title, String description, String category, String url) {
        String requestBody = """
                {
                   "title": "%s",
                   "description": "%s",
                   "category": "%s",
                   "url": "%s"
                }
                """.formatted(title, description, category, url);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/resources")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("resourceId");
    }

    @Test
    void updateResourceTest() {
        Long id = createResourceAndReturnId(
                "Sleep Tips",
                "Tips for better sleep",
                "sleep",
                "https://example.com/sleep"
        );

        String updateBody = """
                {
                   "title": "Sleep Tips Updated",
                   "description": "Advanced tips for better sleep",
                   "category": "sleep",
                   "url": "https://example.com/sleep-updated"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/resources/{id}", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", Matchers.equalTo("Sleep Tips Updated"))
                .body("description", Matchers.equalTo("Advanced tips for better sleep"));
    }

    @Test
    void deleteResourceTest() {
        // Create Test Resource
        Long id = createResourceAndReturnId(
                "Temp Resource",
                "Disposable resource",
                "test",
                "https://example.com/temp"
        );

        // Get Verify Resource
        RestAssured.given()
                .when()
                .get("/api/resources")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("resourceId", Matchers.hasItem(id.intValue()));

        // Delete Test Resource
        RestAssured.given()
                .when()
                .delete("/api/resources/{id}", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify Deletion
        RestAssured.given()
                .when()
                .get("/api/resources")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("resourceId", Matchers.not(Matchers.hasItem(id.intValue())));
    }

    @Test
    void searchResourcesByKeywordTest() {
        createResourceAndReturnId(
                "Stress Management",
                "Techniques to manage stress",
                "mental-health",
                "https://example.com/stress"
        );

        RestAssured.given()
                .queryParam("keyword", "stress")
                .when()
                .get("/api/resources/search")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0));
    }
}