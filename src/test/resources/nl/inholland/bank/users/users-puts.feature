Feature: PUT requests to User endpoint
# NOTE: Those must be run one-by-one instead all together. We don't reset database between tests.

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
      And The user has role of "customer"

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

