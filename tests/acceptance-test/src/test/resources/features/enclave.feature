Feature: Enclave as a service

    Scenario: Enclave deployed as a remote service 
    Given tessera node is running
    And enclave is running
    When quorum node invokes send
    Then tessera nodes forwards transaction to peer 
