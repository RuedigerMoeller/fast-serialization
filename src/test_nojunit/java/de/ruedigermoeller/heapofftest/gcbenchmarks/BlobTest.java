package de.ruedigermoeller.heapofftest.gcbenchmarks;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 28.07.13
 * Time: 23:26
 * To change this template use File | Settings | File Templates.
 */
public class BlobTest {

    static ArrayList blobs = new ArrayList();
    static ArrayList nonblobs = new ArrayList();
    static Object randomStuff[] = new Object[300000];

    public static void main( String arg[] ) throws IOException {
        if ( arg.length > 0 ) {
            parse();
            return;
        }
        if ( Runtime.getRuntime().maxMemory() > 2*1024*1024*1024l) { // 'autodetect' testcase with blobs from mem settings
            int blobGB = (int) (Runtime.getRuntime().maxMemory()/(1024*1024*1024l))-2;
            System.out.println("Allocating "+blobGB*32+" 32Mb blobs ... (="+blobGB+"Gb) ");
            for (int i = 0; i < blobGB*32; i++) {
                blobs.add(new byte[32*1024*1024]);
            }
//            for ( int i = 0; i < blobGB*2700000; i++ )
//                nonblobs.add(new Object[] {new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle(),new Rectangle()});
            System.gc(); // force VM to adapt ..
        }
        // create eden collected tmps with a medium promotion rate (promotion rate can be adjusted by size of randomStuff[])
        while( true ) {
            randomStuff[((int) (Math.random() * randomStuff.length))] = new Rectangle();
        }

    }

    public static void parse() throws IOException {
        DataInputStream din = new DataInputStream(new FileInputStream("f:\\gc.txt"));
        String line = null;
        double minMinor = 999999, maxMinor = 0, avgMinor = 0;
        double minMajor = 999999, maxMajor = 0, avgMajor = 0;
        int cnt = 0,mcount=0;
        while ( (line=din.readLine()) != null ) {
            if ( line.startsWith("[GC") && line.indexOf(",") >= 0 ) {
                line = line.substring(0, line.length()-" secs]".length());
                line = line.substring(line.lastIndexOf(" "));
                double tim = Double.valueOf(line);
                minMinor = Math.min(tim,minMinor);
                maxMinor = Math.max(tim,maxMinor);
                avgMinor +=tim;
                cnt++;
            }
            if ( line.startsWith("[Full GC") ) {
                line = line.substring(line.indexOf(", ")+2);
                line = line.substring(0, line.indexOf(" "));
                double tim = Double.valueOf(line);
                minMajor = Math.min(tim,minMajor);
                maxMajor = Math.max(tim,maxMajor);
                avgMajor +=tim;
                mcount++;
            }
        }
        System.out.println("min, max, avg");
        System.out.println(minMinor+"\t"+maxMinor+"\t"+avgMinor/cnt);
        System.out.println(minMajor+"\t"+maxMajor+"\t"+avgMajor/mcount);
        System.out.println("minor "+cnt+" major "+mcount);
    }

}
