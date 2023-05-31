Feature: Everything to do with user authentication

  Scenario: Login with correct credentials
    Given I have a valid login credentials
    When I call the application login endpoint
    Then I receive a token

  Scenario: Login with correct username but incorrect password
    Given I have a valid username but invalid password
    When I call the application login endpoint
    Then I receive unauthorized error

  Scenario: Login with incorrect credentials
    Given I have an invalid login credentials
    When I call the application login endpoint
    Then I receive unauthorized error

  Scenario: Refresh token
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application refresh token endpoint
    Then I receive a token

  Scenario: Attempt using same refresh token twice
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    And I keep the refresh token
    And I call the application refresh token endpoint
    When I call the application refresh token endpoint again with old refresh token
    Then I receive unauthorized error