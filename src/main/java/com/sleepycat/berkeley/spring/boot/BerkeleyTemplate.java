package com.sleepycat.berkeley.spring.boot;

import java.nio.charset.StandardCharsets;
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
        DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes(StandardCharsets.UTF_8));
        DatabaseEntry databaseValue = new DatabaseEntry(value.trim().getBytes(StandardCharsets.UTF_8));

        OperationStatus res = null;
        Transaction txn = null;

        TransactionConfig txConfig = new TransactionConfig();
        txConfig.setSerializableIsolation(true);

        txn = myDbEnvironment.beginTransaction(null, txConfig);

        if(isOverwrite)
            res = myDatabase.put(txn, databaseKey, databaseValue);
        else
            res = myDatabase.putNoOverwrite(txn, databaseKey, databaseValue);

        txn.commit();

        if(res == OperationStatus.SUCCESS)
            System.out.println("insert success");
        else if(res == OperationStatus.KEYEXIST)
            System.out.println("key exist");
        else
            System.out.println("insert fail");
    }

    /**
     * Iterates all records in the database and returns their keys.
     * @return the list of record keys
     */
    public ArrayList<String> getAllFromDatabase() {
        Cursor myCursor = null;
        ArrayList<String> resultList = new ArrayList<String>();
        Transaction txn = null;

         txn = myDbEnvironment.beginTransaction(null, null);
         CursorConfig cc = new CursorConfig();
         cc.setReadCommitted(true);

         if(myCursor==null)
             myCursor = myDatabase.openCursor(txn, cc);

         DatabaseEntry entryKey = new DatabaseEntry();
         DatabaseEntry entryValue = new DatabaseEntry();

         if(myCursor.getFirst(entryKey, entryValue, LockMode.DEFAULT) == OperationStatus.SUCCESS){
             String key = new String(entryKey.getData(), StandardCharsets.UTF_8);
             resultList.add(key);
             while (myCursor.getNext(entryKey, entryValue, LockMode.DEFAULT) == OperationStatus.SUCCESS)
             {
                 key = new String(entryKey.getData(), StandardCharsets.UTF_8);
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
        DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes(StandardCharsets.UTF_8));
        DatabaseEntry databaseValue = new DatabaseEntry();
        Transaction txn = null;

        TransactionConfig txConfig = new TransactionConfig();
        txConfig.setSerializableIsolation(true);

        txn = myDbEnvironment.beginTransaction(null, txConfig);
        OperationStatus res = myDatabase.get(txn, databaseKey, databaseValue, LockMode.DEFAULT);

        txn.commit();
        if(res == OperationStatus.SUCCESS){
            byte[] retData = databaseValue.getData();
            String foundData = new String(retData, StandardCharsets.UTF_8);
            return foundData;
        }else{
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
        DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes(StandardCharsets.UTF_8));
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
     * Writes a primitive (tuple-bound) value into the database using a tuple binding.
     * @param key the record key
     * @param value the value to store
     */
    @SuppressWarnings("unchecked")
    public void writePrimitiveDatabase(String key, String value){
        DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes(StandardCharsets.UTF_8));
        DatabaseEntry databaseValue = new DatabaseEntry();

        @SuppressWarnings("rawtypes")
        EntryBinding myBinding = TupleBinding.getPrimitiveBinding(String.class);
        myBinding.objectToEntry(value, databaseValue);
        myDatabase.put(null, databaseKey, databaseValue);
    }

    /**
     * Reads and prints the primitive (tuple-bound) value associated with the given key.
     * @param key the record key to look up
     */
    public void readPrimitiveDatabase(String key){
        DatabaseEntry databaseKey = new DatabaseEntry(key.trim().getBytes(StandardCharsets.UTF_8));
        DatabaseEntry databaseValue = new DatabaseEntry();

        @SuppressWarnings("rawtypes")
        EntryBinding myBinding = TupleBinding.getPrimitiveBinding(String.class);

        OperationStatus retVal = myDatabase.get(null, databaseKey, databaseValue,  LockMode.DEFAULT);

        if(retVal == OperationStatus.SUCCESS){
            String value = (String)myBinding.entryToObject(databaseValue);
            System.out.println(value);
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
