package data;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class CreateUserSteps {
    static {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
    }

    @Step("Создаем пользователя")
    public Response createUser(UserModel userModel) {
        return given()
                .header("Content-type", "application/json")
                .body(userModel)
                .log().all()
                .when()
                .post(Endpoints.CREATE_USER);
    }

    @Step("Логин пользователя")
    public Response loginUser(LoginData loginData) {
        return given()
                .header("Content-type", "application/json")
                .body(loginData)
                .log().all()
                .when()
                .post(Endpoints.AUTH_USER);
    }

    @Step("Удаление пользователя по токену")
    public Response deleteUser(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .log().all()
                .when()
                .delete(Endpoints.DELETE_USER);
    }
}
