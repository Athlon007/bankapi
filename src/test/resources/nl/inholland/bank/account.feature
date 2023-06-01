Feature: Everything associated with the Account management

  Scenario: Retrieve all accounts for a specific user ID
    Given The endpoint "/accounts/{id}" is available for the "GET" method
    When I send a "GET" request to "/accounts/{id}" with the following parameters:
      | id |
      | {user_id} |
    Then the response status code should be 200
    And the response body should contain a list of accounts
