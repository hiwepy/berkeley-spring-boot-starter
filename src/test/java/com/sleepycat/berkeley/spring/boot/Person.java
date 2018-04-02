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

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/* An entity class. */
@Entity
public class Person {

    @PrimaryKey
    String ssn;

    String name;
    Address address;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = Person.class)
    String parentSsn;

    @SecondaryKey(relate = Relationship.ONE_TO_MANY)
    Set<String> emailAddresses = new HashSet<String>();

    @SecondaryKey(relate = Relationship.MANY_TO_MANY,
            relatedEntity = Employer.class,
            onRelatedEntityDelete = DeleteAction.NULLIFY)
    Set<Long> employerIds = new HashSet<Long>();

    public Person(String name, String ssn, String parentSsn) {
        this.name = name;
        this.ssn = ssn;
        this.parentSsn = parentSsn;
    }

    private Person() {
    } // For deserialization

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getParentSsn() {
        return parentSsn;
    }

    public void setParentSsn(String parentSsn) {
        this.parentSsn = parentSsn;
    }

    public Set<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(Set<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public Set<Long> getEmployerIds() {
        return employerIds;
    }

    public void setEmployerIds(Set<Long> employerIds) {
        this.employerIds = employerIds;
    }
}