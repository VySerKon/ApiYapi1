import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class ApiYapiTest {


    static Stream<Arguments> dataUsers() {
        return Stream.of(
                Arguments.of(7, "Michael"),
                Arguments.of(8, "Lindsay"),
                Arguments.of(9, "Tobias"),
                Arguments.of(10, "Byron"),
                Arguments.of(11, "George"),
                Arguments.of(12, "Rachel")
        );
    }
    @ParameterizedTest
    @MethodSource("dataUsers")
    @DisplayName("Проверка полного соответствия имён и идентификаторов пользователей")
    void findNameForAllUsers(int userId, String hisName) {
        get("https://reqres.in/api/users?page=2")
                .then()
                .statusCode(200)
                .body("data.find { it.id == " + userId + " }.first_name", equalTo(hisName));
    }

    @Test
    @DisplayName("Проверка корректного сохранения параметров пользователя при создании")
    void checkUserCreation() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Shiza");
        requestBody.put("job", "Superman");
        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", "reqres-free-v1")
                .body(requestBody)
                .when()
                .post("https://reqres.in/api/users")
                .then()
                .statusCode(201)
                .body("name", equalTo("Shiza"))
                .body("job", equalTo("Superman"))
                .body("id", notNullValue())
                .body("createdAt", notNullValue());
    }

    //это с предусловием, тестом и постусловием, субъективно гибко, чтобы тест проходил без следов - 3 в 1
    private int userId;

    @BeforeEach
    void setUp() {
        // Создаю пользователя один раз перед каждым тестом
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Shiza");
        userData.put("job", "Superman");

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("x-api-key", "reqres-free-v1")
                .body(userData)
                .when()
                .post("https://reqres.in/api/users")
                .then()
                .log().all()
                .extract().response();

        userId = response.jsonPath().getInt("id");// Сохраняю ИД для тестов
        System.out.println("Created user ID: " + userId);
    }

    @Test
    @DisplayName("Обновление пользователя через PUT (с проверкой ответа)")
    void updateUser_PutRequest_ReturnsUpdatedData() {
        Map<String, String> updatedData = new HashMap<>();
        updatedData.put("name", "Shiza UPDATED");
        updatedData.put("job", "Batman");

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("x-api-key", "reqres-free-v1")
                .body(updatedData)
                .when()
                .put("https://reqres.in/api/users/" + userId)
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("Shiza UPDATED"))  // Проверяю новое имя
                .body("job", equalTo("Batman"))         // Проверяю новую работу
                .body("updatedAt", notNullValue());     // Проверяю дату обновления
    }

    @AfterEach
    void tearDown() {
        given()
                .log().all()
                .header("x-api-key", "reqres-free-v1")
                .when()
                .delete("https://reqres.in/api/users/" + userId)
                .then()
                .log().all()
                .statusCode(204);
    }
}



