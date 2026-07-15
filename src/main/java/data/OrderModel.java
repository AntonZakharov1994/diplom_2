package data;
import java.util.List;

public class OrderModel {
    private List<String> ingredients;

    public OrderModel(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getIngredients() {
        return ingredients;
    }
}
