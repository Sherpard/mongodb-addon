/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import java.util.Collection;
import java.util.HashSet;

import javax.inject.Inject;

import org.seedstack.mongodb.internal.MongoDbPlugin;
import org.seedstack.mongodb.morphia.MorphiaConfig;
import org.seedstack.mongodb.morphia.MorphiaDatastore;
import org.seedstack.seed.Application;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.init.ValidationManager;
import org.seedstack.seed.core.internal.validation.ValidationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;

/**
 * This plugin manages the MongoDb Morphia object/document mapping library.
 */
public class MorphiaPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(MorphiaPlugin.class);
    private final Collection<MorphiaDatastore> morphiaDatastores = new HashSet<>();
    private final Collection<Class<?>> morphiaListeners = new HashSet<>();
    private MorphiaConfig config;
    @Inject
    private DatastoreFactory datastoreFactory;

    @Override
    public String name() {
        return "morphia";
    }

    @Override
    public Collection<Class<?>> dependencies() {
        return Lists.newArrayList(ValidationPlugin.class, MongoDbPlugin.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .predicate(MorphiaPredicates.PERSISTED_CLASSES)
                .predicate(MorphiaPredicates.ENTITY_LISTENERS)
                .build();
    }

    @Override
    public InitState initialize(InitContext initContext) {
        Application application = getApplication();
        config = application.getConfiguration().get(MorphiaConfig.class);

        boolean validationActive = ValidationManager.get()
                .getValidationLevel() != ValidationManager.ValidationLevel.NONE;

        if (validationActive) {
            LOGGER.info("Validation is enabled on Morphia entities");
        } else {
            LOGGER.info("Validation is disabled on Morphia entities");
        }

        Collection<Class<?>> morphiaPersistedClasses = initContext.scannedTypesByPredicate()
                .get(MorphiaPredicates.PERSISTED_CLASSES);

        if (morphiaPersistedClasses != null && !morphiaPersistedClasses.isEmpty()) {
            LOGGER.info("Creating datastore for {} persisted classes ", morphiaPersistedClasses.size());
            for (Class<?> morphiaClass : morphiaPersistedClasses) {
                MorphiaDatastore morphiaDatastore = MorphiaUtils.createDatastoreAnnotation(application, morphiaClass);
                if (!morphiaDatastores.contains(morphiaDatastore)) {

                    morphiaDatastores.add(morphiaDatastore);
                }
            }
        } else {
            LOGGER.info("No persisted classes found");
        }

        if (!validationActive) {
            return InitState.INITIALIZED;
        }

        
        
        Collection<Class<?>> morphiaEntityListeners = initContext.scannedTypesByPredicate()
                .get(MorphiaPredicates.ENTITY_LISTENERS);

        if (morphiaEntityListeners != null && !morphiaEntityListeners.isEmpty()) {
            LOGGER.info("Found {} entity listeners", morphiaEntityListeners.size());
            morphiaListeners.addAll(morphiaEntityListeners);
        } else {
            LOGGER.info("No entity listeners found");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        if (config.isEnsureCapsAtStartup() || config.isEnsureIndexesAtStartup()) {
            morphiaDatastores.stream()
                    .map(datastoreFactory::createDatastore)
                    .forEach(morphiaDatastore -> {
                        if (config.isEnsureIndexesAtStartup()) {
                            morphiaDatastore.ensureIndexes();
                        }
                        if (config.isEnsureCapsAtStartup()) {
                            morphiaDatastore.ensureCaps();
                        }
                    });
        }
    }

    @Override
    public Object nativeUnitModule() {
        return new MorphiaModule(morphiaDatastores, morphiaListeners);
    }
}
