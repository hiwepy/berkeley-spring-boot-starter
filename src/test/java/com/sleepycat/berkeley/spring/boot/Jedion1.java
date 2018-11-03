package com.sleepycat.berkeley.spring.boot;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;

public class Jedion1 {

    public static void main(String[] args) throws UnsupportedEncodingException{
        String key = "a";

        Url url = new Url();

        url.setId(1);
        url.setUrl("http://www.baidu.com");

        EnvironmentConfig ec = new EnvironmentConfig();
        ec.setAllowCreate(true);
        ec.setTransactional(true);

        Environment env = new Environment(new File("dbEnv"), ec);
        DatabaseConfig dc = new DatabaseConfig();
        dc.setAllowCreate(true);

        Database database = env.openDatabase(null, "db", dc);

        StoredClassCatalog classCatalog = new StoredClassCatalog(database);//创建catalog用来存储序列化对象

        EntryBinding<Url> dataBinding = new SerialBinding(classCatalog, Url.class);//SerialBinding表示这个对象能序列化到磁盘上

        DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes("utf8"));
        DatabaseEntry databaseValue = new DatabaseEntry();
        dataBinding.objectToEntry(url, databaseValue);
        database.put(null, databaseKey, databaseValue); 

        //读取序列化数据并还原
        database.get(null, databaseKey, databaseValue, LockMode.DEFAULT);
        url = (Url)dataBinding.entryToObject(databaseValue);
        System.out.println(url.getId());
        System.out.println(url.getUrl());

    }
}
