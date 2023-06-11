Feature: Everything associated with Transactions

  Scenario: Endpoint check
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application transaction endpoint
    Then I get HTTP status 200
    And I get 0 elements in the list

  Scenario: Retrieve transactions by filters (Returns 0 due to no existing transactions)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application transaction endpoint with page 0, limit 1, minAmount 0, maxAmount 1000, startDateTime "26-02-2023 00:00:00", endDateTime "29-03-2023 23:59:59", transactionID 1, ibanSender "NL60INHO9935031775", ibanReceiver "NL71INHO6310134205", userSenderID 1, userReceiverID 2, transactionType "TRANSACTION"
    Then I get HTTP status 200
    And I get 0 elements in the list

  Scenario: Retrieve transactions by limited filters should return 200
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    When I call the application transaction endpoint with page 0, limit 1, minAmount 0, maxAmount 1000, startDateTime "26-02-2023 00:00:00", endDateTime "29-03-2023 00:00:00", transactionID 0, ibanSender "", ibanReceiver "", userSenderID 0, userReceiverID 0, transactionType "TRANSACTION"
    Then I get HTTP status 200
    And I get 0 elements in the list

  Scenario: Create a transaction should result in 404 (Account not found)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create transaction endpoint with userId 1, sender_iban "NL60INHO9935031775", receiver_iban "NL71INHO6310134205", amount 50.00, description "Transferring money"
    Then I get HTTP status 404

  Scenario: Create a transaction should result in 400 (Invalid)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create transaction endpoint with userId 1, sender_iban "", receiver_iban "NL71INHO6310134205", amount 50.00, description "Transferring money"
    Then I get HTTP status 400

  Scenario: Create a transaction should result in 404 (Account not found on sender)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create transaction endpoint with userId 1, sender_iban "NL71INHO6310134205", receiver_iban "", amount 50.00, description "Transferring money"
    Then I get HTTP status 404

  Scenario: Create a transaction should result in 400 (Invalid)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create transaction endpoint with userId 1, sender_iban "", receiver_iban "", amount 50.00, description "Transferring money"
    Then I get HTTP status 400

  Scenario: Create a withdraw should result in 404 (Account not found)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create withdraw endpoint with iban "NL60INHO9935031775", amount 50.00, currencyType "EURO"
    Then I get HTTP status 404

  Scenario: Create a withdraw should result in 400 (Invalid)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create withdraw endpoint with iban "", amount 10, currencyType "EURO"
    Then I get HTTP status 400

  Scenario: Create a withdraw should result in 404 (Account not found)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create withdraw endpoint with iban "NL60INHO9935031775", amount -50.00, currencyType "EURO"
    Then I get HTTP status 404

  Scenario: Create a withdraw should result in 404 (Account not found)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create withdraw endpoint with iban "NL60INHO9935031775", amount -50.00, currencyType ""
    Then I get HTTP status 404

  Scenario: Create a deposit should result in 404 (Account not found)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create deposit endpoint with iban "NL60INHO9935031775", amount 50.00, currencyType "EURO"
    Then I get HTTP status 404

  Scenario: Create a deposit should result in 400 (Invalid)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create deposit endpoint with iban "", amount 50.00, currencyType "EURO"
    Then I get HTTP status 400

  Scenario: Create a deposit should result in 404 (Account not found)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create deposit endpoint with iban "NL60INHO9935031775", amount -50.00, currencyType "EURO"
    Then I get HTTP status 404

  Scenario: Create a deposit should result in 404 (Account not found)
    Given I have a valid login credentials
    And I call the application login endpoint
    And I receive a token
    Given I call the application create deposit endpoint with iban "NL60INHO9935031775", amount -50.00, currencyType ""
    Then I get HTTP status 404