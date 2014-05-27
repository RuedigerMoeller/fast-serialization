package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

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

public class FrequentCollections implements Serializable, HasDescription {

    HashMap map = new HashMap();
    ArrayList list = new ArrayList();

    public FrequentCollections() {
        
    }

    public FrequentCollections(int dummy) {
        fillRandom();
    }

    public void fillRandom() {
        for ( int i = 0; i < 300; i++ ) {
            map.put(randomValue(i),randomValue(i+1));
        }
        for ( int i = 0; i < 300; i++ ) {
            list.add(randomValue(i));
        }
    }

    private Object randomValue(int i) {
        Object value = null;
        switch ( i%5) {
            case 0:
                value = new Integer((int) (Math.random()*Integer.MAX_VALUE)-Integer.MAX_VALUE);
                break;
            case 1:
                value = i;
                break;
            case 2:
                value = new Integer((int) (Math.random()*Integer.MAX_VALUE)-Integer.MAX_VALUE);
                break;
            case 3:
                value = new Integer((int) (Math.random()*Integer.MAX_VALUE)-Integer.MAX_VALUE);
                break;
            case 4:
                value = new Long((int) (Math.random()*Long.MAX_VALUE)-Long.MAX_VALUE);
                break;
        }
        return value;
    }

    @Override
    public String getDescription() {
        return "Measures serialization of most popular collection classes. (HashMap and an ArrayList filled with Integer and Long).";
    }
}
