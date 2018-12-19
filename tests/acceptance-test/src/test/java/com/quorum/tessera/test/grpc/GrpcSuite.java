package com.quorum.tessera.test.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.CucumberGprcIT;
import com.quorum.tessera.test.ProcessManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SendGrpcIT.class,
    PartyInfoGrpcIT.class,
    TesseraGrpcIT.class,
    CucumberGprcIT.class
})
public class GrpcSuite {
    
    private static final ProcessManager PROCESS_MANAGER = ProcessManager.Builder.create()
            .withCommunicationType(CommunicationType.GRPC)
            .build();
    
    @BeforeClass
    public static void onSetup() throws Exception {
        PROCESS_MANAGER.startNodes();
    }
    
    @AfterClass
    public static void onTearDown() throws Exception {
        PROCESS_MANAGER.stopNodes();
    }
    
}
