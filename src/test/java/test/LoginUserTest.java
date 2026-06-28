package test;
import data.LoginData;
import data.CreateUserSteps;
import data.UserModel;
import io.qameta.allure.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;


public class LoginUserTest {
    private CreateUserSteps steps;
    private String accessToken;

    @Before
    public void setUp() {
        io.restassured.RestAssured.baseURI = "https://stellarburgers.education-services.ru";
        steps = new CreateUserSteps();
        accessToken = null;
    }

    @Description("Успешный вход под существующим пользователем")
    @Test
    public void loginWithExistingUser() {
        String email = "success_" + System.currentTimeMillis() + "@example.com";
        UserModel user = new UserModel(email, "Password123", "Test User");

        var createResponse = steps.createUser(user);
        createResponse.then().statusCode(200).body("success", is(true));
        accessToken = createResponse.jsonPath().getString("accessToken");

        LoginData loginData = new LoginData(email, "Password123");
        var loginResponse = steps.loginUser(loginData);

        loginResponse.then()
                .statusCode(200)
                .body("success", is(true))
                .body("accessToken", notNullValue())
                .body("accessToken", containsString("Bearer"))
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(email))
                .body("user.name", notNullValue());
    }
    @Description("Попытка входа под несуществующим пользователем")
    @Test
    public void loginWithNonExistingUser() {
        // Генерируем гарантированно уникальный email, которого точно нет в базе
        String email = "non_existing_" + System.currentTimeMillis() + "@example.com";
        LoginData loginData = new LoginData(email, "AnyPassword123");

        var loginResponse = steps.loginUser(loginData);

        // Ожидаем 401 Unauthorized — сервер не должен подсказывать, что пользователя нет
        loginResponse.then()
                .statusCode(401)
                .body("message", notNullValue());
    }

    @Description("Попытка входа с неверным паролем")
    @Test
    public void loginWithWrongPassword() {
        String email = "wrong_" + System.currentTimeMillis() + "@example.com";
        UserModel user = new UserModel(email, "CorrectPassword123", "Wrong User");

        var createResponse = steps.createUser(user);
        createResponse.then().statusCode(200).body("success", is(true));
        accessToken = createResponse.jsonPath().getString("accessToken");

        LoginData wrongLoginData = new LoginData(email, "WrongPassword123");
        var loginResponse = steps.loginUser(wrongLoginData);
        loginResponse.then()
                .statusCode(401)
                .body("message", notNullValue());
    }

    @After
    public void tearDown() {
        if (steps != null && accessToken != null && !accessToken.isEmpty()) {
            try {
                steps.deleteUser(accessToken);
                System.out.println("Тестовый пользователь успешно удалён.");
            } catch (Exception e) {
                System.out.println("Не удалось удалить тестового пользователя: " + e.getMessage());
            } finally {
                accessToken = null;
            }
        }
    }
}
