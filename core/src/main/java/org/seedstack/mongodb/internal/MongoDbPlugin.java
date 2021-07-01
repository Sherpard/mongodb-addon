/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.internal;

import com.google.inject.AbstractModule;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.seedstack.coffig.Coffig;
import org.seedstack.mongodb.MongoDbConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MongoDbPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbPlugin.class);

    private static class SyncHolder {
        private static final MongoDbManager INSTANCE = new SyncMongoDbManager();
    }

    private static class AsyncHolder {
        private static final MongoDbManager INSTANCE = new AsyncMongoDbManager();
    }

    private boolean hasSyncClients = false;
    private boolean hasAsyncClients = false;

    @Override
    public String name() {
        return "mongodb";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState initialize(InitContext initContext) {
        Coffig coffig = getConfiguration();
        MongoDbConfig mongoDbConfig = getConfiguration(MongoDbConfig.class);
        Set<String> allDbNames = new HashSet<>();

        if (mongoDbConfig.getClients().isEmpty()) {
            LOGGER.info("No Mongo client configured, MongoDB support disabled");
            return InitState.INITIALIZED;
        }

        for (Map.Entry<String, MongoDbConfig.ClientConfig> clientEntry : mongoDbConfig.getClients().entrySet()) {
            String clientName = clientEntry.getKey();
            MongoDbConfig.ClientConfig clientConfig = clientEntry.getValue();

            if (clientConfig.isAsync()) {
                AsyncHolder.INSTANCE.registerClient(clientName, clientConfig, coffig);
                hasAsyncClients = true;
            } else {
                SyncHolder.INSTANCE.registerClient(clientName, clientConfig, coffig);
                hasSyncClients = true;
            }

            for (Map.Entry<String, MongoDbConfig.ClientConfig.DatabaseConfig> dbEntry : clientConfig.getDatabases().entrySet()) {
                String dbName = dbEntry.getKey();
                MongoDbConfig.ClientConfig.DatabaseConfig dbConfig = dbEntry.getValue();
                String alias = Optional.ofNullable(dbConfig.getAlias()).orElse(dbName);

                if (allDbNames.contains(alias)) {
                    throw SeedException.createNew(MongoDbErrorCode.DUPLICATE_DATABASE_NAME)
                            .put("clientName", clientName)
                            .put("dbName", dbEntry);
                } else {
                    allDbNames.add(alias);
                }

                if (clientConfig.isAsync()) {
                    AsyncHolder.INSTANCE.registerDatabase(clientName, dbName, alias);
                } else {
                    SyncHolder.INSTANCE.registerDatabase(clientName, dbName, alias);
                }
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                if (hasSyncClients) {
                    install(SyncHolder.INSTANCE.getModule());
                }

                if (hasAsyncClients) {
                    install(AsyncHolder.INSTANCE.getModule());
                }
            }
        };
    }

    @Override
    public void stop() {
        if (hasSyncClients) {
            SyncHolder.INSTANCE.shutdown();
        }

        if (hasAsyncClients) {
            AsyncHolder.INSTANCE.shutdown();
        }
    }
}
