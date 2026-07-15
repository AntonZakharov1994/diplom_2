package test;

import io.restassured.RestAssured;
import org.junit.BeforeClass;

public class BaseApiTest {
    @BeforeClass
    public static void setUpBaseUri() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
    }
}
