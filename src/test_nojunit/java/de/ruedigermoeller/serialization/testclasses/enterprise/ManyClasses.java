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
 Copyright [2014] Ruediger Moeller

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
                new ManyClasses(179157),
        };
    }

    @Override
    public String getDescription() {
        return "Tests speed serializing a complex object graph of many different classes with few primitive fields.";
    }
}
