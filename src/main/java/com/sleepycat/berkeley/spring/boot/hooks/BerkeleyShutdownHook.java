package com.sleepycat.berkeley.spring.boot.hooks;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;

/**
 * JVM shutdown hook that cleanly closes the Berkeley DB database, class catalog and
 * environment when the application exits.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class BerkeleyShutdownHook extends Thread{

	/** The database to close on shutdown. */
	private Database database;
	/** The class catalog to close on shutdown. */
	private StoredClassCatalog catalog;
	/** The environment to close on shutdown. */
	private Environment environment;

	/**
	 * Creates a shutdown hook for the given database, class catalog and environment.
	 * @param database the database to close
	 * @param catalog the class catalog to close
	 * @param environment the environment to close
	 */
	public BerkeleyShutdownHook(Database database, StoredClassCatalog catalog, Environment environment) {
		this.database = database;
		this.catalog = catalog;
		this.environment = environment;
	}

	/**
	 * Closes the database, class catalog and environment in order.
	 */
	@Override
	public void run() {
		database.close();
	    catalog.close(); //这句应该可以关闭与之相关的数据库，但是API上没有将
	    environment.close();
	}

}
