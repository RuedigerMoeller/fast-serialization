package ser.offheaplatency;

import java.io.*;
import java.util.*;
import org.nustaq.offheap.*;
import org.nustaq.serialization.simpleapi.*;

public final class MyFSTSerializer{

    private final boolean toStore;
    private final String fileName;
    private final long memorySize;
    private final FSTCoder fastCoder;
    private final FSTLongOffheapMap<MktDataEvent> offHeapMap;

    public MyFSTSerializer(  boolean toStore, String location, String journalName, FSTCoder fastCoder, long memorySize, int count ) throws Exception{
        this.toStore        = toStore;
        this.fileName       = location + File.separator + journalName + ".mmf";
        this.memorySize     = memorySize;
        this.fastCoder      = fastCoder;
        this.offHeapMap     = new FSTLongOffheapMap( fileName, memorySize, count, fastCoder );
//        this.offHeapMap     = new FSTLongOffheapMap<>( memorySize, 2*count, fastCoder );
    }

    public final boolean toStore( ){
        return toStore;
    }

    public final String getFilename( ){
        return fileName;
    }

    public final void start( ){
        fastCoder.getConf().setCrossPlatform( false );
        fastCoder.getConf().setPreferSpeed( true );
        fastCoder.getConf().setShareReferences( false );
        fastCoder.getConf().registerClass( Long.class, MktDataEvent.class );
        System.out.println("Journaling started at " + fileName + " with Memory " +  memorySize ) ;
    }

    public final void storeEvent( MktDataEvent event ){
        offHeapMap.put( event.getSequenceId(), event );
    }

    public final Collection<MktDataEvent> retrieveAllEvents( ){
        Map<Long, MktDataEvent> retrievedMap = new LinkedHashMap();

        for( Iterator<MktDataEvent> iterator = offHeapMap.values(); iterator.hasNext(); ){
            MktDataEvent event = (MktDataEvent) iterator.next();
            retrievedMap.put( event.getSequenceId(), event );
         }

        return retrievedMap.values();
    }

    public final void stop( ){
        try{
            offHeapMap.free( );
            System.out.println("Stopped Journal and freed memory." );
        }catch( Exception e ){
            e.printStackTrace( );
        }
    }
}