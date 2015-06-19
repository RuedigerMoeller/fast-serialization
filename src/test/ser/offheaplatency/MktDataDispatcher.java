package ser.offheaplatency;

import java.util.*;
import java.util.concurrent.*;

public final class MktDataDispatcher implements Runnable{

    private volatile boolean keepDispatching;
    private final ExecutorService service;
    private final MyFSTSerializer serializer;
    private final MktDataListener listener;
    private final AbstractQueue<MktDataEvent> eventQueue;

    public MktDataDispatcher( int queueSize, MyFSTSerializer serializer, MktDataListener listener ){
        this.serializer     = serializer;
        this.listener       = listener;
        this.eventQueue = new ArrayBlockingQueue<MktDataEvent>( queueSize );
        this.service        = Executors.newFixedThreadPool(  1 );
    }

    public final void start( ){
        serializer.start( );
        keepDispatching = true;
        service.execute( this );
    }

    public final boolean enqueue( final MktDataEvent event ){
        return eventQueue.offer( event );
    }

    @Override
    public final void run( ){

        while( keepDispatching ){

            try{
                MktDataEvent event  = eventQueue.poll();
                if( event == null ){
//                    Thread.yield();
                    continue;
                }

                if( serializer.toStore() ){
                    serializer.storeEvent( event );
                }
                listener.update( event );

            }catch( Exception e ){
                e.printStackTrace( );
            }
            }
        }

    protected final int getQueueSize( ){
        return eventQueue.size( );
    }

    public final void stop(){
        serializer.stop( );
        keepDispatching = false;
        service.shutdown();
    }

    public interface MktDataListener{
        public boolean update( MktDataEvent event );
    }

}