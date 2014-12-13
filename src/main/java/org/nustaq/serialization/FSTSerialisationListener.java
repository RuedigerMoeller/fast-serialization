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
package org.nustaq.serialization;

/**
 * Date: 21.10.13
 * Time: 18:28
 *
 * can be used for stats or other weird things (FSTObjectOutput). Note this will slow down serialization significantly
 */
public interface FSTSerialisationListener {

    void objectWillBeWritten( Object obj, int streamPosition );
    void objectHasBeenWritten( Object obj, int oldStreamPosition, int streamPosition );

}