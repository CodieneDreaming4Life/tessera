package enclave;

import cucumber.api.java8.En;
import static org.junit.Assert.*;

public class SendTransaction implements En {

    public SendTransaction() {
        When("quorum node invokes send", () -> {
            fail();
            throw new cucumber.api.PendingException();
        });
    }

}
