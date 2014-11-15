package ser;

import com.cedarsoftware.util.DeepEquals;
import org.junit.Test;
import org.nustaq.serialization.simpleapi.DefaultCoder;
import org.nustaq.serialization.simpleapi.FSTCoder;
import org.nustaq.serialization.simpleapi.MinBinCoder;
import org.nustaq.serialization.simpleapi.OnHeapCoder;

import java.io.Serializable;

import static junit.framework.Assert.assertTrue;

/**
 * Created by ruedi on 14.11.2014.
 */
public class CarBench {

    private static final String VEHICLE_CODE = "abcdef";
    private static final String ENG_MAN_CODE = "abc";
    private static final String MAKE = "AUDI";
    private static final String MODEL = "R8";

    public static enum Model {
        A,B,C
    }

    public static class Engine implements Serializable {
        short capacity;
        byte cylinders;
        short maxRpm;
        String manufactureCode;
        String fuel;
    }

    public static class FueldData implements Serializable{
        short speed;
        float mpg;

        FueldData(short speed, float mpg) {
            this.speed = speed;
            this.mpg = mpg;
        }
    }

    public static enum OptionalExtras implements Serializable {
        SPORTS_BACK, SUN_ROOF
    }

    public static class Accel implements Serializable {
        byte mph;
        float seconds;

        Accel(byte mph, float seconds) {
            this.mph = mph;
            this.seconds = seconds;
        }
    }

    public static class Car implements Serializable {
        int serialNumber;
        short modelYear;
        boolean available;
        Model code = Model.A;
        int someNumbers[];
        String vehicleCode;
        OptionalExtras optionalExtras[];
        Engine engine;
        FueldData fd[];
        String make;
        String model;
        PerformanceFigures perf[];
    }

    public static class PerformanceFigures implements Serializable {
        Accel accel[];
        byte octane;
        public PerformanceFigures(byte octane, Accel[] accels) {
            this.octane = octane;
            this.accel = accels;
        }
    }

    public static Car setupSampleObject() {
        final Car car = new Car();

        car.code = Model.A;
        car.modelYear = 2005;
        car.serialNumber = 12345;
        car.available = true;
        car.vehicleCode = VEHICLE_CODE;

        car.someNumbers = new int[] { 1,2,3,4,5,6 };
        car.optionalExtras = new OptionalExtras[] {OptionalExtras.SPORTS_BACK,OptionalExtras.SUN_ROOF};

        car.engine = new Engine();
        car.engine.capacity=4200;
        car.engine.manufactureCode=ENG_MAN_CODE;
        car.engine.cylinders=8; // :-)

        car.fd = new FueldData[] { new FueldData((short) 30,35.9f), new FueldData((short) 50,35.9f), new FueldData((short) 70,35.9f) };

        car.perf = new PerformanceFigures[] {
                new PerformanceFigures((byte) 95,new Accel[] {
                        new Accel((byte) 30,4.0f),
                        new Accel((byte) 60,7.5f),
                        new Accel((byte) 100,12.2f),
                }),
                new PerformanceFigures((byte) 95,new Accel[] {
                        new Accel((byte) 30,3.8f),
                        new Accel((byte) 60,7.1f),
                        new Accel((byte) 100,11.8f),
                }),
        };

        car.make = MAKE;
        car.model = MODEL;

        return car;
    }

    @Test
    public void carBench() throws Exception {
        System.out.println("shared ..");
        carBenchIntern(true);
        System.out.println("unshared ..");
        carBenchIntern(false);
    }

    public void carBenchIntern(boolean shared) throws Exception {
        OnHeapCoder coder =new OnHeapCoder(shared,
                Car.class, CarBench.Engine.class, CarBench.Model.class,
                CarBench.Accel.class, CarBench.PerformanceFigures.class,
                CarBench.FueldData.class, CarBench.OptionalExtras.class);

        byte arr[] = new byte[10000];

        System.out.println("-----");
        Object car = setupSampleObject();
        Object deser = null;

        for (int i=0; i<10; i++) {
            deser = onhbench(car, coder, arr, 0);
        }
        assertTrue(DeepEquals.deepEquals(car, deser));
    }

    @Test
    public void carBenchDefault() throws Exception {
        System.out.println("default shared ..");
        carBenchInternDefault(true);
        System.out.println("default unshared ..");
        carBenchInternDefault(false);
    }

    public void carBenchInternDefault(boolean shared) throws Exception {
        DefaultCoder coder =new DefaultCoder(shared,
                Car.class, CarBench.Engine.class, CarBench.Model.class,
                CarBench.Accel.class, CarBench.PerformanceFigures.class,
                CarBench.FueldData.class, CarBench.OptionalExtras.class);

        byte arr[] = new byte[10000];

        System.out.println("-----");
        Object car = setupSampleObject();
        Object deser = null;

        for (int i=0; i<10; i++) {
            deser = onhbench(car, coder, arr, 0);
        }
        assertTrue(DeepEquals.deepEquals(car, deser));
    }

    @Test
    public void carBenchMinBin() throws Exception {
        System.out.println("minbin shared ..");
        carBenchInternMinBin(true);
        System.out.println("minbin unshared ..");
        carBenchInternMinBin(false);
    }

    public void carBenchInternMinBin(boolean shared) throws Exception {
        MinBinCoder coder =new MinBinCoder(shared,
                Car.class, CarBench.Engine.class, CarBench.Model.class,
                CarBench.Accel.class, CarBench.PerformanceFigures.class,
                CarBench.FueldData.class, CarBench.OptionalExtras.class);

        byte arr[] = new byte[10000];

        System.out.println("-----");
        Object car = setupSampleObject();
        Object deser = null;

        for (int i=0; i<10; i++) {
            deser = onhbench(car, coder, arr, 0);
        }
        assertTrue(DeepEquals.deepEquals(car, deser));
    }


    protected Object onhbench(Object toSer, FSTCoder coder, byte[] bytez, int off) throws Exception {
        long tim = System.currentTimeMillis();
        int count = 0;
        Object deser = null;
        while ( System.currentTimeMillis() - tim < 1000 ) {
            count++;
            coder.toByteArray(toSer, bytez, off, (int) bytez.length);
        }
        System.out.println("onheap enc COUNT:"+count);
        tim = System.currentTimeMillis();
        count = 0;
        while ( System.currentTimeMillis() - tim < 1000 ) {
            count++;
            deser = coder.toObject(bytez, off, (int) bytez.length);
        }
        System.out.println("onheap dec COUNT:"+count);
        return deser;
    }

}
