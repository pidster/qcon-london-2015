package qcon.reactor;

import reactor.Environment;
import reactor.fn.Consumer;
import reactor.rx.broadcast.Broadcaster;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author swilliams
 */
public class SimpleBatcher<T> implements Consumer<T> {

    private final Broadcaster<T> broadcaster;

    private long maxSize = 1024L;

    private long timespan = 5000L;

    public SimpleBatcher(Consumer<List<T>> delegate) {

        Environment env = Environment.initializeIfEmpty();

        this.broadcaster = Broadcaster.create(env);

        broadcaster
                .buffer(maxSize, timespan, TimeUnit.MILLISECONDS)
                .consume(delegate);
    }

    @Override
    public void accept(T item) {
        broadcaster.onNext(item);
    }

    @PreDestroy
    public void destroy() {
        broadcaster.onComplete();
    }

}