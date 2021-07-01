/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.internal;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.ServerSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import org.bson.codecs.configuration.CodecRegistry;
import org.seedstack.coffig.BuilderSupplier;
import org.seedstack.coffig.Coffig;
import org.seedstack.mongodb.MongoDbConfig;
import org.seedstack.shed.reflect.Classes;

import java.util.List;
import java.util.Optional;

class AsyncMongoDbManager extends AbstractMongoDbManager<MongoClient, MongoDatabase> {
    @Override
    protected MongoClient doCreateClient(String clientName, MongoDbConfig.ClientConfig clientConfig, Coffig coffig) {
        if (clientConfig.isConfiguredByUri()) {
            return MongoClients.create(clientConfig.getUri());
        } else {
            return MongoClients.create(buildMongoClientSettings(clientConfig, clientName, coffig));
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

    private MongoClientSettings buildMongoClientSettings(MongoDbConfig.ClientConfig clientConfig, String clientName, Coffig coffig) {
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder();
        AllSettings allSettings = coffig.get(AllSettings.class, String.format("mongoDb.clients.%s.settings", clientName));

        // Apply hosts
        List<String> hosts = clientConfig.getHosts();
        if (hosts.size() > 0) {
            allSettings.cluster.get().hosts(buildServerAddresses(clientName, hosts));
        }

        // Apply credentials
        settingsBuilder.credentialList(buildMongoCredentials(clientName, clientConfig.getCredentials()));

        // Apply global settings
        Optional.ofNullable(allSettings.readPreference).ifPresent(settingsBuilder::readPreference);
        Optional.ofNullable(allSettings.writeConcern).ifPresent(settingsBuilder::writeConcern);
        Optional.ofNullable(allSettings.codecRegistry).map(Classes::instantiateDefault).ifPresent(settingsBuilder::codecRegistry);

        // Apply sub-settings
        settingsBuilder.clusterSettings(allSettings.cluster.get().build());
        settingsBuilder.socketSettings(allSettings.socket.get().build());
        settingsBuilder.heartbeatSocketSettings(allSettings.heartbeatSocket.get().build());
        settingsBuilder.connectionPoolSettings(allSettings.connectionPool.get().build());
        settingsBuilder.serverSettings(allSettings.server.get().build());
        settingsBuilder.sslSettings(allSettings.ssl.get().build());

        return settingsBuilder.build();
    }

    private static class AllSettings {
        private ReadPreference readPreference;
        private WriteConcern writeConcern;
        private Class<? extends CodecRegistry> codecRegistry;
        private BuilderSupplier<ClusterSettings.Builder> cluster = BuilderSupplier.of(ClusterSettings.builder());
        private BuilderSupplier<SocketSettings.Builder> socket = BuilderSupplier.of(SocketSettings.builder());
        private BuilderSupplier<SocketSettings.Builder> heartbeatSocket = BuilderSupplier.of(SocketSettings.builder());
        private BuilderSupplier<ConnectionPoolSettings.Builder> connectionPool = BuilderSupplier.of(ConnectionPoolSettings.builder());
        private BuilderSupplier<ServerSettings.Builder> server = BuilderSupplier.of(ServerSettings.builder());
        private BuilderSupplier<SslSettings.Builder> ssl = BuilderSupplier.of(SslSettings.builder());
    }
}
