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
    When I call the delete user endpoint for user 2
    And I call the application users endpoint
    And I get a list of users
    Then I get 2 elements in the list