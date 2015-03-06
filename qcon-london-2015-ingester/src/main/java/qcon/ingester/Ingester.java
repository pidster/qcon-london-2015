package qcon.ingester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.support.MutableMessageBuilderFactory;
import org.springframework.messaging.Message;
import reactor.Environment;
import reactor.io.buffer.Buffer;
import reactor.io.codec.Codec;
import reactor.io.codec.LengthFieldCodec;
import reactor.io.codec.StandardCodecs;


/**
 * @author pidster
 */
@Configuration
@EnableAutoConfiguration
@EnableIntegration
public class Ingester {

    public static void main(String[] args) {
        SpringApplication.run(Ingester.class, args);
    }

    @Bean
    public static Environment environment() {
        return Environment.initializeIfEmpty();
    }

    @Bean
    public MessageBuilderFactory messageBuilderFactory() {
        return new MutableMessageBuilderFactory();
    }

    @Bean
    public TcpInboundAdapter inboundAdapter(
                Environment environment,
                Codec<Buffer, byte[], byte[]> codec,
                DirectChannel input) {

        // defines the inbound TCP Server
        return new TcpInboundAdapter(environment, codec, input);
    }

    @Bean
    public Codec<Buffer, byte[], byte[]> codec() {
        return new LengthFieldCodec<>(StandardCodecs.BYTE_ARRAY_CODEC);
    }

    @Bean
    public Function<Message<byte[]>, Object> routingKey() {
        // TODO object key extraction
        return bytes -> null;
    }

    @Bean
    public AdaptiveBatcher batcher(Environment environment) {

        // defines the outbound batcher
        return new AdaptiveBatcher(environment, data -> {
            // TODO consumer of byte batches
        });
    }

    @Bean
    public IntegrationFlow flow(
                DirectChannel input,
                Function<Message<byte[]>, Object> routingKey,
                AdaptiveBatcher batcher) {

        // defines the Integration flow graph
        return IntegrationFlows
                .from(input)
                .enrich(spec -> spec.headerFunction("routingKey", routingKey))
                .filter("true")
                .handle(batcher)
                .get();
    }

}
