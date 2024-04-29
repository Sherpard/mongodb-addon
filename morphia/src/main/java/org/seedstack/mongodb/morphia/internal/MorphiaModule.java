/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
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

import org.seedstack.mongodb.morphia.EntityListener;
import org.seedstack.mongodb.morphia.MorphiaDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

import dev.morphia.Datastore;

class MorphiaModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(MorphiaModule.class);
    private final Collection<MorphiaDatastore> morphiaDatastoresAnnotation;
    private final Collection<Class<? extends EntityListener<?>>> seedEntityListeners;

    MorphiaModule(Collection<MorphiaDatastore> morphiaDatastoresAnnotation,
            Collection<Class<? extends EntityListener<?>>> seedEntityListeners) {
        super();
        this.morphiaDatastoresAnnotation = morphiaDatastoresAnnotation;
        this.seedEntityListeners = seedEntityListeners;

    }

    @SuppressWarnings("rawtypes")
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

        Multibinder<EntityListener> entityListenerMultibinder = Multibinder.newSetBinder(binder(),
                EntityListener.class);

        for (Class<? extends EntityListener> listener : seedEntityListeners) {
            LOGGER.debug("Binding {}", listener.toGenericString());
            entityListenerMultibinder.addBinding().to(listener);
        }

        bind(SeedEntityListener.class).asEagerSingleton();

    }
}
