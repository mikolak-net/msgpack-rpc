//
// MessagePack-RPC for Java
//
// Copyright (C) 2010-2012 FURUHASHI Sadayuki, Mikołaj Koziarkiewicz
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.rpc.loop.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

/**
 * Factory for various socket channel factory implementations,
 * used to provide the correct implementation under a given JVM.
 */
public final class SocketChannelFactoryFactory {
    /**
     * The JVM key value for the implementation map, generated from
     * the current system properties.
     * 
     * <p>Default visibility for testing purposes.
     */
    static String JVM_KEY;

    /**
     * The map holding the factory implementations w.r.t JVM platform 
     * keys.
     */
    private static final Map<String, FactoryWorker> IMPLEMENTATION_MAP;

    /**
     * The default fallback implementation, if the current platform key
     * is not in the map.
     */
    protected static final FactoryWorker DEFAULT_IMPLEMENTATION;

    static {
        JVM_KEY = System.getProperty("java.vm.name");

        
        //the default
        DEFAULT_IMPLEMENTATION = new FactoryWorker() {

            @Override
            protected ClientSocketChannelFactory createClientFactory(
                    Executor ioExecutor, Executor workerExecutor) {
                return new NioClientSocketChannelFactory(ioExecutor, workerExecutor);
            }

            @Override
            protected ServerSocketChannelFactory createServerFactory(
                    Executor ioExecutor, Executor workerExecutor) {
                return new NioServerSocketChannelFactory(ioExecutor, workerExecutor);
            }


        };
        
        //the implementation map
        IMPLEMENTATION_MAP = new HashMap<String, SocketChannelFactoryFactory.FactoryWorker>();

        //Android
        IMPLEMENTATION_MAP.put("Dalvik", new FactoryWorker() {

            @Override
            protected ClientSocketChannelFactory createClientFactory(
                    Executor ioExecutor, Executor workerExecutor) {
                return new OioClientSocketChannelFactory(workerExecutor);
            }

            @Override
            protected ServerSocketChannelFactory createServerFactory(
                    Executor ioExecutor, Executor workerExecutor) {
                return new OioServerSocketChannelFactory(ioExecutor, workerExecutor);
            }


        });

    }

    /**
     * @return the {@link ClientSocketChannelFactory} appropriate for 
     * the current JVM implementation
     */
    public ClientSocketChannelFactory createClientFactory(Executor ioExecutor, Executor workerExecutor) {
        return (IMPLEMENTATION_MAP.containsKey(JVM_KEY) 
                        ? IMPLEMENTATION_MAP.get(JVM_KEY) 
                        : DEFAULT_IMPLEMENTATION
                ).createClientFactory(ioExecutor, workerExecutor);
    }

    
    /**
     * @return the {@link ServerSocketChannelFactory} appropriate for 
     * the current JVM implementation
     */
    public ServerSocketChannelFactory createServerFactory(Executor ioExecutor, Executor workerExecutor) {
        return (IMPLEMENTATION_MAP.containsKey(JVM_KEY) 
                        ? IMPLEMENTATION_MAP.get(JVM_KEY) 
                        : DEFAULT_IMPLEMENTATION
                ).createServerFactory(ioExecutor, workerExecutor);
    }

    /**
     * Worker for the factory, workaround until 
     * Java 8 and first-class functions come around :p.
     */
    private static abstract class FactoryWorker {

        protected abstract ClientSocketChannelFactory createClientFactory(Executor ioExecutor, Executor workerExecutor);

        protected abstract ServerSocketChannelFactory createServerFactory(Executor ioExecutor, Executor workerExecutor);
    }

}
