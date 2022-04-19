Feature: Create Connection

  Scenario: User is able to create SFTP connection
    Given User login into DIL
    When User create "SFTP" connection
    Then Connection is created successfully


