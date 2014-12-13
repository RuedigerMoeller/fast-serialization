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

import java.util.Stack;

/**
 * Created by ruedi on 26.12.13.
 */
public class KsonParseException extends RuntimeException {

    public KsonParseException(String ms, KsonCharInput in) {
        super(ms+":"+in.getString(in.position()-20,20)+getStackString(in));
    }

    private static String getStackString(KsonCharInput in) {
        if ( in instanceof KsonStringCharInput && ((KsonStringCharInput) in).stack != null) {
            final Stack<KsonDeserializer.ParseStep> stack = ((KsonStringCharInput) in).stack;
            StringBuilder res = new StringBuilder("\n\n");
            for (int i = stack.size()-1; i >= 0; i--) {
                KsonDeserializer.ParseStep parseStep = stack.get(i);
                res.append("  ").append(parseStep).append("\n");
            }
            return res.toString();
        }
        return null;
    }

    public KsonParseException(String s, KsonCharInput in, Throwable ex) {
        super(s+":"+in.getString(in.position()-20,20)+getStackString(in),ex);
    }
}
