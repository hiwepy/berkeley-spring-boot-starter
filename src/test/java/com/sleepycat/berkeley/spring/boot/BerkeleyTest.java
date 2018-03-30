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
package com.sleepycat.berkeley.spring.boot;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jeefw.io.utils.IOUtils;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class BerkeleyTest {
	
	private Environment myEnv;
    private EntityStore store;
    private PrimaryIndex<String, Inventory> inventoryBySku;
    private PrimaryIndex<String, Vendor> vendorByName;
    private SecondaryIndex<String, String, Inventory> inventoryByName;

    /* Employer accessors */
    PrimaryIndex<Long, Employer> employerById;
    SecondaryIndex<String, Long, Employer> employerByName;
    EnvironmentConfig
    /* Person accessors */
    PrimaryIndex<String, Person> personBySsn;
    SecondaryIndex<String, String, Person> personByParentSsn;
    SecondaryIndex<String, String, Person> personByEmailAddresses;
    SecondaryIndex<Long, String, Person> personByEmployerIds;


    private File envHome = new File(System.getProperty("user.dir") + File.separator + "bdb");
    private boolean readOnly = false;

    @Before
    public void prepare() {
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        myEnvConfig.setReadOnly(readOnly);
        storeConfig.setReadOnly(readOnly);

        // If the environment is opened for write, then we want to be
        // able to create the environment and entity store if
        // they do not exist.
        myEnvConfig.setAllowCreate(!readOnly);
        storeConfig.setAllowCreate(!readOnly);

        // Open the environment and entity store
        System.out.println(envHome.getAbsolutePath());
        if (!envHome.exists()) {
            envHome.mkdir();
        }
        myEnv = new Environment(envHome, myEnvConfig);
        store = new EntityStore(myEnv, "EntityStore", storeConfig);

        // Primary key for Inventory classes
        inventoryBySku = store.getPrimaryIndex(String.class, Inventory.class);
        // Secondary key for Inventory classes
        // Last field in the getSecondaryIndex() method must be
        // the name of a class member; in this case, an Inventory.class
        // data member.
        inventoryByName = store.getSecondaryIndex(inventoryBySku, String.class, "itemName");
        // Primary key for Vendor class
        vendorByName = store.getPrimaryIndex(String.class, Vendor.class);

        employerById = store.getPrimaryIndex(Long.class, Employer.class);
        employerByName = store.getSecondaryIndex(employerById, String.class, "name");

        personBySsn = store.getPrimaryIndex(String.class, Person.class);
        personByParentSsn = store.getSecondaryIndex(personBySsn, String.class, "parentSsn");
        personByEmailAddresses = store.getSecondaryIndex(personBySsn, String.class, "emailAddresses");
        personByEmployerIds = store.getSecondaryIndex(personBySsn, Long.class, "employerIds");

    }
    
    //添加数据
    @Test
    public void putData() throws IOException {
    	
        List<String> readLines = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream("vendors.txt"), Charsets.UTF_8);
        for (String data : readLines) {
            String[] sArray = data.split("#");
            Vendor theVendor = new Vendor();
            theVendor.setVendorName(sArray[0]);
            theVendor.setAddress(sArray[1]);
            theVendor.setCity(sArray[2]);
            theVendor.setState(sArray[3]);
            theVendor.setZipcode(sArray[4]);
            theVendor.setBusinessPhoneNumber(sArray[5]);
            theVendor.setRepName(sArray[6]);
            theVendor.setRepPhoneNumber(sArray[7]);
            // Put it in the store. Because we do not explicitly set
            // a transaction here, and because the store was opened
            // with transactional support, auto commit is used for each
            // write to the store.
            vendorByName.put(theVendor);
        }

        // Primary key for Inventory classes
        PrimaryIndex<String, Inventory> inventoryBySku = store.getPrimaryIndex(String.class, Inventory.class);
        List<String> data = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream("inventory.txt"), Charsets.UTF_8);
        for (String row : data) {
            String[] sArray = row.split("#");
            Inventory theInventory = new Inventory();
            theInventory.setItemName(sArray[0]);
            theInventory.setSku(sArray[1]);
            theInventory.setVendorPrice((new Float(sArray[2])).floatValue());
            theInventory.setVendorInventory((new Integer(sArray[3])).intValue());
            theInventory.setCategory(sArray[4]);
            theInventory.setVendor(sArray[5]);
            // Put it in the store. Note that this causes our secondary key
            // to be automatically updated for us.
            inventoryBySku.put(theInventory);
        }
    }
    
    //查询数据
    @Test
    public void getInventoryData() {
        // Use the inventory name secondary key to retrieve
        // these objects.
        EntityCursor<Inventory> items =
                inventoryByName.subIndex("Oranges").entities();
        try {
            for (Inventory item : items) {
                System.out.println(ToStringBuilder.reflectionToString(item));
            }
        } finally {
            items.close();
        }
    }

    @Test
    public void getAllInventory() {
        // Get a cursor that will walk every
        // inventory object in the store.
        EntityCursor<Inventory> items = inventoryBySku.entities();

        try {
            for (Inventory item : items) {
                System.out.println(ToStringBuilder.reflectionToString(item));
            }
        } finally {
            items.close();
        }
    }
    
    //更新:如果不开启允许重复记录的话，put就是更新
    @Test
    public void update() {
        String pk = "apple-for-update";
        Inventory theInventory = new Inventory();
        theInventory.setItemName("Apples");
        theInventory.setSku(pk);
        theInventory.setVendorPrice(1.20f);
        theInventory.setVendorInventory(728);
        theInventory.setCategory("fruits");
        theInventory.setVendor("Off the Vine");

        inventoryBySku.put(theInventory);

        Inventory inventory = inventoryBySku.get(pk);
        System.out.println(ToStringBuilder.reflectionToString(inventory));

        inventory.setVendor("vendor update");
        inventoryBySku.put(inventory);

        System.out.println(ToStringBuilder.reflectionToString(inventoryBySku.get(pk)));
    }
    
    //删除
    @Test
    public void delete() {
    	
        String pk = "apple-for-update";
        Inventory theInventory = new Inventory();
        theInventory.setItemName("Apples");
        theInventory.setSku(pk);
        theInventory.setVendorPrice(1.20f);
        theInventory.setVendorInventory(728);
        theInventory.setCategory("fruits");
        theInventory.setVendor("Off the Vine");

        inventoryBySku.put(theInventory);

        Inventory inventory = inventoryBySku.get(pk);
        System.out.println(ToStringBuilder.reflectionToString(inventory));

        boolean rs = inventoryBySku.delete(pk);
        Assert.assertTrue(rs);

        Assert.assertNull(inventoryBySku.get(pk));

    }
    
    //统计
    @Test
    public void count(){
        EntityCursor<Employer> cursor = null;
        try{
            cursor = employerById.entities();
            int count = -1;
            if(cursor.next() != null){
                count = cursor.count();
            }
            System.out.println("employee count:" + count);
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
    }
    
    //级联及主键自增情况
    @Test
    public void sequencePk() {

        /*
         * Add a parent and two children using the Person primary index.
         * Specifying a non-null parentSsn adds the child Person to the
         * sub-index of children for that parent key.
         */
        personBySsn.put(new Person("Bob Smith", "111-11-1111", null));
        personBySsn.put(new Person("Mary Smith", "333-33-3333", "111-11-1111"));
        personBySsn.put(new Person("Jack Smith", "222-22-2222", "111-11-1111"));

        /* Print the children of a parent using a sub-index and a cursor. */
        EntityCursor<Person> children = personByParentSsn.subIndex("111-11-1111").entities();
        try {
            for (Person child : children) {
                System.out.println(child.getSsn() + ' ' + child.getName());
            }
        } finally {
            children.close();
        }

        /* Get Bob by primary key using the primary index. */
        Person bob = personBySsn.get("111-11-1111");
        Assert.assertNotNull(bob);

        /*
         * Create two employers if they do not already exist.  Their primary
         * keys are assigned from a sequence.
         */
        Employer gizmoInc = employerByName.get("Gizmo Inc");
        if (gizmoInc == null) {
            gizmoInc = new Employer("Gizmo Inc");
            employerById.put(gizmoInc);
        }
        Employer gadgetInc = employerByName.get("Gadget Inc");
        if (gadgetInc == null) {
            gadgetInc = new Employer("Gadget Inc");
            employerById.put(gadgetInc);
        }

        /* Bob has two jobs and two email addresses. */
        bob.getEmployerIds().add(gizmoInc.getId());
        bob.getEmployerIds().add(gadgetInc.getId());

        bob.getEmailAddresses().add("bob@bob.com");
        bob.getEmailAddresses().add("bob@gmail.com");

        /* Update Bob's record. */
        personBySsn.put(bob);

        /* Bob can now be found by both email addresses. */
        bob = personByEmailAddresses.get("bob@bob.com");
        Assert.assertNotNull(bob);
        bob = personByEmailAddresses.get("bob@gmail.com");
        Assert.assertNotNull(bob);

        /* Bob can also be found as an employee of both employers. */
        EntityIndex<String, Person> employees;
        employees = personByEmployerIds.subIndex(gizmoInc.getId());
        Assert.assertTrue( employees.contains("111-11-1111"));
        employees = personByEmployerIds.subIndex(gadgetInc.getId());
        Assert.assertTrue(employees.contains("111-11-1111"));

        /*
         * When an employer is deleted, the onRelatedEntityDelete=NULLIFY for
         * the employerIds key causes the deleted ID to be removed from Bob's
         * employerIds.
         */
        employerById.delete(gizmoInc.getId());
        bob = personBySsn.get("111-11-1111");
        Assert.assertNotNull(bob);
        Assert.assertFalse(bob.getEmployerIds().contains(gizmoInc.getId()));
    }

    @Test
    public void cursor() {
        CursorConfig cc = new CursorConfig();
        // This is ignored if the store is not opened with uncommitted read
        // support.
        cc.setReadUncommitted(true);

        EntityCursor<Employer> employers = employerById.entities(null, cc);
        try{
            for(Employer employer : employers){
                System.out.println(ToStringBuilder.reflectionToString(employer));
            }
        }finally{
            employers.close();
        }
    }
    
    @After
    public void close() {
        if (store != null) {
            try {
                store.close();
            } catch (DatabaseException dbe) {
                dbe.printStackTrace();
            }
        }

        if (myEnv != null) {
            try {
                // Finally, close the store and environment.
                myEnv.close();
            } catch (DatabaseException dbe) {
                dbe.printStackTrace();
            }
        }
    }
}
