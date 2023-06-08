Feature: GET requests on User end-point

  Scenario: Endpoint check
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When The endpoint for "/users" is available for method "GET"
    Then I get HTTP status 200

  Scenario: Get all users as admin
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint
    Then I get HTTP status 200
    And I get a list of users
    And I get 5 elements in the list

  Scenario: Get all users as client
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint
    Then I get HTTP status 200
    And I get a list of users for client

  Scenario: Getting all users without credentials results in 401
    When I call the application users endpoint
    Then I get HTTP status 401

  Scenario: Getting all users with page and limit
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint with page 1 and limit 2
    Then I get HTTP status 200
    And I get 2 elements in the list

  Scenario: Getting all users that have name like "yo mama"
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint with name "yo mama"
    Then I get HTTP status 200
    And I get 1 elements in the list

  Scenario: Getting all users that have no accounts
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint with no accounts
    Then I get HTTP status 200
    And I get 1 elements in the list
    And First element first name is "Goofy"

  Scenario: Getting all users as client with page and limit
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint with page 0 and limit 1
    Then I get HTTP status 200
    And I get 1 elements in the list

  Scenario: Getting all users as client that have name like "Yo Mama"
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint with name "Yo Mama"
    Then I get HTTP status 200
    And I get 1 elements in the list

  Scenario: Getting user by ID should as admin should return 200 and UserResponse object
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user with id "1"
    Then I get HTTP status 200
    And I get a user with first name "Namey" and last name "McNameface"
    And The user has role of "admin"

  Scenario: Getting client as employee should return 200 and UserResponse object
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user with id "3"
    Then I get HTTP status 200
    And I get a user with first name "Yo" and last name "Mama"
    And The user has role of "user"

  Scenario: Client getting his own account should return full UserResponse and not UserForClientResponse
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user with id "3"
    Then I get HTTP status 200
    And I get a user with first name "Yo" and last name "Mama"
    And The user has role of "user"

  Scenario: Client getting someone else account should return UserForClientResponse and not UserResponse
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user with id "2"
    Then I get HTTP status 200
    And I get a user for client with first name "Goofy" and last name "Ahh"
    And Response is kind of UserForClientResponse

  Scenario: Getting user by ID as guest should return 401
    When I request user with id "1"
    Then I get HTTP status 401

  Scenario: Getting user that does not exist should return 404
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user with id "999"
    Then I get HTTP status 404

  Scenario: Getting users with invalid ID should return 400
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user with id "banana"
    Then I get HTTP status 400

  Scenario: Get User 'Bank' by his lastname
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user a user with name "Bank"
    Then I get HTTP status 200
    And I get 1 elements in the list