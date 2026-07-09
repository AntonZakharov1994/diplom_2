package data;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.List;
import static io.restassured.RestAssured.given;

public class OrderSteps {


    @Step("Получаем список ингредиентов")
    public List<String> getIngredients() {
        return given()
                .header("Content-type", "application/json")
                .log().ifValidationFails()
                .when()
                .get(Endpoints.GET_INGREDIENTS)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data._id");
    }

    @Step("Создаем заказ с авторизацией")
    public Response createOrder(String accessToken, OrderModel order) {
        return given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .log().ifValidationFails()
                .body(order)
                .when()
                .post(Endpoints.CREATE_ORDER);
    }

    @Step("Пытаемся создать заказ без авторизации")
    public Response createOrderUnauthorized(OrderModel order) {
        return given()
                .header("Content-type", "application/json")
                .log().ifValidationFails()
                .body(order)
                .when()
                .post(Endpoints.CREATE_ORDER);
    }
    @Step("Получаем UUID первого ингредиента")
    public String getFirstIngredientUuid() {
        var response = given()
                .header("Content-type", "application/json")
                .log().ifValidationFails()
                .when()
                .get(Endpoints.GET_INGREDIENTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<String> uuids = response.jsonPath().getList("data._id");
        if (uuids.isEmpty()) {
            throw new IllegalStateException("Список ингредиентов пуст — тест не может быть выполнен");
        }
        return uuids.get(0);
    }
}
