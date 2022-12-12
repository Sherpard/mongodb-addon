/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.internal;

import com.google.common.base.Preconditions;
import com.google.inject.Module;
import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.seedstack.coffig.BuilderSupplier;
import org.seedstack.coffig.Coffig;
import org.seedstack.mongodb.MongoDbConfig;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MongoDbManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbManager.class);
    private final Map<String, MongoClient> mongoClients = new HashMap<>();
    private final Map<String, MongoDatabase> mongoDatabases = new HashMap<>();

    protected MongoClient doCreateClient(String clientName, MongoDbConfig.ClientConfig clientConfig, Coffig coffig) {
        AllOptions allOptions = coffig.get(AllOptions.class, String.format("mongoDb.clients.%s", clientName));
        if (clientConfig.isConfiguredByUri()) {
            return new MongoClient(new MongoClientURI(clientConfig.getUri(), allOptions.options.get()));
        } else {
            return createMongoClient(clientName, clientConfig, allOptions.options.get().build());
        }
    }

    protected MongoDatabase doCreateDatabase(MongoClient client, String dbName) {
        return client.getDatabase(dbName);
    }

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

        MongoCredential mongoCredential = buildMongoCredential(clientName, clientConfig.getCredentials());
        if (mongoCredential == null) {
            if (serverAddresses.size() == 1) {
                return new MongoClient(serverAddresses.get(0), mongoClientOptions);
            } else {
                return new MongoClient(serverAddresses, mongoClientOptions);
            }
        } else {
            if (serverAddresses.size() == 1) {
                return new MongoClient(serverAddresses.get(0), mongoCredential, mongoClientOptions);
            } else {
                return new MongoClient(serverAddresses, mongoCredential, mongoClientOptions);
            }
        }
    }

    public void registerClient(String clientName, MongoDbConfig.ClientConfig clientConfig, Coffig coffig) {
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
                    LOGGER.error(String.format("Unable to properly close MongoDB client %s", mongoClientEntry.getKey()), e);
                }
            }
        } finally {
            mongoDatabases.clear();
            mongoClients.clear();
        }
    }

    public Module getModule() {
        return new MongoDbModule<>(com.mongodb.MongoClient.class, MongoDatabase.class, mongoClients, mongoDatabases);
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

    MongoCredential buildMongoCredential(String clientName, String credential) {
        if (credential == null || credential.isEmpty()) {
            return null;
        } else {
            String[] elements = credential.split(":", 3);
            if (elements.length == 3) {
                String[] sourceElements = elements[0].split("/", 2);
                if (sourceElements.length == 2) {
                    return buildMongoCredential(clientName, elements[1], elements[2], sourceElements[1], sourceElements[0]);
                } else if (sourceElements.length == 1) {
                    return buildMongoCredential(clientName, elements[1], elements[2], sourceElements[0], null);
                } else {
                    throw SeedException.createNew(MongoDbErrorCode.INVALID_CREDENTIAL_SYNTAX)
                            .put("credential", credential)
                            .put("clientName", clientName);
                }
            } else {
                throw SeedException.createNew(MongoDbErrorCode.INVALID_CREDENTIAL_SYNTAX)
                        .put("credential", credential)
                        .put("clientName", clientName);
            }
        }
    }

    MongoCredential buildMongoCredential(String clientName, String user, String password, String source, String mechanism) {
        if (mechanism != null) {
            AuthenticationMechanism authenticationMechanism = AuthenticationMechanism.fromMechanismName(mechanism);
            switch (authenticationMechanism) {
                case PLAIN:
                    return MongoCredential.createPlainCredential(user, source, password.toCharArray());
                case SCRAM_SHA_1:
                    return MongoCredential.createScramSha1Credential(user, source, password.toCharArray());
                case SCRAM_SHA_256:
                    return MongoCredential.createScramSha256Credential(user, source, password.toCharArray());
                case MONGODB_AWS:
                    return MongoCredential.createAwsCredential(user, password.toCharArray());
                case MONGODB_X509:
                    return MongoCredential.createMongoX509Credential(user);
                case GSSAPI:
                    return MongoCredential.createGSSAPICredential(user);
                default:
                    throw SeedException.createNew(MongoDbErrorCode.UNSUPPORTED_AUTHENTICATION_MECHANISM)
                            .put("clientName", clientName)
                            .put("mechanism", authenticationMechanism.getMechanismName());
            }
        } else {
            return MongoCredential.createCredential(
                    user,
                    source,
                    password.toCharArray()
            );
        }
    }

    private static class AllOptions {
        private BuilderSupplier<MongoClientOptions.Builder> options = BuilderSupplier.of(MongoClientOptions.builder());
    }
}
