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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class WellnessResourceServiceApplicationTests {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @LocalServerPort
    private Integer port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        registry.add("spring.cache.type", () -> "redis");
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
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

        // GET Resources - check that our resource exists in the list
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/resources")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("title", Matchers.hasItem("Yoga for Beginners"))
                .body("category", Matchers.hasItem("exercise"));
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
                .body("category", Matchers.hasItem("counseling"));
    }

    // FIXED: Changed return type from Long to Integer
    private Integer createResourceAndReturnId(String title, String description, String category, String url) {
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
        Integer id = createResourceAndReturnId(
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
        Integer id = createResourceAndReturnId(
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
                .body("resourceId", Matchers.hasItem(id));

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
                .body("resourceId", Matchers.not(Matchers.hasItem(id)));
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