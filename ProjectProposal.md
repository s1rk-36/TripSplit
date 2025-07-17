# Project Proposal: TripSplit

## Description
- TripSplit will be an app designed to simplify group trip planning by allowing users to create shared spaces where they can manage a group's budget and track shared expenses.
- Each group acts as a collaborative environment where members can view expenses, track payments, and maintain transparency in cost-sharing. 
- The app aims to remove the confusion and hassle of manual expense tracking, especially when multiple people are involved.
- Within each group, members can create, view, update, and delete expenses. Expenses can be categorized (e.g., food, transportation, lodging), and users can add details such as amount, description, who paid, and how the cost should be split among group members.
- Roles:
    - **User**:
      - Can create a group and become a group admin
      - Can join any group using a valid group ID
      - Can perform Create, Read, Update, and Delete (CRUD) operations on expenses
      - Can perform CRUD operations on comments they personally added
    - **Group Admin**:
      - Has access to all user functions
      - Can update the group’s name and description
      - Can add new users to the group
      - Can remove users from the group
    - **Moderator**:
      - Has full access to CRUD operations for Groups, Users, & Comments


## Data

### Users

- **userId** (`int`): unique identifier for the user.
- **firstName** (`String`): the user's given name.
- **lastName** (`String`): the user's family name.
- **email** (`String`, unique): the user's email for login and notifications.
- **username** (`String`, unique): display name or handle.
- **passwordHash** (`String`): hashed version of user’s password.

### Groups

- **groupId** (`int`): unique identifier for the group.
- **name** (`String`): name/title of the group.
- **description** (`String`): short summary of the trip or purpose.
- **createdBy** (`User`): reference to the User who created the group.

### Expenses

- **expenseId** (`int`): unique identifier for the expense.
- **name** (`String`): short title of the expense.
- **totalCost** (`double`): total amount of the expense.
- **category** (`String`): category type (e.g., lodging, food).
- **description** (`String`, optional): extra details.
- **createdAt** (`LocalDateTime`): timestamp of creation.
- **createdBy** (`User`): reference to the user who created the expense.
- **group** (`Group`): reference to the group this expense belongs to.

### Receipts

- **receiptId** (`int`): unique identifier for the receipt.
- **imageURL** (`String`): path to the uploaded receipt image.
- **uploadedAt** (`LocalDateTime`): timestamp of upload.
- **expense** (`Expense`): reference to the expense the receipt belongs to.

### Comments

- **commentId** (`int`): unique identifier for the comment.
- **timestamp** (`LocalDateTime`): when the comment was posted.
- **content** (`String`): comment text.
- **createdBy** (`User`): reference to the user who made the comment.
- **expense** (`Expense`): reference to the related expense.

## Validation

### Users
- **firstName** is required and cannot be blank.
- **lastName** is required and cannot be blank.
- **email** is required, must be a valid email format, and must be unique.
- **username** is required, must be unique, and must be 3–20 characters long using only letters, numbers, and underscores.
- **passwordHash** is required and must represent a valid hashed password.

### Groups
- **name** is required and must be between 3 and 50 characters.
- **description** is optional but must not exceed 255 characters.
- **createdBy** must reference an existing user and cannot be null.

### Expenses
- **name** is required and must be between 3 and 100 characters.
- **totalCost** is required, must be a positive number greater than 0.
- **category** is required and must be one of the following predefined categories: `"food"`, `"lodging"`, `"transportation"`, `"activities"`, or `"miscellaneous"`.
- **description** is optional but must not exceed 255 characters.
- **createdAt** must not be in the future.
- **group** must reference an existing group and cannot be null.
- **createdBy** must reference an existing user and cannot be null.

### Receipts
- **imageURL** is required and must be a valid URL or file path format.
- **uploadedAt** must not be in the future.
- **expense** must reference an existing expense and cannot be null.

### Comments
- **timestamp** must not be in the future.
- **content** is required and must be between 1 and 500 characters.
- **expense** must reference an existing expense and cannot be null.
- **createdBy** must reference an existing user and cannot be null.

## Database Schema

![DatabaseSchema](DatabaseShema.png)

## Package/Class Overview   

