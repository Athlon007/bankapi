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

    Scenario: Registering a new user as guest
      Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 201
      And I get a user with first name "John" and last name "Doe"

    Scenario: Registering a new user with already existing username should result in 400
      Given I call the application register endpoint with username "client", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 400

    Scenario: Registering a new user with wrong password should result in 400
      Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "password", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 400

    Scenario: Registering a new user with no first name should result in 400
      Given I call the application register endpoint with username "johhny", first name "", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 400

    Scenario: Registering a new user with no username should result in 400
      Given I call the application register endpoint with username "", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 400

    Scenario: Registering a new user with invalid email result in 400
      Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
        Then I get HTTP status 400

    Scenario: Registering a new user with invalid BSN result in 400
      Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "0000", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 400

    Scenario: Registering a new user with invalid phone number result in 400
      Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "123456" and birth-date "2000-09-08"
      Then I get HTTP status 400

    Scenario: Registering a new user with invalid birthdate should result in 400
      Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "banana"
      Then I get HTTP status 400

    Scenario: Registering a new user with no last name should result in a new user
      Given I call the application register endpoint with username "johhny", first name "John", last name "", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 201
      And I get a user with first name "John" and last name ""

    Scenario: Trying to use register endpoint without body should result in 400
      Given I call the application register endpoint with no body
      Then I get HTTP status 400

    Scenario: Register new employee as admin should result in a valid user
      Given I have a valid login credentials
      And I call the application login endpoint
      And I receive a token
      And I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678", birth-date "2000-09-08", and role "employee"
      Then I get HTTP status 201
      And I get a user with first name "John" and last name "Doe"
      And The user has role of "employee"

    Scenario: Registering a new employee as guest should result in 401
      Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678", birth-date "2000-09-08", and role "employee"
      Then I get HTTP status 401

    Scenario: Registering a new employee as employee should result in 401.
        Given I have a valid employee login credentials
        And I call the application login endpoint
        And I receive a token
        And I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678", birth-date "2000-09-08", and role "employee"
        Then I get HTTP status 401