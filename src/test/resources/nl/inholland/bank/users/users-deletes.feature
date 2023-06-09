Feature: DELETE features to Users endpoint
  Scenario: Endpoint check
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When The endpoint for "/users/1" is available for method "DELETE"
    Then I get HTTP status 200

  Scenario: Delete user as admin that does not have an account
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    And I call the application users endpoint
    And I get a list of users
    And I get 5 elements in the list
    When I call the delete user endpoint for user 2
    And I call the application users endpoint
    And I get a list of users
    Then I get 4 elements in the list

  Scenario: Delete user as admin that does have an account results in user deactivation
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the delete user endpoint for user 3
    And I request user with id "3"
    Then User is inactive

  Scenario: Reactivating a deleted user
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the delete user endpoint for user 3
    And I request user with id "3"
    Then User is inactive
    When I update user with id "3" with username "client", first name "John", last name "Doe", email "client@example.com", password "Password123!", bsn "318419403", phone number "0612345678" and birth-date "2000-09-08"
    And I request user with id "3"
    Then User is active

  Scenario: Employee deleting Admin results in 401
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the delete user endpoint for user 1
    Then I get HTTP status 401

  Scenario: Client deleting users other than itself results in 401
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the delete user endpoint for user 2
    Then I get HTTP status 401

  Scenario: Client deleting itself results in 200 and deactivated user
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the delete user endpoint for user 3
    Then I get HTTP status 200
    And I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I request user with id "3"
    Then User is inactive

  Scenario: Client deleting/deactivating itself and login again results in 403
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the delete user endpoint for user 3
    Then I get HTTP status 200
    And I have a valid user login credentials
    And I call the application login endpoint
    Then I get HTTP status 403

  Scenario: Deleting not existing user results in 404
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the delete user endpoint for user 999
    Then I get HTTP status 404

  Scenario: Deleting user without login results in 401
    When I call the delete user endpoint for user 3
    Then I get HTTP status 401