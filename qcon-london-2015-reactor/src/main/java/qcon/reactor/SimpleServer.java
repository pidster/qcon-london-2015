package qcon.reactor;

import reactor.Environment;
import reactor.io.buffer.Buffer;
import reactor.io.net.NetStreams;
import reactor.io.net.tcp.TcpServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author swilliams
 */
public class SimpleServer {

    private TcpServer<Buffer, Buffer> tcpServer;

    @PostConstruct
    public void init() throws InterruptedException {

        Environment env = Environment.initializeIfEmpty();

        this.tcpServer = NetStreams.tcpServer(s -> s
                .listen("localhost", 4003)
                .env(env)
                .synchronousDispatcher()
        );

        tcpServer
                .log("log server")
                .consume(c -> c
                    .log("log channel")
                    .consume(buf -> buf.createView().get())
                );

        tcpServer.start().await(5L, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (tcpServer == null) {
            return;
        }

        tcpServer.shutdown().await(5L, TimeUnit.SECONDS);
    }

}
