package test;

import data.OrderModel;
import data.CreateUserSteps;
import data.LoginData;
import data.OrderSteps;
import io.qameta.allure.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class CreateOrderTest {

    private OrderSteps orderSteps;
    private CreateUserSteps userSteps;
    private String accessToken;
    private String validIngredientUuid;

    @Before
    public void setUp() {
        orderSteps = new OrderSteps();
        userSteps = new CreateUserSteps();

        String email = "order_test_" + System.currentTimeMillis() + "@example.com";
        var user = new data.UserModel(email, "Password123", "Order User");
        var loginData = new LoginData(email, "Password123");

        userSteps.createUser(user);
        var loginResponse = userSteps.loginUser(loginData);
        this.accessToken = loginResponse.jsonPath().getString("accessToken");

        var ingredientsResponse = io.restassured.RestAssured.given()
                .log().ifValidationFails()
                .when()
                .get("/api/ingredients");

        List<String> uuids = ingredientsResponse.jsonPath().getList("data._id");
        if (uuids.isEmpty()) {
            throw new IllegalStateException("Не удалось получить ни одного ингредиента из API");
        }
        this.validIngredientUuid = uuids.get(0);
    }



    @Description("Создание заказа с авторизацией и валидными ингредиентами")
    @Test
    public void createOrderWithAuthAndIngredients() {
        var order = new OrderModel(Collections.singletonList(validIngredientUuid));
        var response = orderSteps.createOrder(accessToken, order);

        response.then()
                .statusCode(200)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Description("Создание заказа с авторизацией, но без ингредиентов")
    @Test
    public void createOrderWithAuthAndNoIngredients() {
        var order = new OrderModel(Collections.emptyList());
        var response = orderSteps.createOrder(accessToken, order);

        response.then()
                .statusCode(400)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Description("Создание заказа без авторизации")
    @Test
    public void createOrderWithoutAuth() {
        var order = new OrderModel(Collections.singletonList(validIngredientUuid));
        var response = orderSteps.createOrderUnauthorized(order);

        response.then()
                .statusCode(200);

    }

    @Description("Создание заказа с неверным хешем ингредиентов")
    @Test
    public void createOrderWithInvalidIngredientHash() {
        var order = new OrderModel(Collections.singletonList("invalid-uuid-12345"));
        var response = orderSteps.createOrder(accessToken, order);

        response.then()
                .statusCode(500);

    }
    @After
    public void tearDown() {
        if (accessToken != null) {
            userSteps.deleteUser(accessToken);
        }
    }
}