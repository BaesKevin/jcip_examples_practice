package practice.blocking_interrupting;

import org.junit.Test;
import practice.SlowRandomNumberConsumer;
import practice.SlowRandomNumberGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SlowRandomNumberConsumerTest {

    @Test
    public void fetchNumbersSync() throws InterruptedException {
        SlowRandomNumberGenerator slowRandomNumberGenerator = new SlowRandomNumberGenerator(1);
        SlowRandomNumberConsumer slowRandomNumberConsumer = new SlowRandomNumberConsumer(slowRandomNumberGenerator);

        List<Integer> integers = slowRandomNumberConsumer.fetchNumbersSync();

        assertThat(integers).hasSize(10);
    }

    @Test
    public void fetchNumbersConcurrent() throws InterruptedException {
        SlowRandomNumberGenerator slowRandomNumberGenerator = new SlowRandomNumberGenerator(1);
        SlowRandomNumberConsumer slowRandomNumberConsumer = new SlowRandomNumberConsumer(slowRandomNumberGenerator);

        List<Integer> integers = slowRandomNumberConsumer.fetchNumbersConcurrent();

        assertThat(integers).hasSize(10);
    }
}