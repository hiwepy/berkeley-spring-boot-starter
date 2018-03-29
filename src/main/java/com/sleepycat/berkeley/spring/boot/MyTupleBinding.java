package com.sleepycat.berkeley.spring.boot;
/*
 * Copyright (c) 2010-2020, wandalong (hnxyhcwdl1003@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */



import com.sleepycat.bind.tuple.TupleBinding;

import com.sleepycat.bind.tuple.TupleInput;

import com.sleepycat.bind.tuple.TupleOutput;

 

public class MyTupleBinding extends TupleBinding {

 

    // Write a MyData2 object to a TupleOutput

    public void objectToEntry(Object object, TupleOutput to) {

 

        MyData2 myData = (MyData2)object;

 

        // Write the data to the TupleOutput (a DatabaseEntry).

        // Order is important. The first data written will be

        // the first bytes used by the default comparison routines.

        to.writeDouble(myData.getDouble().doubleValue());

        to.writeLong(myData.getLong());

        to.writeString(myData.getString());

    }

 

    // Convert a TupleInput to a MyData2 object

    public Object entryToObject(TupleInput ti) {

 

        // Data must be read in the same order that it was

        // originally written.

        Double theDouble = new Double(ti.readDouble());

        long theLong = ti.readLong();

        String theString = ti.readString();

 

        MyData2 myData = new MyData2();

        myData.setDouble(theDouble);

        myData.setLong(theLong);

        myData.setString(theString);

 

        return myData;

    }

}

 