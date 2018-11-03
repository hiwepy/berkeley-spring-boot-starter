package com.sleepycat.berkeley.spring.boot;

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

 