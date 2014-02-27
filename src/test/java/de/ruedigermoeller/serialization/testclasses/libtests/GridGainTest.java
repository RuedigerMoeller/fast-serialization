//// commented as i can't redistribute the jars.
//
//package de.ruedigermoeller.serialization.testclasses.libtests;
//
//import org.gridgain.grid.GridException;
//import org.gridgain.grid.marshaller.GridMarshaller;
//import org.gridgain.grid.marshaller.optimized.GridOptimizedMarshaller;
//
//import java.io.ByteArrayInputStream;
//import java.io.OutputStream;
//import java.util.Arrays;
//
///**
//* Copyright (c) 2012, Ruediger Moeller. All rights reserved.
//* <p/>
//* This library is free software; you can redistribute it and/or
//* modify it under the terms of the GNU Lesser General Public
//* License as published by the Free Software Foundation; either
//* version 2.1 of the License, or (at your option) any later version.
//* <p/>
//* This library is distributed in the hope that it will be useful,
//* but WITHOUT ANY WARRANTY; without even the implied warranty of
//* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//* Lesser General Public License for more details.
//* <p/>
//* You should have received a copy of the GNU Lesser General Public
//* License along with this library; if not, write to the Free Software
//* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
//* MA 02110-1301  USA
//* <p/>
//* Date: 16.06.13
//* Time: 10:52
//* To change this template use File | Settings | File Templates.
//*/
//public class GridGainTest extends SerTest {
//    public GridGainTest(String title) {
//        super(title);
//    }
//
//    GridMarshaller marsh;
//
//    @Override
//    public void run(Object toWrite) {
//        // marshaller cannot be reused
////        try {
////            marsh = new GridOptimizedMarshaller(false, Arrays.asList(toWrite.getClass().getName()), null);
////        } catch (GridException e) {
////            throw new RuntimeException(e);
////        }
//        super.run(toWrite);
//    }
//
//    @Override
//    public void runOnce(Object toWrite) {
//        try {
//            marsh = new GridOptimizedMarshaller(false, Arrays.asList(toWrite.getClass().getName()), null);
//        } catch (GridException e) {
//            throw new RuntimeException(e);
//        }
//        super.runOnce(toWrite);
//    }
//
//    @Override
//    protected void readTest(ByteArrayInputStream bin, Class cl) {
//        try {
//            resObject = marsh.unmarshal(bin,cl.getClassLoader());
//        } catch (GridException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
//        try {
//            marsh.marshal(toWrite,bout);
//        } catch (GridException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public String getColor() {
//        return "#a0a0a0";
//    }
//}
