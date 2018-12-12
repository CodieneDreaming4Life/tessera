
package enclave;

import cucumber.api.java8.En;
import static org.junit.Assert.fail;


public class PeerRecieveTransaction implements En {

    public PeerRecieveTransaction() {
        Then("tessera nodes forwards transaction to peer", () -> {
            fail();
            // Write code here that turns the phrase above into concrete actions
            throw new cucumber.api.PendingException();
        });
    }

}