```
src
├─── main
│   └─── java
│       └─── learn
│           └─── tripSplit
│               │   App.java                          -- app entry point
│               │
│               ├─── data
│               │   └─── mappers
│               │          UserMapper.java           -- maps SQL rows to User objects
│               │          GroupMapper.java          -- maps SQL rows to Group objects
│               │          ExpenseMapper.java        -- maps SQL rows to Expense objects
│               │          ReceiptMapper.java        -- maps SQL rows to Receipt objects
│               │          CommentMapper.java        -- maps SQL rows to Comment objects
│               │   
│               │   UserJdbcTemplateRepository.java     -- concrete User repository
│               │   UserRepository.java                 -- User repository interface
│               │   GroupJdbcTemplateRepository.java    -- concrete Group repository
│               │   GroupRepository.java                -- Group repository interface
│               │   ExpenseJdbcTemplateRepository.java  -- concrete Expense repository
│               │   ExpenseRepository.java              -- Expense repository interface
│               │   ReceiptJdbcTemplateRepository.java  -- concrete Receipt repository
│               │   ReceiptRepository.java              -- Receipt repository interface
│               │   CommentJdbcTemplateRepository.java  -- concrete Comment repository
│               │   CommentRepository.java              -- Comment repository interface
│
│               ├─── domain
│               │       Result.java                  -- generic result wrapper for operations
│               │       ResultType.java              -- enum for result types (SUCCESS, INVALID, etc.)
│               │       UserService.java             -- business logic for users
│               │       GroupService.java            -- business logic for groups
│               │       ExpenseService.java          -- business logic for expenses
│               │       ReceiptService.java          -- business logic for receipts
│               │       CommentService.java          -- business logic for comments
│
│               ├─── models
│               │       User.java                    -- user model
│               │       Group.java                   -- group model
│               │       Expense.java                 -- expense model
│               │       Receipt.java                 -- receipt model
│               │       Comment.java                 -- comment model  
│               │       UserGroup.java               -- relationship between users and groups
│               │       UserExpense.java             -- relationship between users and expenses
│
│               └─── controllers
│                       ErrorResponse.java           -- standard error message structure
│                       GlobalExceptionHandler.java  -- handles uncaught exceptions and sends proper responses
│                       UserController.java          -- controller for users
│                       GroupController.java         -- controller for groups
│                       ExpenseController.java       -- controller for expenses
│                       ReceiptController.java       -- controller for receipts
│                       CommentController.java       -- controller for comments
│                       UserGroupController.java     -- controller for user-group operations
│                       UserExpenseController.java   -- controller for user-expense operations
│
└─── test
    └─── java
        └─── learn
            └─── tripSplit
                ├─── data
                │       UserJdbcTemplateRepositoryTest.java     -- tests for User repository
                │       GroupJdbcTemplateRepositoryTest.java    -- tests for Group repository
                │       ExpenseJdbcTemplateRepositoryTest.java  -- tests for Expense repository
                │       ReceiptJdbcTemplateRepositoryTest.java  -- tests for Receipt repository
                │       CommentJdbcTemplateRepositoryTest.java  -- tests for Comment repository
                │
                └─── domain
                        UserServiceTest.java            -- tests for UserService
                        GroupServiceTest.java           -- tests for GroupService
                        ExpenseServiceTest.java         -- tests for ExpenseService
                        ReceiptServiceTest.java         -- tests for ReceiptService
                        CommentServiceTest.java         -- tests for CommentService
```

## Class Details

## App

* `public static void main(String[])` — Entry point of the application. Instantiates and wires dependencies, then starts the application via the controller layer.

## Data Layer

### Repositories (`JdbcTemplateRepository` implementations)

Each repository class provides CRUD methods using `JdbcTemplate`.

Common Methods:
- `List<T> findAll()`
- `T findById(int id)`
- `T add(T entity)`
- `boolean update(T entity)`
- `boolean deleteById(int id)`

These classes include the use of their respective `RowMapper` in the `mappers` package.

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

### Mappers

Each class implements `RowMapper<T>` and maps `ResultSet` rows into model objects.

- **UserMapper** — maps SQL rows to `User`
- **GroupMapper** — maps SQL rows to `Group`
- **ExpenseMapper** — maps SQL rows to `Expense`
- **ReceiptMapper** — maps SQL rows to `Receipt`
- **CommentMapper** — maps SQL rows to `Comment`

## Domain Layer

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

## Models Layer

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

## Controllers Layer

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
