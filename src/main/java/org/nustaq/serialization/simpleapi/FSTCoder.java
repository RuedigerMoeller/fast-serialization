/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nustaq.serialization.simpleapi;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by ruedi on 15.11.14.
 */
public interface FSTCoder {

    public Object toObject( byte arr[], int startIndex, int availableSize);
    public Object toObject( byte arr[] );

    public int toByteArray( Object obj, byte result[], int resultOffset, int availableSize );
    public byte[] toByteArray( Object o );

    // take care: changes in setup have to happen before first use
    public FSTConfiguration getConf();

}
