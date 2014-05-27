package de.ruedigermoeller.serialization.testclasses.jdkcompatibility;

import de.ruedigermoeller.serialization.annotations.*;
import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

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

public class ExternalizableTest implements Serializable, HasDescription {
    Vector test = new Vector();
    ArrayList<ExternalTest> li = new ArrayList<ExternalTest>();
    ExternalTest ext = new ExternalTest(31);

    public ExternalizableTest() {
        for (int i = 0; i < 150; i++)
        {
            test.add(new ExternalTest(i));
            li.add(new ExternalTest(i+500));
        }
    }

    @Override
    public String getDescription() {
        return "Performance of Externalizable objects.";
    }

    public static class ExternalTest implements Externalizable {

        String pok = "HuckaHuckaHuckaHuckaHuckaHucka";
        NestedExternalTest nested = new NestedExternalTest(43);
        int i = 34535;

        int j = 0;

        public ExternalTest() {
        }

        ExternalTest(int j) {
            this.j = j;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(i);
            out.writeInt(j);
            out.writeObject(nested);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            i = in.readInt();
            j = in.readInt();
            pok = "HuckaHuckaHuckaHuckaHuckaHucka";
            nested = (NestedExternalTest) in.readObject();
        }
    }

    public static class NestedExternalTest implements Externalizable {

        String pok = "Some String";
        int i = 22;

        int j = 0;

        public NestedExternalTest() {
        }

        NestedExternalTest(int j) {
            this.j = j;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(i);
            out.writeInt(j);
            out.writeUTF(pok);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            i = in.readInt();
            j = in.readInt();
            pok = in.readUTF();
        }
    }

}
