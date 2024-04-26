/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.seedstack.coffig.BuilderSupplier;
import org.seedstack.coffig.Coffig;
import org.seedstack.mongodb.MongoDbConfig;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

class MongoDbManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbManager.class);
    private final Map<String, MongoClient> mongoClients = new HashMap<>();
    private final Map<String, MongoDatabase> mongoDatabases = new HashMap<>();

    protected MongoClient doCreateClient(
            String clientName, MongoDbConfig.ClientConfig clientConfig, Coffig coffig) {
        AllOptions allOptions = coffig.get(AllOptions.class, String.format("mongoDb.clients.%s", clientName));
        if (clientConfig.isConfiguredByUri()) {
            return MongoClients.create(
                    allOptions.options
                            .get()
                            .applyConnectionString(new ConnectionString(clientConfig.getUri()))
                            .build());
        } else {
            return createMongoClient(clientName, clientConfig, allOptions.options.get());
        }
    }

    protected MongoDatabase doCreateDatabase(MongoClient client, String dbName) {
        return client.getDatabase(dbName);
    }

    protected void doClose(MongoClient client) {
        client.close();
    }

    private MongoClient createMongoClient(
            String clientName,
            MongoDbConfig.ClientConfig clientConfig,
            MongoClientSettings.Builder mongoClientSettingsBuilder) {
        List<ServerAddress> serverAddresses = buildServerAddresses(clientName, clientConfig.getHosts());

        if (serverAddresses.isEmpty()) {
            throw SeedException.createNew(MongoDbErrorCode.MISSING_HOSTS_CONFIGURATION)
                    .put("clientName", clientName);
        }

        ConnectionString connectionString = buildConnectionString(clientName, serverAddresses,
                clientConfig.getCredentials());

        return MongoClients.create(mongoClientSettingsBuilder.applyConnectionString(connectionString).build());

    }

    public void registerClient(
            String clientName, MongoDbConfig.ClientConfig clientConfig, Coffig coffig) {
        LOGGER.info("Creating MongoDB client {}", clientName);
        mongoClients.put(clientName, doCreateClient(clientName, clientConfig, coffig));
    }

    public void registerDatabase(String clientName, String dbName, String alias) {
        MongoClient mongoClient = mongoClients.get(clientName);
        Preconditions.checkNotNull(mongoClient, "Mongo client " + clientName + " is not registered");
        mongoDatabases.put(alias, doCreateDatabase(mongoClient, dbName));
    }

    public void shutdown() {
        try {
            for (Map.Entry<String, MongoClient> mongoClientEntry : mongoClients.entrySet()) {
                LOGGER.info("Closing MongoDB client {}", mongoClientEntry.getKey());
                try {
                    doClose(mongoClientEntry.getValue());
                } catch (Exception e) {
                    LOGGER.error(
                            String.format(
                                    "Unable to properly close MongoDB client %s", mongoClientEntry.getKey()),
                            e);
                }
            }
        } finally {
            mongoDatabases.clear();
            mongoClients.clear();
        }
    }

    public Module getModule() {
        return new MongoDbModule<>(
                com.mongodb.client.MongoClient.class, MongoDatabase.class, mongoClients, mongoDatabases);
    }

    ConnectionString buildConnectionString(String clientName, List<ServerAddress> serverAddresses,
            String mongoCredential) {

        StringBuilder builder = new StringBuilder();
        builder.append("mongodb://");

        if (mongoCredential != null) {
            builder.append(mongoCredential);
            builder.append("@");
        }
        if (serverAddresses.size() == 1) {
            builder.append(serverAddresses.get(0));
        } else {
            builder.append(serverAddresses.stream()
                    .map(x -> x.toString())
                    .collect(Collectors.joining(",")));
        }
        LOGGER.info("Connection string", builder);
        return new ConnectionString(builder.toString());
    }

    List<ServerAddress> buildServerAddresses(String clientName, List<String> addresses) {
        List<ServerAddress> serverAddresses = new ArrayList<>();

        if (addresses != null) {
            for (String address : addresses) {
                String[] split = address.split(":", 2);
                if (split.length == 1) {
                    serverAddresses.add(new ServerAddress(split[0]));
                } else if (split.length == 2) {
                    serverAddresses.add(new ServerAddress(split[0], Integer.parseInt(split[1])));
                } else {
                    throw SeedException.createNew(MongoDbErrorCode.INVALID_SERVER_ADDRESS)
                            .put("clientName", clientName)
                            .put("address", address);
                }
            }
        }

        return serverAddresses;
    }

    private static class AllOptions {
        private BuilderSupplier<MongoClientSettings.Builder> options = BuilderSupplier
                .of(MongoClientSettings.builder());
    }
}
