/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */

package org.seedstack.mongodb.morphia.internal;

import java.util.Collection;

import org.seedstack.mongodb.morphia.MorphiaDatastore;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;

import dev.morphia.Datastore;

class MorphiaModule extends AbstractModule {
    private final Collection<MorphiaDatastore> morphiaDatastoresAnnotation;
    private final Collection<Class<?>> morphiaEntityListeners;

    MorphiaModule(Collection<MorphiaDatastore> morphiaDatastoresAnnotation,
            Collection<Class<?>> morphiaEntityListeners) {
        super();
        this.morphiaDatastoresAnnotation = morphiaDatastoresAnnotation;
        this.morphiaEntityListeners = morphiaEntityListeners;

    }

    @Override
    protected void configure() {
        bind(DatastoreFactory.class);
        if (morphiaDatastoresAnnotation != null && !morphiaDatastoresAnnotation.isEmpty()) {
            for (MorphiaDatastore morphiaDatastore : morphiaDatastoresAnnotation) {
                DatastoreProvider datastoreProvider = new DatastoreProvider(morphiaDatastore);
                requestInjection(datastoreProvider);
                bind(Key.get(Datastore.class, morphiaDatastore)).toProvider(datastoreProvider).in(Scopes.SINGLETON);
            }
        }

        this.morphiaEntityListeners.forEach(this::requestInjection);
    }
}
