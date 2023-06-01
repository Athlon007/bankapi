Feature: Everything to do with users API


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

  Scenario: Registering a new employee with no role should result in 401
    Given I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678", birth-date "2000-09-08", and role "employee"
    Then I get HTTP status 401

  Scenario: Registering a new employee with invalid role should result in 400
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    And I call the application register endpoint with username "johhny", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678", birth-date "2000-09-08", and role "ducky"
    Then I get HTTP status 400



  Scenario: Updating user as admin should return 200 and updated UserResponse object
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    And I update user with id "1" with username "admin", first name "John", last name "Doe", email "mail@example.com", password "Password1!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
    Then I get HTTP status 200
    And I get a user with first name "John" and last name "Doe"
    And The user has role of "admin"

  Scenario: Updating other user as admin should return 200 and updated UserResponse object
      Given I have a valid login credentials
      And I call the application login endpoint
      And I receive a token
      And I update user with id "3" with username "client", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 200
      And I get a user with first name "John" and last name "Doe"
      And The user has role of "user"

  Scenario: Updating non existent user should return 404
      Given I have a valid login credentials
      And I call the application login endpoint
      And I receive a token
      And I update user with id "99" with username "client", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 404

  Scenario: Updating own user as user should return 200 and updated UserResponse object
      Given I have a valid user login credentials
      And I call the application login endpoint
      And I receive a token
      And I update user with id "3" with username "client", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 200
      And I get a user with first name "John" and last name "Doe"

  Scenario: Updating other user as user should return 401
      Given I have a valid user login credentials
      And I call the application login endpoint
      And I receive a token
      And I update user with id "1" with username "admin", first name "John", last name "Doe", email "mail@example.com", password "Password1!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 401

  Scenario: Updating username to already existing username should return 400
      Given I have a valid login credentials
      And I call the application login endpoint
      And I receive a token
      And I update user with id "3" with username "employee", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 400

  Scenario: Updating email to already existing email should return 400
      Given I have a valid login credentials
      And I call the application login endpoint
      And I receive a token
      And I update user with id "3" with username "client", first name "John", last name "Doe", email "admin@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
      Then I get HTTP status 400
    
  Scenario: Updating an admin as an employee should return 401
        Given I have a valid employee login credentials
        And I call the application login endpoint
        And I receive a token
        And I update user with id "1" with username "admin", first name "John", last name "Doe", email "mail@example.com", password "Password1!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
        Then I get HTTP status 401

  Scenario: Updating other user's role as employee should return 401
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    And I update user with id "3" with username "client", first name "John", last name "Doe", email "mail@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08" and role "admin"
    Then I get HTTP status 401

