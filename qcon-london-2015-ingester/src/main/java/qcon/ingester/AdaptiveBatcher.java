package qcon.ingester;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import reactor.Environment;
import reactor.function.Consumer;
import reactor.rx.Streams;
import reactor.rx.broadcast.Broadcaster;

import java.util.concurrent.atomic.AtomicLong;


/**
 * @author swilliams
 */
public class AdaptiveBatcher extends AbstractMessageHandler {

    private static final Long SIGNAL = 0L;

    private final Consumer<byte[]> consumer;

    private final Broadcaster<byte[]> broadcaster;

    private final Broadcaster<Long> adaptive;

    private final AtomicLong counter = new AtomicLong(0L);

    @Value("${batch.initialSize:1024}")
    long initBatchSize;

    @Value("${batch.minSize:1}")
    long minBatchSize;

    @Value("${batch.maxSize:8192}")
    long maxBatchSize;

    @Value("${batch.timespan:1000L}")
    long maxTimespan;

    /**
     * @param environment to use
     */
    public AdaptiveBatcher(Environment environment, Consumer<byte[]> consumer) {

        this.broadcaster = Broadcaster.create(environment);
        this.adaptive = Broadcaster.create(environment);

        this.consumer = consumer;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        byte[] bytes = message.getHeaders().get("bytes", byte[].class);
        broadcaster.onNext(bytes);
    }

    @Override
    protected void onInit() throws Exception {

        broadcaster
                .buffer(() -> Streams.timer(maxTimespan).mergeWith(adaptive))
                .capacity(1L)
                .batchConsume(list -> {

                    // example of recombining byte arrays
                    int size = 0;
                    for (byte[] item : list) {
                        size += item.length;
                    }

                    byte[] data = new byte[size];
                    int position = 0;
                    for (byte[] item : list) {
                        System.arraycopy(item, 0, data, position, item.length);
                        position += item.length;
                    }

                    // Record time taken
                    long timestamp = currentTime();
                    consumer.accept(data);
                    recordTime(timestamp);

                }, previous -> {

                    counter.incrementAndGet();

                    if (counter.compareAndSet(1024, 0)) {
                        adaptive.onNext(SIGNAL);
                    }

                    // TODO calculate new batch size
                    return calculateNextBatchSize(previous);
                });

        // control.start();
    }

    private long currentTime() {
        return System.nanoTime();
    }

    private void recordTime(long timestamp) {
        // TODO record time
        long diff = System.nanoTime() - timestamp;
    }

    private long calculateNextBatchSize(long previous) {
        long calculated = previous;

        if (calculated < minBatchSize) {
            return minBatchSize;
        }
        if (calculated > maxBatchSize) {
            return maxBatchSize;
        }
        return calculated;
    }

}
