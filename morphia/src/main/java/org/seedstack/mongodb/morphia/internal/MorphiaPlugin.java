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
import org.seedstack.mongodb.morphia.EntityListener;
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
    private final Collection<Class<? extends EntityListener<?>>> seedEntityListeners = new HashSet<>();

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
                .predicate(MorphiaPredicates.SEED_INJECTED_ENTITY_LISTENERS)
                .predicate(MorphiaPredicates.MORPHIA_RAW_ENTITY_LISTENERS)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState initialize(InitContext initContext) {
        Application application = getApplication();
        config = application.getConfiguration().get(MorphiaConfig.class);

        boolean isValidationActive = ValidationManager.get()
                .getValidationLevel() != ValidationManager.ValidationLevel.NONE;

        if (isValidationActive) {
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

        Collection<Class<?>> morphiaListeners = initContext.scannedTypesByPredicate()
                .get(MorphiaPredicates.MORPHIA_RAW_ENTITY_LISTENERS);

        if (morphiaListeners != null && morphiaListeners.size() > 0) {
            LOGGER.warn(
                    "Morphia EntityListeners found. No injection will be performed over those EntityListeners." +
                            "Activate debug logs to get more information");
            if (LOGGER.isDebugEnabled()) {
                morphiaListeners.forEach(
                        listener -> LOGGER.debug("\t class '{}' will not have injection", listener.toGenericString()));
            }

        }

        Collection<Class<?>> seedListeners = initContext.scannedTypesByPredicate()
                .get(MorphiaPredicates.SEED_INJECTED_ENTITY_LISTENERS);

        if (seedListeners != null && seedListeners.size() > 0) {
            for (Class<?> listener : seedListeners) {
                if (!isValidationActive && listener.equals(ValidatingEntityInterceptor.class)) {
                    continue;
                }
                LOGGER.debug("Detected '{}' EntityListener", listener.toGenericString());
                this.seedEntityListeners.add((Class<? extends EntityListener<?>>) listener);
            }
        }

        LOGGER.info("{} EntityListeners loaded", seedListeners.size());

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
        return new MorphiaModule(morphiaDatastores, seedEntityListeners);
    }
}
