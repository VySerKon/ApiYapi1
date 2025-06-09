import io.restassured.http.ContentType;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


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



}

@Test
@DisplayName("Создание → обновление через PUT → проверка, что данные изменились")
void createUserThenUpdateAndVerify() {
    // 1. Создаём пользователя
    Map<String, String> createUserBody = new HashMap<>();
    createUserBody.put("name", "Shiza");
    createUserBody.put("job", "Superman");

    Response createResponse = given()
            .contentType(ContentType.JSON)
            .header("x-api-key", "reqres-free-v1")
            .body(createUserBody)
            .when()
            .post("https://reqres.in/api/users");

    createResponse.then().statusCode(201);
    int userId = createResponse.jsonPath().getInt("id");

    // 2. Обновляем его данные через PUT
    Map<String, String> updateUserBody = new HashMap<>();
    updateUserBody.put("name", "Shiza UPDATED");
    updateUserBody.put("job", "Batman");

    given()
            .contentType(ContentType.JSON)
            .header("x-api-key", "reqres-free-v1")
            .body(updateUserBody)
            .when()
            .put("https://reqres.in/api/users/" + userId)
            .then()
            .statusCode(200);

    // 3. Проверяем через GET, что данные обновились
    given()
            .header("x-api-key", "reqres-free-v1")
            .when()
            .get("https://reqres.in/api/users/" + userId)
            .then()
            .statusCode(200)
            .body("data.name", equalTo("Shiza UPDATED")) // Проверяем имя
            .body("data.job", equalTo("Batman"));        // Проверяем работу
}

//это с предусловием, тестом и постусловием
private int userId;

@BeforeEach
void setUp() {
    // Создаём пользователя один раз перед каждым тестом
    Map<String, String> userData = new HashMap<>();
    userData.put("name", "Shiza");
    userData.put("job", "Superman");

    Response response = given()
            .contentType(ContentType.JSON)
            .header("x-api-key", "reqres-free-v1")
            .body(userData)
            .when()
            .post("https://reqres.in/api/users");

    userId = response.jsonPath().getInt("id"); // Сохраняем ID для тестов
}

@Test
@DisplayName("Обновление пользователя через PUT (проверка ответа)")
void updateUser_PutRequest_ReturnsUpdatedData() {
    Map<String, String> updatedData = new HashMap<>();
    updatedData.put("name", "Shiza UPDATED");
    updatedData.put("job", "Batman");

    given()
            .contentType(ContentType.JSON)
            .header("x-api-key", "reqres-free-v1")
            .body(updatedData)
            .when()
            .put("https://reqres.in/api/users/" + userId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Shiza UPDATED"))  // Проверяем новое имя
            .body("job", equalTo("Batman"))         // Проверяем новую работу
            .body("updatedAt", notNullValue());     // Проверяем дату обновления
}

@AfterEach
void tearDown() {
    // Если API поддерживает удаление – можно добавить
    given()
            .header("x-api-key", "reqres-free-v1")
            .when()
            .delete("https://reqres.in/api/users/" + userId)
            .then()
            .statusCode(204);
}