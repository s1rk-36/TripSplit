## Task List

| #                                         | Task                                                           | Est. (hrs) | Actual (hrs) |
|-------------------------------------------|----------------------------------------------------------------|------------|--------------|
| **Models & Database**                     |
| 1                                         | Define models: User, Group, Expense, Receipt, Comment          | 1          |              |
| 2                                         | Create SQL schema and initialize MySQL database (with Docker)  | 2          |              |
| 3                                         | Seed test data for development                                 | 1          |              |
| **Backend (Spring Boot + JDBC Template)** |
| 4                                         | Set up Spring Boot project structure                           | 1          |              |
| 5                                         | Implement User repository (JDBC)                               | 2          |              |
| 6                                         | Implement Group repository (JDBC)                              | 2          |              |
| 7                                         | Implement Expense repository (JDBC)                            | 2          |              |
| 8                                         | Implement Receipt repository (JDBC)                            | 2          |              |
|                                           | Implement Comment repository (JDBC)                            | 2          |              |
| 9                                         | Test CRUD operations for the repositories                      | 3          |              |
| 10                                        | Implement services for User, Group, Expense, Receipt, Comment  | 4          |              |
| 11                                        | Add validation logic to services                               | 2          |              |
| 12                                        | Test service logic                                             | 2          |              |
| 13                                        | Implement REST controllers for each model                      | 4          |              |
| 14                                        | Test API endpoints with REST Client                            | 2          |              |
| **Frontend (React)**                      |
| 15                                        | Set up React project                                           | 1          |              |
| 16                                        | Create UI for User CRUD                                        | 3          |              |
| 17                                        | Create UI for Group CRUD                                       | 3          |              |
| 18                                        | Create UI for Expense CRUD                                     | 3          |              |
| 19                                        | Create UI for Receipt upload/view                              | 3          |              |
| 20                                        | Connect React frontend to backend API                          | 3          |              |
| 21                                        | Test integration of frontend with backend                      | 2          |              |
| 22                                        | Add form validation and error handling                         | 2          |              |
| 23                                        | Polish UI/UX with Bootstrap and CSS                            | 2          |              |
| **Stretch Goal (AWS S3)**                 |
| 24                                        | Implement receipt image upload to expenses using AWS S3        | 4          |              |
| 25                                        | Integrate AWS SDK into Spring Boot backend                     | 4          |              |
| 27                                        | Create frontend UI for uploading and displaying receipt images | 4          |              |

## Class Details

### App

* `public static void main(String[])` — Entry point of the application. Instantiates and wires dependencies, then starts the application via the controller layer.

### data.DataException

Custom exception for data access layer.

* `public DataException(String, Throwable)` — Constructs the exception with a message and root cause.

### data.UserJdbcTemplateRepository

* `private JdbcTemplate jdbcTemplate` — For executing SQL.
* `RowMapper<User>` — Maps SQL result set to `User` objects.
* `public List<User> findAll()` — Retrieves all users from the database.
* `public User findById(int)` — Retrieves a user by their ID.
* `public User add(User)` — Inserts a new user into the database.
* `public boolean update(User)` — Updates an existing user.
* `public boolean deleteById(int)` — Deletes a user by ID.

### data.GroupJdbcTemplateRepository

* Same structure and method descriptions as `UserJdbcTemplateRepository`, but for `Group`.

### data.ExpenseJdbcTemplateRepository

* Same structure and method descriptions as `UserJdbcTemplateRepository`, but for `Expense`.

### data.ReceiptJdbcTemplateRepository

* Same structure and method descriptions as `UserJdbcTemplateRepository`, but for `Receipt`.

### data.CommentJdbcTemplateRepository

* Same structure and method descriptions as `UserJdbcTemplateRepository`, but for `Comment`.

### Repository Interfaces (User, Group, Expense, Receipt, Comment)

Defines the contract that the respective JdbcTemplate repositories implement:

* `List<T> findAll()` — Retrieves all entities from the database.
* `T findById(int)` — Retrieves an entity by its ID.
* `T add(T)` — Adds a new entity to the database.
* `boolean update(T)` — Updates an existing entity.
* `boolean deleteById(int)` — Deletes an entity by its ID.

### domain.Result

