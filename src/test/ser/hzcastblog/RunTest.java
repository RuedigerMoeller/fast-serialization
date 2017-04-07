package ser.hzcastblog;

import org.nustaq.serialization.simpleapi.DefaultCoder;

import java.util.Date;
import java.util.Random;

/**
 * Created by ruedi on 24/12/14.
 */
public class RunTest {

    static String[] products;
    static int maxOrderLines = 5;

    static Order createNewOrder(Random random, int id) {
        Order order = new Order();
        order.orderId = random.nextInt(id);
        order.date = new Date();

        int orderlineCount = random.nextInt(maxOrderLines);
        for (int k = 0; k < orderlineCount; k++) {
            OrderLine orderLine = new OrderLine();
            orderLine.amount = random.nextInt(100);
            orderLine.product = products[random.nextInt(products.length)];
            order.orderLines.add(orderLine);
        }

        return order;
    }

    public static void main( String arg[] ) {
        Random r = new Random(1000);
        products = new String[100];
        for (int k = 0; k < 100; k++) {
            products[k] = "product-" + k;
        }

        Order orders[] = new Order[10000];
        for (int i = 0; i < orders.length; i++) {
            orders[i] = createNewOrder(r,i+1);
        }

        int curOrder = 0;
        DefaultCoder coder = new DefaultCoder(true,Order.class,OrderLine.class);
//        DefaultCoder coder = new DefaultCoder(false,Order.class,OrderLine.class); faster, no ref sharing
        byte buf[] = new byte[10000]; // reuse
        // write once for decode test only
        coder.toByteArray(orders[0], buf, 0, buf.length);

        int count = 0;

        while( true ) {
            long tim = System.currentTimeMillis();
            for ( int i = 0; i < 10000000; i++ ) {
                coder.toByteArray(orders[count], buf, 0, buf.length);
//                Object deser = coder.toObject(buf);
            }
            System.out.println("time: "+(System.currentTimeMillis()-tim) );
        }


    }
}
