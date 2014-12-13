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

package org.nustaq.kson;

/**
 *
 * Created by ruedi on 07.08.2014.
 */
public class KsonStringOutput implements KsonCharOutput {

    StringBuilder builder = new StringBuilder();

    @Override
    public void writeChar(char c) {
        builder.append(c);
    }

    @Override
    public void writeString(String s) {
        builder.append(s);
    }

    @Override
    public char lastChar() {
        if ( builder.length() > 0 ) {
            return builder.charAt(builder.length()-1);
        }
        return 0;
    }

    @Override
    public void back(int i) {
        builder.setLength(builder.length()-i);
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    public String toString() {
        return builder.toString();
    }
}
