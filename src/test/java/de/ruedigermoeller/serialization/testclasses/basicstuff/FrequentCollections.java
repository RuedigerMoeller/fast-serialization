package de.ruedigermoeller.serialization.testclasses.basicstuff;

import de.ruedigermoeller.serialization.annotations.Conditional;
import de.ruedigermoeller.serialization.annotations.Flat;
import de.ruedigermoeller.serialization.annotations.Predict;
import de.ruedigermoeller.serialization.testclasses.HasDescription;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

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
 * Date: 14.06.13
 * Time: 20:51
 * To change this template use File | Settings | File Templates.
 */
public class FrequentCollections implements Serializable, HasDescription {

    HashMap map = new HashMap();
    ArrayList list = new ArrayList();

    public FrequentCollections() {
        fillRandom();
    }

    public void fillRandom() {
        for ( int i = 0; i < 500; i++ ) {
            map.put(randomValue(i),randomValue(i+1));
        }
        for ( int i = 0; i < 500; i++ ) {
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
