package test;
import data.LoginData;
import data.CreateUserSteps;
import static org.apache.http.HttpStatus.*;
import io.qameta.allure.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;


public class LoginUserTest extends BaseApiTest {
    private CreateUserSteps steps;
    private String accessToken;
    private String testEmail;


    @Before
    public void setUp() {
        steps = new CreateUserSteps();
        accessToken = null;

        testEmail = "login_test_" + System.currentTimeMillis() + "@example.com";
        var user = new data.UserModel(testEmail, "Password123", "Login Test User");
        var loginData = new LoginData(testEmail, "Password123");

        var createResponse = steps.createUser(user);
        createResponse.then().statusCode(SC_OK).body("success", is(true));

        var loginResponse = steps.loginUser(loginData);
        this.accessToken = loginResponse.jsonPath().getString("accessToken");
    }

    @Description("Успешный вход под существующим пользователем")
    @Test
    public void loginWithExistingUser() {

        LoginData loginData = new LoginData(testEmail, "Password123");
        var loginResponse = steps.loginUser(loginData);

        loginResponse.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("accessToken", notNullValue())
                .body("user.email", equalTo(testEmail))
                .body("user.name", notNullValue());
    }
    @Description("Попытка входа под несуществующим пользователем")
    @Test
    public void loginWithNonExistingUser() {
        String email = "non_existing_" + System.currentTimeMillis() + "@example.com";
        LoginData loginData = new LoginData(email, "AnyPassword123");

        var loginResponse = steps.loginUser(loginData);
        loginResponse.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("message", notNullValue());
    }

    @Description("Попытка входа с неверным паролем")
    @Test
    public void loginWithWrongPassword() {
            LoginData wrongLoginData = new LoginData(testEmail, "WrongPassword123");
            var loginResponse = steps.loginUser(wrongLoginData);
            loginResponse.then()
                    .statusCode(SC_UNAUTHORIZED)
                    .body("message", notNullValue());
    }

    @After
    public void tearDown() {
        if (steps != null && accessToken != null && !accessToken.isEmpty()) {
            try {
                steps.deleteUser(accessToken);
            } catch (Exception e) {
                System.out.println("Не удалось удалить тестового пользователя: " + e.getMessage());
            } finally {
                accessToken = null;
                testEmail = null;
            }
        }
    }
}
