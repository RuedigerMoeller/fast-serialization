package de.ruedigermoeller.serialization.testclasses.libtests;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import de.ruedigermoeller.serialization.testclasses.jdkcompatibility.Swing;
import de.ruedigermoeller.serialization.util.FSTUtil;
import sun.misc.Unsafe;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

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
 * Date: 16.06.13
 * Time: 10:38
 * To change this template use File | Settings | File Templates.
 */
public class FSTTest extends SerTest {

    FSTConfiguration defconf;
    boolean uns;
    boolean preferSpeed;
    FSTObjectInput in;
    FSTObjectOutput out;
    public FSTTest(String desc,boolean uns,boolean preferSpeed) {
        super(desc);
        this.uns = uns;
        this.preferSpeed = preferSpeed;
    }

    @Override
    public void init() {
        defconf = FSTConfiguration.createDefaultConfiguration();
        defconf.setPreferSpeed(preferSpeed);
        in = new FSTObjectInput(defconf);
        out = new FSTObjectOutput(defconf);
    }

    public String getColor() {
        return "#4040a0";
    }

    @Override
    protected void readTest(ByteArrayInputStream bin, Class cl) {
        try {
            in.resetForReuse(bin);
            Object res = in.readObject(cl);
            resObject = res;
        } catch (Throwable e) {
            FSTUtil.printEx(e);
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void writeTest(Object toWrite, OutputStream bout, Class aClass) {
        out.resetForReUse(bout);
        try {
            out.writeObject(toWrite, aClass);
            out.flush();
        } catch (Throwable e) {
            FSTUtil.printEx(e);
            throw new RuntimeException(e);
        }
    }
};
