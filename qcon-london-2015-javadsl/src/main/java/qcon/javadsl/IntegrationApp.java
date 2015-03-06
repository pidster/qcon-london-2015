package qcon.javadsl;

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
import org.springframework.messaging.MessageChannel;
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
public class IntegrationApp {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApp.class, args);
    }

    @Bean
    public IntegrationFlow flow(MessageChannel input) {

        // defines the Integration flow graph
        // Looks a bit like a MicroService!
        return IntegrationFlows
                .from(input)
                .enrich(spec -> spec.headerFunction("routingKey", f -> "eep!"))
                .filter("true")
                .handle(System.out::println)
                .get();
    }



}