* `private ArrayList<String> messages` — Holds error messages for validation or process failures.
* `private T payload` — Data returned from service layer.
* `private ResultType type` — Indicates result status (success, invalid input, not found, etc).
* `public ResultType getType()` — Returns result type.
* `public boolean isSuccess()` — Returns true if result is successful.
* `public T getPayload()` — Gets the payload.
* `public void setPayload(T)` — Sets the payload.
* `public void addMessage(String, ResultType)` — Adds a message and sets the result type. Used to report validation or process failures.

### domain.ResultType

Enum: `SUCCESS`, `INVALID`, `NOT_FOUND` — Represents result status.

### domain.service (User, Group, Expense, Receipt)

Business logic layer for each model.

* Repositories are injected — Constructor-based dependency injection of repositories.
* `findAll()` — Returns all entities from the database.
* `findById(int)` — Gets one entity by its ID.
* `add(T)` — Validates and adds an entity to the database.
* `update(T)` — Validates and updates an entity.
* `deleteById(int)` — Deletes an entity by its ID.
* `validate(T)` — Ensures data integrity before processing.

### models.User

* Fields: `id`, `firstName`, `lastName`, `email`, `username`, `passwordHash`

    * `id` — Unique identifier for the user.
    * `firstName`, `lastName` — User's personal names.
    * `email` — User's email address for communication.
    * `username` — Login credential name.
    * `passwordHash` — Securely hashed user password.
* Getters/setters, `equals`, and `hashCode` implemented.

### models.Group

* Fields: `id`, `name`, `description`, `createdBy (User)`

    * `id` — Unique identifier for the group.
    * `name` — Name of the group.
    * `description` — Brief explanation of the group's purpose.
    * `createdBy` — User who created the group.
* Getters/setters, `equals`, and `hashCode` implemented.

### models.Expense

* Fields: `id`, `name`, `BigDecimal totalCost`, `category`, `description`, `LocalDateTime createdAt`, `User createdBy`, `Group group`

    * `id` — Unique identifier for the expense.
    * `name` — Title or name of the expense.
    * `totalCost` — The total cost represented using BigDecimal for monetary accuracy.
    * `category` — Classification of the expense.
    * `description` — Optional details about the expense.
    * `createdAt` — Timestamp of when the expense was logged.
    * `createdBy` — User who recorded the expense.
    * `group` — Group associated with the expense.
* Getters/setters, `equals`, and `hashCode` implemented.

### models.Receipt

* Fields: `id`, `String imageURL`, `LocalDateTime uploadedAt`, `Expense expense`

    * `id` — Unique identifier for the receipt.
    * `imageURL` — Link to the uploaded image stored (e.g., in AWS S3).
    * `uploadedAt` — Timestamp when the image was uploaded.
    * `expense` — The associated expense this receipt belongs to.
* Getters/setters, `equals`, and `hashCode` implemented.

### models.Comment

* Fields: `id`, `LocalDateTime timestamp`, `String comment`, `User createdBy`, `Expense expense`

    * `id` — Unique identifier for the comment.
    * `timestamp` — Time when the comment was made.
    * `comment` — The textual content of the comment.
    * `createdBy` — User who wrote the comment.
    * `expense` — The expense the comment is related to.
* Getters/setters, `equals`, and `hashCode` implemented.

### controllers (User, Group, Expense, Receipt)

REST endpoints for CRUD operations.

* Injects respective service — Uses dependency injection to connect the service layer.
* `findAll()` — GET all entities.
* `findById(int)` — GET a single entity by its ID.
* `add(T)` — POST to create a new entity.
* `update(int, T)` — PUT to update an existing entity.
* `deleteById(int)` — DELETE to remove an entity by ID.

### controllers.UserGroupController

* Handles user-group relationships.
* `add(UserGroup)` — Creates new relationship.
* `update(UserGroup)` — Updates relationship.
* `deleteByKey(int, int)` — Deletes using composite key (userId and groupId).

### controllers.UserExpenseController

* Handles user-expense relationships.
* `add(UserExpense)` — Creates new relationship.
* `update(UserExpense)` — Updates relationship.
* `deleteByKey(int, int)` — Deletes using composite key (userId and expenseId).

### controllers.ErrorResponse

* Represents error details returned from API.
* `message`, `timestamp`, constructor, and getters — Contains the error message and time it occurred.
* `build(Result<T>)` — Creates a `ResponseEntity` from a failed `Result` for structured error response.




