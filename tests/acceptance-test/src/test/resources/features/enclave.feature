Feature: Enclave as a service

    Scenario: Enclave deployed as a remote service 
    Given enclave is running
    And tessera nodes are running
    When quorum node invokes send
    Then tessera nodes forwards transaction to peer 
