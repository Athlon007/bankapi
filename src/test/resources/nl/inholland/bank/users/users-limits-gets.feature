Feature: Getting user limits
  Scenario: Endpoint check
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When The endpoint for "/users/3/limits" is available for method "GET"
    Then I get HTTP status 200

  Scenario: User can get his own limits
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I get user limits for user 3
    Then I get HTTP status 200
    And I get valid user limits schema

  Scenario: User cannot get other user's limits results in 401
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I get user limits for user 2
    Then I get HTTP status 401

  Scenario: Employee can see other user's limits
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    When I get user limits for user 3
    Then I get HTTP status 200
    And I get valid user limits schema

  Scenario: Getting user limits of user that does not have account results in 405
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I get user limits for user 2
    Then I get HTTP status 405

  Scenario: Admin can see other user's limits
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I get user limits for user 3
    Then I get HTTP status 200
    And I get valid user limits schema

  Scenario: Getting user limits that does not exist results in 404
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I get user limits for user 999
    Then I get HTTP status 404