package de.ruedigermoeller.serialization.testclasses.basicstuff;

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
 * Date: 24.02.13
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public class BugReport2
{

    private static FSTConfiguration sm_conf = FSTConfiguration.createDefaultConfiguration();

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        testFstClass t = new testFstClass();

        try
        {
            ByteArrayOutputStream optStream = new ByteArrayOutputStream();
            FSTObjectOutput out = new FSTObjectOutput(optStream, sm_conf);
            out.writeObject(t);
            out.close();
            byte[] b = optStream.toByteArray();

            ByteArrayInputStream iptStream = new ByteArrayInputStream(b);
            FSTObjectInput in = new FSTObjectInput(iptStream, sm_conf);
            testFstClass tmpa = (testFstClass) in.readObject();

        }
        catch (IOException e)
        {
            e.printStackTrace();

        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}

class testFstClass implements Serializable
{
    enum testEnum
    {
        abc
    }

    testEnum m_packetDef;

    // use this name cause Exception
    public int bbbbb = 23;

    // use this name is fine
    // public int wqxcxzggsdgxcv;

    public boolean aaaaaaa = false;
    public boolean ddddddd = true;
}