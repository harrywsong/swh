package ca.gbc.comp3095.eventservice;

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

import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventServiceApplicationTests {

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
    void createEventTest() {
        String eventDate = LocalDateTime.now().plusDays(7).toString();
        String requestBody = """
                {
                   "title": "Test Workshop",
                   "description": "Test Description",
                   "date": "%s",
                   "location": "Test Location",
                   "capacity": 30
                }
                """.formatted(eventDate);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/events")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("eventId", Matchers.notNullValue())
                .body("title", Matchers.equalTo("Test Workshop"))
                .body("description", Matchers.equalTo("Test Description"))
                .body("location", Matchers.equalTo("Test Location"))
                .body("capacity", Matchers.equalTo(30))
                .body("registeredStudents", Matchers.equalTo(0));
    }

    @Test
    void getAllEventsTest() {
        String eventDate = LocalDateTime.now().plusDays(5).toString();
        String requestBody = """
                {
                   "title": "Test Seminar",
                   "description": "Test Seminar Description",
                   "date": "%s",
                   "location": "Test Location 123",
                   "capacity": 50
                }
                """.formatted(eventDate);

        // Create/Post Test Event
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/events")
                .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("eventId", Matchers.notNullValue())
                .body("title", Matchers.equalTo("Test Seminar"));

        // GET Events
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/events")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("title", Matchers.hasItem("Test Seminar"))
                .body("capacity", Matchers.hasItem(50));
    }

    private Integer createEventAndReturnId(String title, String description, String location, int capacity) {
        String eventDate = LocalDateTime.now().plusDays(7).toString();
        String requestBody = """
                {
                   "title": "%s",
                   "description": "%s",
                   "date": "%s",
                   "location": "%s",
                   "capacity": %d
                }
                """.formatted(title, description, eventDate, location, capacity);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/events")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("eventId");
    }

    @Test
    void updateEventTest() {
        Integer id = createEventAndReturnId(
                "New Event",
                "New Description",
                "New Location",
                20
        );

        String eventDate = LocalDateTime.now().plusDays(10).toString();
        String updateBody = """
                {
                   "title": "Updated Event",
                   "description": "Updated Description",
                   "date": "%s",
                   "location": "Updated Location",
                   "capacity": 25
                }
                """.formatted(eventDate);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/events/{id}", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", Matchers.equalTo("Updated Event"))
                .body("capacity", Matchers.equalTo(25));
    }

    @Test
    void registerStudentForEventTest() {
        Integer id = createEventAndReturnId(
                "Workshop 1",
                "Workshop 1 Description",
                "Room 101",
                25
        );

        RestAssured.given()
                .when()
                .post("/api/events/{id}/register", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("registeredStudents", Matchers.equalTo(1));

        // Register another student
        RestAssured.given()
                .when()
                .post("/api/events/{id}/register", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("registeredStudents", Matchers.equalTo(2));
    }

    @Test
    void unregisterStudentFromEventTest() {
        Integer id = createEventAndReturnId(
                "Workshop 2",
                "Workshop 2 Description",
                "Room 202",
                15
        );

        // Register student first
        RestAssured.given()
                .when()
                .post("/api/events/{id}/register", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("registeredStudents", Matchers.equalTo(1));

        // Unregister student
        RestAssured.given()
                .when()
                .post("/api/events/{id}/unregister", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("registeredStudents", Matchers.equalTo(0));
    }

    @Test
    void deleteEventTest() {
        // Create Test Event
        Integer id = createEventAndReturnId(
                "Temp Event",
                "Disposable event",
                "TBD",
                10
        );

        // Get Verify Event
        RestAssured.given()
                .when()
                .get("/api/events")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("eventId", Matchers.hasItem(id));

        // Delete Test Event
        RestAssured.given()
                .when()
                .delete("/api/events/{id}", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify Deletion
        RestAssured.given()
                .when()
                .get("/api/events")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("eventId", Matchers.not(Matchers.hasItem(id)));
    }

    @Test
    void getEventsByLocationTest() {
        createEventAndReturnId(
                "Location Title",
                "Location Description",
                "Building 123",
                100
        );

        RestAssured.given()
                .when()
                .get("/api/events/location/Building 123")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", Matchers.greaterThan(0))
                .body("location", Matchers.hasItem("Building 123"));
    }
}