Feature: Everything to do with user authentication

  Scenario: Login with correct credentials
    Given The endpoint for "/auth/login" is available for method "POST"
    And I get HTTP status 200
    When I login with username "admin" and password "Password1!"
    Then I get HTTP status 200