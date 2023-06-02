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
    Given I call the application accounts end point with IBAN "NL09RABO9091319364", currencyType "EURO", accountType "SAVING", userId 3
    Then I get HTTP status 201
    And I get an account's IBAN "NL09RABO9091319364" and currencyType "EURO" and accountType "SAVING" and id 3


  Scenario: Get accounts by user id without employee or admin credentials should result in 401
    When I call the application accounts end point with user id 3
    Then I get HTTP status 401

  Scenario: Creating an current account to a user who already have a current account should result in 400
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application accounts end point with IBAN "NL60INHO9935031745", currencyType "EURO", accountType "CURRENT", userId 3
    Then I get HTTP status 400

  Scenario: Creating an account with invalid IBAN should result in 400
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application accounts end point with IBAN "111", currencyType "EURO", accountType "SAVING", userId 3
    Then I get HTTP status 400

    Scenario: Creating an account with invalid accountType should result in 400
    Given I have a valid employee login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application accounts end point with IBAN "NL60INHO9935031745", currencyType "EURO", accountType "SAVING-CURRENT", userId 3
    Then I get HTTP status 400


