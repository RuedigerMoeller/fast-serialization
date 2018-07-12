package ser.serializers;

import org.junit.Assert;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author ykalemi
 * @since 28.04.18
 */
public class FSTStreamEncoderTest {

    @Test
    public void newConfigWontUseRegisteredClassFromPrevious() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        for (int i = 0; i < 2; i++) {

            final int number = i;

            Thread t = new Thread() {

                @Override
                public void run() {

                    FSTConfiguration cacheConfig = FSTConfiguration.createDefaultConfiguration();
                    cacheConfig.setClassLoader(Thread.currentThread().getContextClassLoader());

                    if (number == 0) {
                        cacheConfig.registerClass(Timestamp.class);
                    }

                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    byte[] data = cacheConfig.asByteArray(ts);

                    Timestamp deserialized = (Timestamp) cacheConfig.asObject(data);

                    Assert.assertEquals(ts, deserialized);
                }
            };

            Future<?> f = executor.submit(t);
            boolean failed = true;
            try {
                f.get();
                failed = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Assert.assertFalse(failed);
        }
    }
}
