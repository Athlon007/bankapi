Feature: Everything associated with the Account

  Scenario: Endpoint check
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application accounts endpoint with user id 3
    Then I get HTTP status 200
    And I get 1 elements in the list

  Scenario: Create an account as employee/admin
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application accounts end point with IBAN "NL60INHO9935031745", currencyType "EURO", accountType "SAVING", userId 3
    Then I get HTTP status 201
    And I get an account's IBAN "NL60INHO9935031745" and currencyType "EURO" and accountType "SAVING" and id 3


    Scenario: Get accounts by user id without credentials should result in 401
    When I call the application accounts end point with user id 3
    Then I get HTTP status 401

