Feature: User Limits PUT requests

  Scenario: Endpoint check
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When The endpoint for "/users/3/limits" is available for method "PUT"
    Then I get HTTP status 200

  Scenario: User updating their limits results in 401
    Given I have a valid user login credentials
    And I call the application login endpoint
    And I receive a token
    When I update user id 3 limits to transaction limit 1000, daily limit 100
    Then I get HTTP status 401

  Scenario: Employee updating other users limit results in valid response
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    When I update user id 3 limits to transaction limit 1000, daily limit 400
    Then I get HTTP status 200
    And I get a response with the following body with transaction limit 1000, daily limit 400, absolute limit 0 and remaining daily limit 100

  Scenario: Setting transaction limit to negative number results in 400
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    When I update user id 3 limits to transaction limit -1000, daily limit 400
    Then I get HTTP status 400

  Scenario: Setting daily limit to negative number results in 400
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    When I update user id 3 limits to transaction limit 1000, daily limit -400
    Then I get HTTP status 400