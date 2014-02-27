package de.ruedigermoeller.serialization.testclasses.enterprise;

import de.ruedigermoeller.serialization.testclasses.HasDescription;
import de.ruedigermoeller.serialization.testclasses.enterprise.murks.common.ManagingManager;
import de.ruedigermoeller.serialization.testclasses.enterprise.murks.common.special.common.UnManagedManager;
import de.ruedigermoeller.serialization.testclasses.enterprise.murks.common.special.common.util.UtilManager;
import de.ruedigermoeller.serialization.testclasses.enterprise.schwurbel.kinda.xml.UnmanagedManagingManager;
import de.ruedigermoeller.serialization.testclasses.enterprise.schwurbel.others.ManagingCoordinatorDispatcher;
import de.ruedigermoeller.serialization.testclasses.enterprise.schwurbel.v1.ManagingDispatcherCoordinator;
import de.ruedigermoeller.serialization.testclasses.enterprise.wurschtel.NoniteratingObjectIteratorWrapperVisitor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 02.12.12
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class ManyClasses implements Serializable, HasDescription {

    ObjectOrientedDataType var;
    ObjectOrientedDataType var1;
    ObjectOrientedInt int0;
    ObjectOrientedInt int1;
    SimpleOrder order, order1;
    Trader coordinatorBoard;

    public ManyClasses() { // kryo
    }

    public ManyClasses(int num) {
        var = new UnManagedManager("Man"+num);
        var1 = new ManagingCoordinatorDispatcher("Coordinate"+num);
        int0 = new ManagingDispatcherCoordinator(num);
        int1 = new UnmanagedManagingManager(num);
        order = new UtilManager(
                new Date(18),
                new UnManagedManager("pok"+num),
                new ObjectOrientedDataType("pokasd"),
                new UnManagedManager("irgendwas"+num),
                new UnManagedManager("irgendwasanderes"+num),
                new ObjectOrientedInt(num+13),
                new UnmanagedManagingManager(num*12),
                new UnmanagedManagingManager(num*11),
                9999999l,
                new UnManagedManager("pok1"+num)
                );
        order1 = new NoniteratingObjectIteratorWrapperVisitor(
                new Date(19),
                new ManagingCoordinatorDispatcher("fui"+num),
                new ObjectOrientedDataType("adadad"),
                new UnManagedManager("irgendwas"),
                new ManagingCoordinatorDispatcher("irgendwasanderes"+num),
                new ObjectOrientedInt(num+14),
                new UnmanagedManagingManager(num*10),
                new UnmanagedManagingManager(num*9),
                9999999l,
                new ObjectOrientedDataType("pok21"+num)

        );
        coordinatorBoard = new ManagingManager(
                new ManagingDispatcherCoordinator(num*2),
                new ManagingDispatcherCoordinator(num*4),
                new UnManagedManager("ManX"+num),
                new UnmanagedManagingManager(24),
                new UnmanagedManagingManager(27+num),
                new ManagingCoordinatorDispatcher("ManXX"+num),
                new ObjectOrientedDataType("adadad"),
                new ManagingDispatcherCoordinator(num*345),
                null,
                new ObjectOrientedDataType("adadad"),
                null,
                new ManagingCoordinatorDispatcher("ManXXX"+num),
                true,
                null,
                new UnManagedManager("ManXY"+num)
        );
    }

    public static Object getArray() {
        return new ManyClasses[] {
                new ManyClasses(13), new ManyClasses(1257), new ManyClasses(18),
                new ManyClasses(525), new ManyClasses(1243), new ManyClasses(1257),
                new ManyClasses(915775), new ManyClasses(714913), new ManyClasses(179157),
        };
    }

    @Override
    public String getDescription() {
        return "Tests speed serializing a complex object graph of many different classes with few primitive fields.";
    }
}
