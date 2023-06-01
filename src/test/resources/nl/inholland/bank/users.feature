Feature: Everything to do with users API

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
    And I get 3 elements in the list

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
    And I get 1 elements in the list

  Scenario: Getting all users that have name like "o"
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application users endpoint with name "o"
    Then I get HTTP status 200
    And I get 2 elements in the list

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