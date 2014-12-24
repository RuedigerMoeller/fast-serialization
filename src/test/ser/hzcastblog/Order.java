package ser.hzcastblog;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * from hzcast blog post
 */
public class Order implements Serializable {

    public long orderId;
    public Date date;
    public List<OrderLine> orderLines = new LinkedList<OrderLine>();

}