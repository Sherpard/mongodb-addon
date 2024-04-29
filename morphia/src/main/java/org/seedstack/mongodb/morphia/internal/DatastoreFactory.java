/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import static org.seedstack.mongodb.morphia.internal.MorphiaUtils.createDatastoreAnnotation;
import static org.seedstack.mongodb.morphia.internal.MorphiaUtils.getMongoClientConfig;

import javax.inject.Inject;

import org.seedstack.mongodb.morphia.MorphiaDatastore;
import org.seedstack.seed.Application;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.mongodb.client.MongoClient;

import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class DatastoreFactory {
    private final Application application;
    private final Injector injector;
    private final SeedEntityListener seedEntityListener;

    @Inject
    DatastoreFactory(Application application, Injector injector, SeedEntityListener seedEntityListener) {
        this.application = application;
        this.injector = injector;
        this.seedEntityListener = seedEntityListener;
    }

    public Datastore createDatastore(Class<?> morphiaClass) {
        return createDatastore(createDatastoreAnnotation(application, morphiaClass));
    }

    public Datastore createDatastore(MorphiaDatastore datastoreAnnotation) {
        return createDatastore(datastoreAnnotation.clientName(), datastoreAnnotation.dbName());
    }

    public Datastore createDatastore(String clientName, String dbName) {
        MongoClient client = injector.getInstance(Key.get(MongoClient.class, Names.named(clientName)));
        String dbAlias = MorphiaUtils.resolveDatabaseAlias(getMongoClientConfig(application, clientName), dbName);

        return new DatastoreWrapper(Morphia.createDatastore(client, dbAlias), seedEntityListener);
    }
}
