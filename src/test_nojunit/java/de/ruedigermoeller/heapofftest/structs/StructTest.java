package de.ruedigermoeller.heapofftest.structs;

import de.ruedigermoeller.heapoff.structs.FSTStruct;
import de.ruedigermoeller.heapoff.structs.FSTTypedStructAllocator;
import de.ruedigermoeller.heapoff.structs.structtypes.StructInt;
import de.ruedigermoeller.heapoff.structs.structtypes.StructMap;
import de.ruedigermoeller.heapoff.structs.structtypes.StructArray;
import de.ruedigermoeller.heapoff.structs.structtypes.StructString;
import de.ruedigermoeller.heapoff.structs.unsafeimpl.FSTStructFactory;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 07.07.13
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
public class StructTest {

    public static void main( String arg[] ) {

        TestData data = new TestData();
        data.setNested(new TestData());
        data.dataStructArray = new StructArray<TestData>(10, new TestData());

        FSTTypedStructAllocator<TestData> alloc = new FSTTypedStructAllocator<TestData>(data,50);
        FSTTypedStructAllocator<StructString> strAlloc = new FSTTypedStructAllocator<StructString>( new StructString(10), 10 );

        System.out.println("size td:"+alloc.getTemplateSize()+" str:"+strAlloc.getTemplateSize());


        check(alloc.newStruct(new TestData()).getDataStructArray() == null);
        check(alloc.newStruct(new TestData()).getNested() == null);

        StructMap<StructInt,TestData> intMap = alloc.newMap(1000, new StructInt(0));

        for (int i=0; i < 1000; i++ ) {
            TestData value = new TestData();
            value.getString().setString("int "+i);
            try {
                intMap.put(new StructInt(i), value);
            } catch (Exception wx) {
                System.out.println(i+" "+wx);
            }
        }

        for (int i=0; i < 1000; i++ ) {
            StructString string = intMap.get(new StructInt(i)).getString();
            if ( ! string.toString().equals("int "+i) ) {
                throw new RuntimeException("error: '"+string+"'");
            }
//            System.out.println(""+i+" => "+ string);
        }

        compareTestData( data, alloc.newStruct() );
        compareTestData( data.getNested(), alloc.newStruct().getNested() );
        compareTestData(data.getNested(), alloc.newStruct().getDataStructArray().get(3));
        check(alloc.newStruct().getNested().getDataStructArray()==null);

        ////////////////////////////////////////////////////////////////
        // test structMap
        ////////////////////////////////////////////////////////////////

        TestData smapTest = alloc.newStruct();
        StructMap<StructInt, StructString> structMap = smapTest.getStructMap();
        for ( int i = 0; i<6; i++) {
            StructString value = new StructString("Hallo" + i);
            StructInt key = new StructInt(i);
            check(structMap.get(key) == null);
            structMap.put(key, value);
            check(structMap.get(key).equals(value));
        }
        structMap.put(new StructInt(99), new StructString("test",10));
        boolean exThrown = false;
        try {
            structMap.put(new StructInt(98), new StructString("test",11));
        } catch (Exception ex) {
            exThrown = true;
        }
        check(exThrown);


        ////////////////////////////////////////////////////////////////
        // test untyped polymorphic objectArray
        ////////////////////////////////////////////////////////////////

        TestData objArrayTest = alloc.newStruct();
        check( ((StructInt)objArrayTest.objArray(4).cast()).get() == 17 );
        System.out.println("len "+objArrayTest.objArrayElementSize()+" "+((StructString)objArrayTest.objArray(1).cast()).getByteSize());
        check( objArrayTest.objArrayElementSize() == ((StructString)objArrayTest.objArray(1).cast()).getByteSize() );
        exThrown = false;
        try {
            ((StructString)objArrayTest.objArray(1).cast()).setString("01234567890123456780");
        } catch (Exception ex) {
            exThrown = true;
        }
        check(exThrown);
        FSTStruct iterPointer = objArrayTest.objArrayPointer();
        for ( int i= 0; i < objArrayTest.objArrayLen(); i++ ) {
            if ( iterPointer.isNull() ) {
                check( i==2 || i == 0 || i == 5 );
                System.out.println(i+" null");
            } else {
                System.out.println(i+" "+iterPointer.cast().getPointedClass());
            }
            iterPointer.next();
        }

        FSTStruct fstStruct = objArrayTest.objArrayPointer();
        check(fstStruct.isNull());

        for ( int i = 0; i < objArrayTest.objArrayLen(); i++) {
            objArrayTest.objArray(i,new StructInt(i));
            check(((StructInt)objArrayTest.objArray(i).cast()).get() == i);
        }

        objArrayTest.objArray(0, new StructString("01234567890123456780"));
        System.out.println("Struct index " + objArrayTest.objArrayStructIndex());
        System.out.println("objarr index "+objArrayTest.objArrayIndex());
        System.out.println("objarr pointer offset "+objArrayTest.objArrayPointer().___offset);
        check(objArrayTest.objArrayStructIndex() > 0);
        check(objArrayTest.objArrayElementSize() > 0);
        check(objArrayTest.objArrayPointer() != null);
        check(objArrayTest.objArrayStructIndex()%8 == 0);
        check(objArrayTest.objArrayIndex()%8 == 0);

        ////////////////////////////////////////////////////////////////
        // test templated array
        ////////////////////////////////////////////////////////////////

        TestData tpl = alloc.newStruct();
        for ( int i = 0; i < tpl.templatedObjArrayLen(); i++ ) {
            check( tpl.templatedObjArray(i).cast().toString().equals("Oh") );
        }

        for ( int i = 0; i < objArrayTest.objArrayLen(); i++) {
            objArrayTest.objArray(i,null);
            check(objArrayTest.objArray(i) == null);
        }

        ////////////////////////////////////////////////////////////////
        // test typed array
        ////////////////////////////////////////////////////////////////
        TestData typed = alloc.newStruct();
        check(typed.typedArray(0) == null);
        for ( int i = 0; i < typed.typedArrayLen(); i++ ) {
            System.out.println("typed " + i + ":" + tpl.typedArray(i));
        }

        ////////////////////////////////////////////////////////////////
        // test object set/get
        ////////////////////////////////////////////////////////////////
        TestData objTest = alloc.newStruct();
        check(objTest.getString().charsLen() == 50);

        objTest.getString().setString("X");
        check(objTest.getString().toString().equals("X"));

        objTest.setString( new StructString("Test") );
        check( objTest.getString().toString().equals("Test") );

        objTest.setString(null);
        check( objTest.getString() == null );

        objTest.setString(new StructString("bla"));
        check(objTest.getString().toString().equals("bla"));
        objTest.setString(new StructString(50));
        exThrown = false;
        try {
            objTest.setString(new StructString(51));
        } catch (Exception e) {
            exThrown = true;
        }
        check(exThrown);

        ////////////////////////////////////////////////////////////////
        // test structarr + detach
        ////////////////////////////////////////////////////////////////
        TestData structArrTest = alloc.newStruct();
        StructArray<TestData> dataStructArray = structArrTest.getDataStructArray();
        dataStructArray.detach();

        TestData toMod = dataStructArray.get(dataStructArray.size() - 1);
        toMod.detach();
        toMod.getString().setString("modified");
        structArrTest.getDataStructArray().set(dataStructArray.size() - 2, toMod);
        check(dataStructArray.___offset != structArrTest.getDataStructArray().get(dataStructArray.size() - 2).___offset);
        check(dataStructArray.get(dataStructArray.size() - 1).getString().toString().equals("modified"));
        check(dataStructArray.get(dataStructArray.size() - 2).getString().toString().equals("modified"));
        toMod = (TestData) toMod.createCopy();
        toMod.getString().setString("---");
        check(dataStructArray.get(dataStructArray.size() - 1).getString().toString().equals("modified"));
        check(dataStructArray.get(dataStructArray.size() - 2).getString().toString().equals("modified"));
        structArrTest.getDataStructArray().set(dataStructArray.size() - 2, toMod);
        structArrTest.getDataStructArray().set(dataStructArray.size() - 1, toMod);
        check(dataStructArray.get(dataStructArray.size() - 1).getString().toString().equals("---"));
        check(dataStructArray.get(dataStructArray.size() - 2).getString().toString().equals("---"));

        // random string fumbling
        StructArray <StructString> sl = strAlloc.newArray(25);
        System.out.println("len "+sl.getByteSize());

        for ( int i=0; i < sl.size(); i++ ) {
            sl.set(i,new StructString("Hallo"+i));
        }

        for ( int i=0; i < sl.size(); i++ ) {
            check(sl.get(i).toString().equals("Hallo"+i));
        }


    }

