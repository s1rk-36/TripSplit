package learn.tripsplit.models;

public enum Category {

    LODGING(1, "Lodging"),
    TRAVEL_FEES(2, "Travel Fees"),
    FOOD(3, "Food"),
    TRANSPORTATION(4, "Transportation"),
    ACTIVITIES(5, "Activities"),
    SHOPPING(6, "Shopping"),
    OTHER(7, "Other");

    private final int value;
    private final String name;

    Category(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Category findByName(String name) {
        for (Category category : Category.values()) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        String message = String.format("No Category with name: %s.", name);
        throw new RuntimeException(message);
    }

    public static Category findByValue(int value) {
        for (Category category : Category.values()) {
            if (category.getValue() == value) {
                return category;
            }
        }
        String message = String.format("No Category with value: %s.", value);
        throw new RuntimeException(message);
    }

}
