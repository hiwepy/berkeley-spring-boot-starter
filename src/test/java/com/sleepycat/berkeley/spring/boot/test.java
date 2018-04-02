/*
 * Copyright (c) 2010-2020, vindell (hnxyhcwdl1003@163.com).
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

package com.sleepycat.berkeley.spring.boot;

import java.io.File;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class test {

	public static void main(String[] args) {

	}

	/**
	 * 
	 * 打开和关闭环境，示例一
	 * 
	 *
	 */

	public void eg1() {

		// ----打开环境，如果不存在，则创建一个------------

		Environment myDbEnvironment = null;

		try {

			EnvironmentConfig envConfig = new EnvironmentConfig();

			envConfig.setAllowCreate(true); // true不存在就创建,false如果不存在则打开环境失败

			// envConfig.setReadOnly(true); //true
			// 以只读方式打开，如果是多进程应用，每个进程都要设置为true

			// envConfig.setTransactional(true);//true支持事务，false不支持，默认false。可以更改配置文件来设置此参数。

			myDbEnvironment = new Environment(new File(".//"), envConfig);// 环境所在路径

			java.util.List myDbNames = myDbEnvironment.getDatabaseNames(); // 得到所有的数据库的名字

			for (int i = 0; i < myDbNames.size(); i++) {

				System.out.println("Database Name: "
						+ (String) myDbNames.get(i));

			}

		} catch (DatabaseException dbe) {

			// 错误处理

		}

		// ----关闭环境----------------

		try {

			if (myDbEnvironment != null) {

				myDbEnvironment.cleanLog(); // 在关闭前也最好执行一下cleaner，清理一下日志文件，因为delete操作会浪费一些空间

				myDbEnvironment.close();

			}

		} catch (DatabaseException dbe) {

			// 错误处理

		}

	}

	/**
	 * 
	 * 打开环境示例二
	 * 
	 *
	 */

	public void eg2()

	{

		Environment myEnv = null;

		try {

			myEnv = new Environment(new File("/export/dbEnv"), null);

			EnvironmentMutableConfig envMutableConfig = new EnvironmentMutableConfig();

			envMutableConfig.setCachePercent(50);// 设置je的cache占用jvm 内存的百分比。

			// envMutableConfig.setCacheSize(123456);//设定缓存的大小为123456Bytes

			envMutableConfig.setTxnNoSync(true);// 设定事务提交时是否写更改的数据到磁盘，true不写磁盘。

			// envMutableConfig.setTxnWriteNoSync(false);//设定事务在提交时，是否写缓冲的log到磁盘。如果写磁盘会影响性能，不写会影响事务的安全。随机应变。

			myEnv.setMutableConfig(envMutableConfig);

			EnvironmentStats envStats = myEnv.getStats(null);// 调用
																// Environment.getStats()
																// 返回一个EnvironmentStas对象。调用EnvironmentStats.getNCacheMiss()看命不中数。

			long cacheMisses = envStats.getNCacheMiss(); // 这个统计非常重要，尤其针对于长时间运行的应用。
															// 它返回不能够在内存中命中的请求总数，这可以用来参考指定cache的大小。

			// myEnv.getMutableConfig();//得到当前的环境配置信息

		} catch (DatabaseException dbe) {
		}

	}

	/**
	 * 
	 * 打开database,以及相关操作
	 * 
	 *
	 */

	public void eg3() {

		Environment myDbEnvironment = null;

		Database myDatabase = null;

		try {

			// Open the environment. Create it if it does not already exist.

			EnvironmentConfig envConfig = new EnvironmentConfig();

			envConfig.setAllowCreate(true);

			myDbEnvironment = new Environment(new File("/export/dbEnv"),
					envConfig); // 也可能用DatabaseConfig参数来打开，这样就可以设置数据库的属性，比如是否允许在库不存在时创建它，是否只读打开，是否支持事务等。

			// Open the database. Create it if it does not already exist.

			DatabaseConfig dbConfig = new DatabaseConfig();

			dbConfig.setAllowCreate(true);

			// transaction为null，不支持事务

			myDatabase = myDbEnvironment.openDatabase(null, "sampleDatabase",
					dbConfig); // 打开库，要提供一个数据库名字作为参数

		} catch (DatabaseException dbe) {

			// Exception handling goes here

		}

		// 记录

		String aKey = "key";

		String aData = "data";

		try {

			DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));// 最好指定编码方式，因为不指定编码会用系统的默认编码来转换，因为系统的默认编码可能会被人更改。

			DatabaseEntry theData = new DatabaseEntry(aData.getBytes("UTF-8"));

			byte[] myKey = theKey.getData();

			byte[] myData = theData.getData();

			// 从byte数组转换为string的方法

			String key = new String(myKey, "UTF-8");

			String data = new String(myData, "UTF-8");

			// get和put用在非重复的数据存储，读写库时，注意一个小区别，就是数据库，是否允许重复的记录存在，两个记录公用一个key，这就是重复的记录，我们把重复的记录成为重复集合。或者叫多重。

			// 游标用于重复数据存储put和get。

			// 数据记录在内部是用Btree按照特定排序来存储的。一般是用key来排序的，key相同的多重数据是按照data来排序。

			// 记录Using Database Records

			// 记录是由key和data组成，即所熟悉的key->value，二者都被是有DatabaseEntry封装的。

			// 这个之前也提过很多次了，DatabaseEntry可以封装原始类型和复杂的对象类型，二者都要被转换为byte
			// array存储，转换可以使用Bind API来完成

			// 写数据

			myDatabase.put(null, theKey, theData);// 如果不是可重复数据库，put将会覆盖原有的记录。

			// myDatabase.putNoOverwrite(null, theKey,
			// theData);//不允许覆盖，不管是否允许数据重复。

			// 读数据

			// --myDatabase.getSearchBoth(null, theKey, theData,
			// LockMode.DEFAULT);//查找key和data都匹配的记录

			// --查询出来的key和data都是byte数组形式。

			if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS)

			{

				byte[] retData = theData.getData();

				String foundData = new String(retData, "UTF-8");

				System.out.println("For key: '" + aKey + "' found data: '"
						+ foundData + "'.");

			}

			// 删除数据

			myDatabase.delete(null, theKey); // 删除数据

		} catch (Exception e) {
		}

		// 关闭数据库

		// 如果打开了游标，关闭时JE会发出警告，让你关闭他们先。活动状态的游标在关闭库的过程中会产生意想不到的结果，尤其是其他线程在写库的过程中。确定所有的访问都结束后再关闭库

		try {

			if (myDatabase != null) {

				myDatabase.close();

				myDbEnvironment.renameDatabase(null, "sampleDatabase", "test");// 重命名,必须先关闭数据库

				myDbEnvironment.removeDatabase(null, "sampleDatabase");// 删除数据库,必须先关闭数据库

				// myDbEnvironment.truncateDatabase(null,
				// myDatabase.getDatabaseName(),true);//删除并回收数据库空间
				// ，true返回删除的记录的数量,false不返回删除的记录数量值

			}

			if (myDbEnvironment != null) {

				myDbEnvironment.close();

			}

		} catch (DatabaseException dbe) {

			// Exception handling goes here

		}

	}

	/**
	 * 
	 * Deferred Write Databases 缓冲写库
	 * 
	 *
	 * 
	 * By默认，JE的数据库是持久化，意思就是说不缓存，写库的，这样多个应用都可以使用，把数据库设置成非持久化的，就成为缓冲写库。
	 * 
	 * 缓冲写库本质上说就是内存级数据库，这适用于那些临时的操作。比如把Oracle中的数据导入bdb，然后读bdb cache。
	 * 
	 * 缓冲写库并不是总是请求磁盘I/O，很重要的一点，缓冲写库会在内存不够用的时候写磁盘。如果内存够大，用缓存写库那是最好不过了。
	 * 
	 * 调用Database.sync()让修改过的cache数据写到磁盘
	 * 
	 * 当重新打开一个缓冲写库的环境时，数据库的状态是和上一次sync时一致的。如果缓冲写库没有同步，就会当成是空库。问号，
	 * 那么在数据关闭之前一定要sync？maybe yes。
	 * 
	 * 程序员很容易就可以把缓冲写库的修改存储到磁盘上，对经常性的修改，增加，及删除等等的操作的应用也很有用处。通过延迟写库，延迟了磁盘IO，
	 * 这可以提高数据的流量。
	 * 
	 * 注意，当没有调用sync的库关闭时而且环境也关闭的时候，cache的修改会丢失。如果没有关闭环境，所有的cache的修改还是会保留的。
	 * 
	 * 设置库为defered or not，然后用DatabaseConfig的选项打开库。
	 */

	public void eg4() {

		Environment myDbEnvironment = null;

		Database myDatabase = null;

		try {

			EnvironmentConfig envConfig = new EnvironmentConfig();

			envConfig.setAllowCreate(true);

			myDbEnvironment = new Environment(new File("/export/dbEnv"),
					envConfig);

			DatabaseConfig dbConfig = new DatabaseConfig();

			dbConfig.setAllowCreate(true); // 设置允许创建与否，默认值是false，打开不存在的数据库会报错。true的时候，数据库不存在就创建。

			// dbConfig.setBtreeComparator();//设置B树比较器，用来比较两个记录的key是否相同。

			// dbConfig.setDuplicateComparator();//设置允许key重复的比较器。

			dbConfig.setSortedDuplicates(false);// 设置为true，允许key重复，false的话，put一个存在key的记录会产生错误。如果使用了关联了多个索引则一定不能支持重复的记录。

			// dbConfig.setExclusiveCreate(false);//如果true，只能创建，如果存在，则打开失败If
			// true, the database open fails if the database currently exists.
			// That is, the open must result in the creation of a new database.
			// Default is false.

			// dbConfig.setReadOnly(true);//设置是否只读

			// dbConfig.setTransactional(true);//设置是否支持事务

			dbConfig.setDeferredWrite(true); // true为进行缓冲写库，false则不进行缓冲写库

			myDatabase = myDbEnvironment.openDatabase(null, "sampleDatabase",
					dbConfig);

			String dbName = myDatabase.getDatabaseName();// 得到数据库的名字

			Environment theEnv = myDatabase.getEnvironment();// 得到当前数据库环境

			myDatabase.preload(1024 * 1024); // 预先加载数据到内存，long型参数表示要装载到内存的数据的最大数

			// long类型的数据存储方法

			try {

				String aKey = "myLong";

				DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));

				Long myLong = new Long(123456789l);

				DatabaseEntry theData = new DatabaseEntry();

				EntryBinding myBinding = TupleBinding
						.getPrimitiveBinding(Long.class);

				myBinding.objectToEntry(myLong, theData);

				// 存储long类型的数据

				myDatabase.put(null, theKey, theData);

				OperationStatus retVal = myDatabase.get(null, theKey, theData,
						LockMode.DEFAULT);
				String retKey = null;

				if (retVal == OperationStatus.SUCCESS) {

					// 取得long类型的数据

					Long theLong = (Long) myBinding.entryToObject(theData);

					retKey = new String(theKey.getData(), "UTF-8");

					System.out.println("For key: '" + retKey
							+ "' found Long: '" +

							theLong + "'.");

				} else {

					System.out.println("No record found for key '" + retKey
							+ "'.");

				}

			} catch (Exception e) {

				// Exception handling goes here

			}

			// implements Serializable 的对象的存储

			try {

				String aKey = "myLong";

				DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));

				// 写对象

				java.util.Vector data2Store = new java.util.Vector(); // 假设他是implements
																		// Serializable

				StoredClassCatalog classCatalog = new StoredClassCatalog(
						myDatabase);

				EntryBinding dataBinding = new SerialBinding(classCatalog,
						java.util.Vector.class);// 指定类型

				DatabaseEntry theData = new DatabaseEntry();

				dataBinding.objectToEntry(data2Store, theData);// 绑定数据

				myDatabase.put(null, theKey, theData);

				// 读对象

				myDatabase.get(null, theKey, theData, LockMode.DEFAULT);

				java.util.Vector retrievedData = (java.util.Vector) dataBinding
						.entryToObject(theData);

			} catch (Exception e) {

				// Exception handling goes here

			}

			// 做一些处理

			myDatabase.sync(); // 当写完一定量的数据以后，同步要硬盘中

		} catch (DatabaseException dbe) {

			// Exception handling goes here

		}

		// 关闭数据库

		try {

			if (myDatabase != null) {

				myDatabase.close();

			}

			if (myDbEnvironment != null) {

				myDbEnvironment.close();

			}

		} catch (DatabaseException dbe) {

			// Exception handling goes here

		}

	}

	/**
	 * 
	 * Data Persistence 持久化
	 * 
	 * 如果是在内存中修了数据，需要写到磁盘。
	 * 
	 * 怕因为系统错误引发数据丢失，可以使用transaction，每commit一次，修改都会被保存。
	 * 
	 * 只是用来存放应用临时数据的话，就不需要用transaction了。
	 * 
	 * 在每次关闭env的时候会执行，也可以在程序中调用执行。
	 */

}