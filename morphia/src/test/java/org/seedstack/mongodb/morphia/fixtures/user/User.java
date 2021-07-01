/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.fixtures.user;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.seedstack.business.domain.AggregateRoot;

import javax.validation.constraints.NotNull;

@Entity
public class User implements AggregateRoot<Long> {
    public User() {
    }

    public User(long id, String name, String lastName, Address address) {
        this.id = id;
        this.name = name;
        this.lastname = lastName;
        this.address = address;
    }

    @Id
    private long id;

    @NotNull
    private String name;

    private String lastname;

    private Address address;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