    private static void check(boolean b) {
        if ( ! b ) {
            throw new RuntimeException("assertion failed");
        }
    }

    private static void compareTestData(TestData data, TestData data1) {
        if ( !data.getString().equals(data1.getString()) )
            throw new RuntimeException();

        if ( data.isBool() !=data1.isBool() )
            throw new RuntimeException();
        if ( data.getA() !=data1.getA() )
            throw new RuntimeException();
        if ( data.getB() !=data1.getB() )
            throw new RuntimeException();
        if ( data.getC() !=data1.getC() )
            throw new RuntimeException();
        if ( data.getD() !=data1.getD() )
            throw new RuntimeException();
        if ( data.getE() !=data1.getE() )
            throw new RuntimeException();
        if ( data.getF() !=data1.getF() )
            throw new RuntimeException();
        if ( data.getG() !=data1.getG() )
            throw new RuntimeException();

        for ( int i = 0; i < data.arrcLen(); i++ ) {
            if ( data1.arrc(i) != data.arrc(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrbLen(); i++ ) {
            if ( data1.arrb(i) != data.arrb(i) ) {
                throw new RuntimeException("at elem "+i);
            }
        }
        for ( int i = 0; i < data.arrdLen(); i++ ) {
            if ( data1.arrd(i) != data.arrd(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrALen(); i++ ) {
            if ( data1.arrA(i) != data.arrA(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrgLen(); i++ ) {
            if ( data1.arrg(i) != data.arrg(i) ) {
                throw new RuntimeException();
            }
        }
        for ( int i = 0; i < data.arrBoolLen(); i++ ) {
            if ( data1.arrBool(i) != data.arrBool(i) ) {
                for ( int ii = 0; ii < data.arrBoolLen(); ii++ ) {
                    System.out.println(""+ii+" "+data.arrBool(ii));
                }
                for ( int ii = 0; ii < data.arrBoolLen(); ii++ ) {
                    System.out.println(""+ii+" "+data1.arrBool(ii));
                }
                System.out.println("0:"+data+" 1:"+data1);
                throw new RuntimeException("len "+data.arrBoolLen());
            }
        }
    }

}

