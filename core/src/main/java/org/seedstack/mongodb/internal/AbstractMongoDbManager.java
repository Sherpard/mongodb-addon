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
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.seedstack.coffig.Coffig;
import org.seedstack.mongodb.MongoDbConfig;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractMongoDbManager<C, D> implements MongoDbManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMongoDbManager.class);

    private final Map<String, C> mongoClients = new HashMap<>();
    private final Map<String, D> mongoDatabases = new HashMap<>();

    @Override
    public void registerClient(String clientName, MongoDbConfig.ClientConfig clientConfig, Coffig coffig) {
        LOGGER.info("Creating MongoDB client {}", clientName);
        mongoClients.put(clientName, doCreateClient(clientName, clientConfig, coffig));
    }

    @Override
    public void registerDatabase(String clientName, String dbName, String alias) {
        C mongoClient = mongoClients.get(clientName);
        Preconditions.checkNotNull(mongoClient, "Mongo client " + clientName + " is not registered");
        mongoDatabases.put(alias, doCreateDatabase(mongoClient, dbName));
    }

    @Override
    public void shutdown() {
        try {
            for (Map.Entry<String, C> mongoClientEntry : mongoClients.entrySet()) {
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

    @Override
    @SuppressWarnings("unchecked")
    public Module getModule() {
        Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        return new MongoDbModule<>((Class<C>) actualTypeArguments[0], (Class<D>) actualTypeArguments[1], mongoClients, mongoDatabases);
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

    List<MongoCredential> buildMongoCredentials(String clientName, List<String> credentials) {
        List<MongoCredential> mongoCredentials = new ArrayList<>();

        if (credentials != null) {
            for (String credential : credentials) {
                String[] elements = credential.split(":", 3);
                if (elements.length == 3) {
                    String[] sourceElements = elements[0].split("/", 2);
                    if (sourceElements.length == 2) {
                        mongoCredentials.add(buildMongoCredential(clientName, elements[1], elements[2], sourceElements[1], sourceElements[0]));
                    } else if (sourceElements.length == 1) {
                        mongoCredentials.add(buildMongoCredential(clientName, elements[1], elements[2], sourceElements[0], null));
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

        return mongoCredentials;
    }

    MongoCredential buildMongoCredential(String clientName, String user, String password, String source, String mechanism) {
        if (mechanism != null) {
            AuthenticationMechanism authenticationMechanism = AuthenticationMechanism.fromMechanismName(mechanism);
            switch (authenticationMechanism) {
                case PLAIN:
                    return MongoCredential.createPlainCredential(user, source, password.toCharArray());
                case MONGODB_CR:
                    return MongoCredential.createMongoCRCredential(user, source, password.toCharArray());
                case SCRAM_SHA_1:
                    return MongoCredential.createScramSha1Credential(user, source, password.toCharArray());
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

    protected abstract C doCreateClient(String clientName, MongoDbConfig.ClientConfig clientConfiguration, Coffig coffig);

    protected abstract D doCreateDatabase(C client, String dbName);

    protected abstract void doClose(C client);
}
