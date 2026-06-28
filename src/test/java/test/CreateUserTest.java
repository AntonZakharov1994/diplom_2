package test;


import data.UserModel;
import data.CreateUserSteps;
import io.qameta.allure.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class CreateUserTest {

    private CreateUserSteps steps;
    private String accessToken;

    @Before
    public void setUp() {
        io.restassured.RestAssured.baseURI = "https://stellarburgers.education-services.ru";

        steps = new CreateUserSteps();
        accessToken = null;
    }



    @Description("Успешное создание нового пользователя с корректными данными")
    @Test
    public void createUniqueUser() {
        String email = "user_" + System.currentTimeMillis() + "@example.com";
        UserModel user = new UserModel(email, "Password123", "Test User");

        var response = steps.createUser(user);

        response.then()
                .statusCode(200)
                .body("success", is(true));

        accessToken = response.jsonPath().getString("accessToken");
    }

    @Description("Попытка зарегистрировать пользователя с существующим email")
    @Test
    public void createDuplicateUser() {
        String email = "dup_" + System.currentTimeMillis() + "@example.com";
        UserModel user = new UserModel(email, "Password123", "Dup User");

        var firstResponse = steps.createUser(user);
        firstResponse.then().statusCode(200).body("success", is(true));
        String token = firstResponse.jsonPath().getString("accessToken");

        var secondResponse = steps.createUser(user);

        secondResponse.then()
                .statusCode(403)
                .body("message", notNullValue());

        accessToken = token;
    }

    @Description("Создание пользователя с пустым email")
    @Test
    public void createUserWithoutRequiredField() {
        UserModel user = new UserModel("", "Password123", "No Email User");

        var response = steps.createUser(user);

        response.then()
                .statusCode(403)
                .body("message", notNullValue());
    }
    @Description("Создание пользователя с пустым паролем")
    @Test
    public void createUserWithEmptyPassword() {
        String email = "pass_empty_" + System.currentTimeMillis() + "@example.com";
        UserModel user = new UserModel(email, "", "Test User");

        var response = steps.createUser(user);
        response.then()
                .statusCode(403)
                .body("message", notNullValue());
    }
    @Description("Создание пользователя с пустым именем")
    @Test
    public void createUserWithEmptyName() {
        String email = "name_empty_" + System.currentTimeMillis() + "@example.com";
        UserModel user = new UserModel(email, "Password123", "");
        var response = steps.createUser(user);
        response.then()
                .statusCode(403)
                .body("message", notNullValue());
    }

    @After
    public void tearDown() {
        if (steps != null && accessToken != null && !accessToken.isEmpty()) {
            try {
                steps.deleteUser(accessToken);
            } catch (Exception e) {
                System.out.println("Не удалось удалить тестового пользователя: " + e.getMessage());
            }
        }
    }
}
