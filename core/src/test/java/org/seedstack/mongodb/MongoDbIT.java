/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import javax.inject.Inject;
import javax.inject.Named;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class MongoDbIT {
    @Inject
    @Named("client1")
    MongoClient client1;

    @Inject
    @Named("db1")
    MongoDatabase db1;

    @Inject
    MongoClient implicitClient;

    @Inject
    MongoDatabase implicitDatabase;

    @Test
    public void mongo_clients_are_injectable() {
        assertThat(client1).isNotNull();
        assertThat(implicitClient).isNotNull();
    }

    @Test
    public void mongo_databases_are_injectable() {
        assertThat(db1).isNotNull();
        assertThat(implicitDatabase).isNotNull();
    }

    @Test
    public void test_insert_into_collection() {
        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102));

        db1.getCollection("test1").insertOne(doc);
    }
}
