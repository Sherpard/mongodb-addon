/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import org.seedstack.mongodb.MongoDbConfig;
import org.seedstack.mongodb.morphia.MorphiaDatastore;
import org.seedstack.seed.Application;
import org.seedstack.seed.ClassConfiguration;
import org.seedstack.seed.SeedException;

import java.util.Map;

public final class MorphiaUtils {
    private MorphiaUtils() {
        // no instantiation allowed
    }

    /**
     * Returns an instance of the annotation MorphiaDatastore if the morphia configuration is ok.
     *
     * @param application  Application
     * @param morphiaClass persistent morphia object
     * @return MorphiaDatastore
     */
    static MorphiaDatastore createDatastoreAnnotation(Application application, Class<?> morphiaClass) {
        ClassConfiguration<?> morphiaEntityConfiguration = application.getConfiguration(morphiaClass);
        if (morphiaEntityConfiguration.isEmpty()) {
            throw SeedException.createNew(MorphiaErrorCode.PERSISTED_CLASS_NOT_CONFIGURED)
                    .put("aggregate", morphiaClass.getName());
        }

        String clientName = morphiaEntityConfiguration.get("mongoDbClient");
        if (clientName == null) {
            throw SeedException.createNew(MorphiaErrorCode.CLIENT_NAME_NOT_CONFIGURED)
                    .put("aggregate", morphiaClass.getName());
        }

        String dbName = morphiaEntityConfiguration.get("mongoDbDatabase");
        if (dbName == null) {
            throw SeedException.createNew(MorphiaErrorCode.DATABASE_NOT_CONFIGURED)
                    .put("aggregate", morphiaClass.getName());
        }

        checkMongoClient(getMongoClientConfig(application, clientName), morphiaClass, clientName, dbName);

        return new DatastoreImpl(clientName, dbName);
    }

    /**
     * Resolve the real database name given an alias.
     *
     * @param clientConfig the configuration of the client.
     * @param dbName       the name of the alias or the database.
     * @return the resolved database name (may be the provided database name if no alias is defined).
     */
    static String resolveDatabaseAlias(MongoDbConfig.ClientConfig clientConfig, String dbName) {
        for (Map.Entry<String, MongoDbConfig.ClientConfig.DatabaseConfig> databaseEntry : clientConfig.getDatabases().entrySet()) {
            if (dbName.equals(databaseEntry.getValue().getAlias())) {
                return databaseEntry.getKey();
            }
        }
        return dbName;
    }

    /**
     * Retrieve the configuration of a specific MongoDb client.
     *
     * @param application The application object.
     * @param clientName  The name of the configured MongoDb client.
     * @return the client configuration.
     */
    static MongoDbConfig.ClientConfig getMongoClientConfig(Application application, String clientName) {
        MongoDbConfig.ClientConfig clientConfig = application.getConfiguration().get(MongoDbConfig.class).getClients().get(clientName);
        if (clientConfig == null) {
            throw SeedException.createNew(MorphiaErrorCode.UNKNOWN_CLIENT)
                    .put("clientName", clientName);
        }
        return clientConfig;
    }

    static void checkMongoClient(MongoDbConfig.ClientConfig clientConfig, Class<?> mappedClass, String clientName, String dbName) {
        boolean async = clientConfig.isAsync();
        if (async) {
            throw SeedException.createNew(MorphiaErrorCode.ASYNC_CLIENT_NOT_SUPPORTED)
                    .put("aggregate", mappedClass.getName())
                    .put("clientName", clientName);
        }

        boolean found = false;
        for (String nameToCheck : clientConfig.getDatabases().keySet()) {
            if (nameToCheck.equals(resolveDatabaseAlias(clientConfig, dbName))) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw SeedException.createNew(MorphiaErrorCode.UNKNOWN_DATABASE)
                    .put("aggregate", mappedClass.getName())
                    .put("clientName", clientName)
                    .put("dbName", dbName);
        }
    }
}
