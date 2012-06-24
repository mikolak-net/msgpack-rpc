package org.msgpack.rpc.loop.netty;



import static org.mockito.Mockito.*;
import java.util.UUID;
import java.util.concurrent.Executor;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SocketChannelFactoryFactoryTest extends TestCase {

    private String oldJVM;

    @Before
    public void setUp() {
        oldJVM = SocketChannelFactoryFactory.JVM_KEY;
    }
    
    @After
    public void tearDown() {
        SocketChannelFactoryFactory.JVM_KEY = oldJVM;
    }
    
    @Test
    public void testFactoryCreation() {
        String [] jvmValues = new String[10];
        
        int i = 0;
        jvmValues[i++] = "Dalvik";
        while(i < jvmValues.length) {
            jvmValues[i++] = UUID.randomUUID().toString();
        }
        
        for(String jvmId : jvmValues) {
            SocketChannelFactoryFactory.JVM_KEY = jvmId;
            assertNotNull(new SocketChannelFactoryFactory().createClientFactory(mock(Executor.class),mock(Executor.class)));
            assertNotNull(new SocketChannelFactoryFactory().createServerFactory(mock(Executor.class),mock(Executor.class)));
        }
    }
}
