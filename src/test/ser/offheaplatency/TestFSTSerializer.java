package ser.offheaplatency;

import java.io.*;
import java.util.concurrent.*;

import org.HdrHistogram.*;
import org.nustaq.serialization.simpleapi.*;



public final class TestFSTSerializer {


    protected static void printResult( Histogram histogram ){
        System.out.println( "\nDetail Result (in micros)");
        System.out.println( "------------------------------------------------------------------");

        histogram.outputPercentileDistribution( System.out, 1000.0 );
        double valueAt99Percentile  = histogram.getValueAtPercentile( 99.99d );

        System.out.println( "\nValue 99.99th percentile >> " + valueAt99Percentile/1000.0 );
    }


    protected static MyFSTSerializer createFSTSerializer(  boolean toStore, int eventCount, int memorySizeOf1Object ) throws Exception{

        long expectedMemory     = memorySizeOf1Object * eventCount;
//        String fileLocation     = "C:\\Temp";
        String fileLocation     = "/tmp";
        String journalName      = "Test";
        MyFSTSerializer ser     = new MyFSTSerializer( toStore, fileLocation, journalName, new DefaultCoder(), expectedMemory, eventCount );

        return ser;
    }


    protected static void destroyFSTSerializer( MyFSTSerializer serializer ){

        if( serializer != null ){
            serializer.stop();
            boolean deleted = new File( serializer.getFilename() ).delete();
            if( deleted ){
                System.out.println( "Deleted file from " +  serializer.getFilename());
            }else{
//                throw new RuntimeException( "TEST FAILED as we failed to delete file " + serializer.getFilename() );
            }
        }

    }


    public static void testOffHeapPersistence( ){

        MyFSTSerializer serializer= null;

        try{

            int eventCount          = 50000;
            int memorySizeOf1Object = 1000;
            Histogram  histogram    = new Histogram( TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS), 2);

            System.out.println( "Testing off heap persistence performance of FSTLongOffheapMap by storing " + eventCount + " events.");
            serializer              = createFSTSerializer( true, eventCount, memorySizeOf1Object );
            serializer.start( );

            for( int i =0; i<eventCount; i++ ){

                MktDataEvent event = new MktDataEvent( "EDM6", 99.0, (100 + i), 99.50, (200 + i) );
                serializer.storeEvent( event );
                histogram.recordValue(System.nanoTime() - event.getCreationTime() );

            }

            int retrievedEventSize  = serializer.retrieveAllEvents().size();
            if( eventCount != retrievedEventSize )
                throw new RuntimeException("Store failed as we stored " + eventCount + " events but retrieved " + retrievedEventSize );


            printResult( histogram );

        }catch( Exception e ){
            throw new RuntimeException("TEST FAILED as ", e);

        }finally{
            destroyFSTSerializer( serializer );
        }

    }



    public static void testDispatchAndPersistence( boolean toStore ) throws Exception{

        int eventCount                  = 50000;
        int memorySizeOf1Object         = 1000;

        DummyListener listener          = new DummyListener( );
        MyFSTSerializer serializer      = createFSTSerializer( toStore, eventCount, memorySizeOf1Object );
        MktDataDispatcher dispatcher    = new MktDataDispatcher( eventCount, serializer, listener );

        if( toStore ){
            System.out.println( "Testing off heap persistence with dispathcer performance of FSTLongOffheapMap by storing " + eventCount + " events.");
        }else{
            System.out.println( "Testing off heap persistence with dispathcer performance of FSTLongOffheapMap WITHOUT storing " + eventCount + " events.");
        }

        dispatcher.start();
        Thread.sleep( 3000 );

        for( int i = 0; i< eventCount; i++ ){
            MktDataEvent event = new MktDataEvent( "EDM6", 99.0, (100 + i), 99.50, (200 + i) );
            dispatcher.enqueue( event );
            long nanos = System.nanoTime();
            while( System.nanoTime() - nanos < 3000 )
                Thread.yield();

        }

        //Let the listener get all the elements
        while( (dispatcher.getQueueSize() != 0) ){
            Thread.sleep(100);
        }

        Thread.sleep( 2000 );
        dispatcher.stop();
        listener.generateLatencyStats();
        destroyFSTSerializer( serializer );

    }


    public static class DummyListener implements MktDataDispatcher.MktDataListener {

        private final Histogram histogram;

        public DummyListener( ){
            this.histogram  = new Histogram( TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS), 2);
        }


        @Override
        public final boolean update( MktDataEvent event ){
            histogram.recordValue( (System.nanoTime() - event.getCreationTime()) );
            return true;
        }


        public final void generateLatencyStats( ){

            histogram.outputPercentileDistribution( System.out, 1000.0 );
            double valueAt99Percentile  = histogram.getValueAtPercentile( 99.99d );
            System.out.println( "\nValue at 99.99th percentile (micros) >> " + valueAt99Percentile/1000.0 );

        }

    }

    public static void main( String ... args ) throws Exception{

        for (int i = 0; i < 1000; i++) {
            System.gc();
            Thread.sleep( 2000 );
            System.out.println("start test ==>");
            testDispatchAndPersistence( true );
//            testOffHeapPersistence();
        }

//        System.gc();
//        Thread.sleep( 2000 );
//        testDispatchAndPersistence( true );
//        testDispatchAndPersistence( true );
//        testDispatchAndPersistence( true );

    }

}