package ser.offheaplatency;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ruedi on 19/06/15.
 */
public final class MktDataEvent implements Serializable {

    private final long sequenceId;
    private final long creationTime;
    private final String symbol;
    private final double bidPrice;
    private final long bidQuantity;
    private final double askPrice;
    private final long askQuantity;

    private final static long serialVersionUID = 1L;
    private final static AtomicLong SEQUENCE    = new AtomicLong();

    public MktDataEvent(String symbol, double bidPrice, long bidQuantity, double askPrice, long askQuantity){

        this.creationTime   = System.nanoTime( );
        this.sequenceId     = SEQUENCE.incrementAndGet();
        this.symbol         = symbol;
        this.bidPrice       = bidPrice;
        this.bidQuantity    = bidQuantity;
        this.askPrice       = askPrice;
        this.askQuantity    = askQuantity;
    }

       public final long getSequenceId( ){
        return sequenceId;
    }

    public final long getCreationTime( ){
        return creationTime;
    }

    public final String getSymbol(){
        return symbol;
    }

    public final double getBidPrice( ){
        return bidPrice;
    }

    public final long getBidQuantity( ){
        return bidQuantity;
    }

    public final double getAskPrice( ){
        return askPrice;
    }

    public final long getAskQuantity( ){
        return askQuantity;
    }
}