package com.sleepycat.berkeley.spring.boot;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

/**
 * Template simplifying common Berkeley DB (JE) key/value operations such as writing,
 * reading, deleting and iterating records within a managed environment.
 *
 * @author <a href="https://github.com/loong10k">@Loong Wan</a>
 * @since 1.0.0
 */
public class BerkeleyTemplate {

	@Autowired
	private Database myDatabase;
	@Autowired
	private Environment myDbEnvironment;

	/**
	 * Writes a key/value pair into the database, optionally overwriting an existing
	 * value for the same key.
	 * @param key the record key
	 * @param value the record value
	 * @param isOverwrite whether to overwrite when the key already exists
	 */
    public void writeToDatabase(String key, String value, boolean isOverwrite){
        try {
            //JE的记录包含两部分，key键值和value数据值，这两个值都是通过DatabaseEntry对象封装起来的
            //所以说如果要使用记录，则必须创建两个DatabaseEntry对象，一个是key，一个是value
            //DatabaseEntry内部使用的是bytes数组
            DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes("utf8"));
            DatabaseEntry databaseValue = new DatabaseEntry(value.trim().getBytes("utf8"));

            OperationStatus res = null;//操作状态码
            Transaction txn = null;//事务对象

            TransactionConfig txConfig = new TransactionConfig();//事务配置
            txConfig.setSerializableIsolation(true);//设置串行化隔离级别

            txn = myDbEnvironment.beginTransaction(null, txConfig);//开始事物

            if(isOverwrite)
                //添加一条记录。如数据库不支持一个key对应多个data或当前数据库中已经存在该key了，则使用此方法将使用新的值覆盖旧的值。
                res = myDatabase.put(txn, databaseKey, databaseValue);
            else
                //不管数据库是否允许支持多重记录(一个key对应多个value),只要存在该key就不允许添加，并且返回perationStatus.KEYEXIST信息
                res = myDatabase.putNoOverwrite(txn, databaseKey, databaseValue);

            txn.commit();//提交事务

            if(res == OperationStatus.SUCCESS)
                System.out.println("insert success");
            else if(res == OperationStatus.KEYEXIST)
                System.out.println("key exist");
            else
                System.out.println("insert fail");

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Iterates all records in the database and returns their keys.
     * @return the list of record keys
     * @throws UnsupportedEncodingException if the UTF-8 encoding is not available
     */
    public ArrayList<String> getAllFromDatabase() throws UnsupportedEncodingException{
        Cursor myCursor = null;//游标
        ArrayList<String> resultList = new ArrayList<String>();
        Transaction txn = null;

         txn = myDbEnvironment.beginTransaction(null, null);
         CursorConfig cc = new CursorConfig();//游标配置
         cc.setReadCommitted(true);//设置隔离级别

         if(myCursor==null)
             myCursor = myDatabase.openCursor(txn, cc);

         DatabaseEntry entryKey = new DatabaseEntry();
         DatabaseEntry entryValue = new DatabaseEntry(); 

         if(myCursor.getFirst(entryKey, entryValue, LockMode.DEFAULT) == OperationStatus.SUCCESS){
             String key = new String(entryKey.getData(), "UTF-8");
             resultList.add(key);
             while (myCursor.getNext(entryKey, entryValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) 
             {
                 key = new String(entryKey.getData(), "UTF-8");
                 resultList.add(key);
             }
         }

         myCursor.close();

         txn.commit();
         return resultList;
    }
    /**
     * Reads the value associated with the given key from the database.
     * @param key the record key to look up
     * @return the stored value, or an empty string when not found
     */
    public String readFromDatabase(String key){
        try {
            DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes("utf8"));
            DatabaseEntry databaseValue = new DatabaseEntry();
            Transaction txn = null;//事务对象

            TransactionConfig txConfig = new TransactionConfig();//事务配置
            txConfig.setSerializableIsolation(true);//设置串行化隔离级别

            txn = myDbEnvironment.beginTransaction(null, txConfig);//开始事务
            OperationStatus res = myDatabase.get(txn, databaseKey, databaseValue, LockMode.DEFAULT);

            txn.commit();//提交事务
            if(res == OperationStatus.SUCCESS){
                byte[] retData = databaseValue.getData();
                String foundData = new String(retData, "utf8");
                return foundData;
            }else{
                return "";
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    /**
     * Deletes the record identified by the given key from the database.
     * @param key the record key to delete
     */
    public void deleteFromDatabase(String key){
        Transaction txn = null;

        TransactionConfig txConfig = new TransactionConfig();
        txConfig.setSerializableIsolation(true);

        txn = myDbEnvironment.beginTransaction(null, txConfig);
        DatabaseEntry databaseKey = null;
        try {
            databaseKey = new DatabaseEntry(key.trim().getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        OperationStatus res = myDatabase.delete(txn, databaseKey);
        txn.commit();

        if(res == OperationStatus.SUCCESS)
            System.out.println("delete success");
        else if(res == OperationStatus.KEYEMPTY)
            System.out.println("no key");
        else
            System.out.println("delete fail");
    }

    /**
     * Demonstrates miscellaneous database management operations such as retrieving the
     * database name, listing databases, renaming, removing and truncating.
     */
    public void otherMethod(){
        String databaseName = myDatabase.getDatabaseName();//数据库名字
        System.out.println("databaseName : " + databaseName);

        Environment env = myDatabase.getEnvironment();//取得当前数据库的环境信息
        System.out.println(env);

        List<String> list = myDbEnvironment.getDatabaseNames();//取得当前环境下数据库名称列表
        System.out.println(list);

        env.renameDatabase(null, databaseName, "newName");//给数据库改名
        env.removeDatabase(null, databaseName);//删除当前环境数据库

        long deleteNum = env.truncateDatabase(null, databaseName, true);//清空数据库中所有记录，并返回数量
        System.out.println(deleteNum);



    }
    
    /**
     * Writes a primitive (tuple-bound) value into the database using a tuple binding.
     * @param key the record key
     * @param value the value to store
     */
    @SuppressWarnings("unchecked")
    public void writePrimitiveDatabase(String key, String value){
        try {
            DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes("utf8"));
            DatabaseEntry databaseValue = new DatabaseEntry();

            @SuppressWarnings("rawtypes")
            EntryBinding myBinding = TupleBinding.getPrimitiveBinding(String.class);
            myBinding.objectToEntry(value, databaseValue);
            myDatabase.put(null, databaseKey, databaseValue);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Reads and prints the primitive (tuple-bound) value associated with the given key.
     * @param key the record key to look up
     */
    public void readPrimitiveDatabase(String key){
        try {
            DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes("utf8"));
            DatabaseEntry databaseValue = new DatabaseEntry();

            @SuppressWarnings("rawtypes")
            EntryBinding myBinding = TupleBinding.getPrimitiveBinding(String.class);

            OperationStatus retVal = myDatabase.get(null, databaseKey, databaseValue,  LockMode.DEFAULT);

            if(retVal == OperationStatus.SUCCESS){
                String value = (String)myBinding.entryToObject(databaseValue);
                System.out.println(value);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
	
	/**
	 * Returns the underlying database.
	 * @return the database
	 */
	public Database getDatabase() {
		return myDatabase;
	}

	/**
	 * Sets the underlying database.
	 * @param database the database to use
	 */
	public void setDatabase(Database database) {
		this.myDatabase = database;
	}
	
}
