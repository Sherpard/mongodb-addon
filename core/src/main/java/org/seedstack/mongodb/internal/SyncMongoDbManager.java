/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.internal;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.seedstack.coffig.BuilderSupplier;
import org.seedstack.coffig.Coffig;
import org.seedstack.mongodb.MongoDbConfig;
import org.seedstack.seed.SeedException;

class SyncMongoDbManager extends AbstractMongoDbManager<MongoClient, MongoDatabase> {
    @Override
    protected MongoClient doCreateClient(String clientName, MongoDbConfig.ClientConfig clientConfig, Coffig coffig) {
        AllOptions allOptions = coffig.get(AllOptions.class, String.format("mongoDb.clients.%s", clientName));
        if (clientConfig.isConfiguredByUri()) {
            return new MongoClient(new MongoClientURI(clientConfig.getUri(), allOptions.options.get()));
        } else {
            return createMongoClient(clientName, clientConfig, allOptions.options.get().build());
        }
    }

    @Override
    protected MongoDatabase doCreateDatabase(MongoClient client, String dbName) {
        return client.getDatabase(dbName);
    }

    @Override
    protected void doClose(MongoClient client) {
        client.close();
    }

    private MongoClient createMongoClient(String clientName, MongoDbConfig.ClientConfig clientConfig,
            MongoClientOptions mongoClientOptions) {
        List<ServerAddress> serverAddresses = buildServerAddresses(clientName, clientConfig.getHosts());

        if (serverAddresses.isEmpty()) {
            throw SeedException.createNew(MongoDbErrorCode.MISSING_HOSTS_CONFIGURATION)
                    .put("clientName", clientName);
        }

        List<MongoCredential> mongoCredentials = buildMongoCredentials(clientName, clientConfig.getCredentials());
        if (mongoCredentials.isEmpty()) {
            if (serverAddresses.size() == 1) {
                return new MongoClient(serverAddresses.get(0), mongoClientOptions);
            } else {
                return new MongoClient(serverAddresses, mongoClientOptions);
            }
        } else {
            if (serverAddresses.size() == 1) {
                return new MongoClient(serverAddresses.get(0), mongoCredentials, mongoClientOptions);
            } else {
                return new MongoClient(serverAddresses, mongoCredentials, mongoClientOptions);
            }
        }
    }

    private static class AllOptions {
        private BuilderSupplier<MongoClientOptions.Builder> options = BuilderSupplier.of(MongoClientOptions.builder());
    }
}
