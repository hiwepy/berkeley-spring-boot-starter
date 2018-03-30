package com.sleepycat.berkeley.spring.boot.hooks;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;

public class BerkeleyShutdownHook extends Thread{
	
	private Database database;
	private StoredClassCatalog catalog;
	private Environment environment;
	
	public BerkeleyShutdownHook(Database database, StoredClassCatalog catalog, Environment environment) {
		this.database = database;
		this.catalog = catalog;
		this.environment = environment;
	}

	@Override
	public void run() {
		database.close();
	    catalog.close(); //这句应该可以关闭与之相关的数据库，但是API上没有将
	    environment.close();
	}
	
}
