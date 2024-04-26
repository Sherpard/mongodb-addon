/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Config("mongoDb")
public class MongoDbConfig {
    private Map<String, ClientConfig> clients = new HashMap<>();

    public Map<String, ClientConfig> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    public MongoDbConfig addClient(String name, ClientConfig config) {
        this.clients.put(name, config);
        return this;
    }

    public static class ClientConfig {
        @SingleValue
        private String uri;
        private List<String> hosts = new ArrayList<>();
        private String credentials;
        private Map<String, DatabaseConfig> databases = new HashMap<>();

        public boolean isConfiguredByUri() {
            return uri != null;
        }

        public String getUri() {
            return uri;
        }

        public ClientConfig setUri(String uri) {
            if (!hosts.isEmpty()) {
                throw new IllegalStateException("Cannot set MongoDb URI, the client is already configured through hosts");
            }
            this.uri = uri;
            return this;
        }

        public List<String> getHosts() {
            return Collections.unmodifiableList(hosts);
        }

        public ClientConfig addHost(String host) {
            if (uri != null) {
                throw new IllegalStateException("Cannot add MongoDb host, the client is already configured through an URI");
            }
            this.hosts.add(host);
            return this;
        }

        public void setCredentials(String credentials) {
            this.credentials = credentials;
        }

        public String getCredentials() {
            return credentials;
        }

        public Map<String, DatabaseConfig> getDatabases() {
            return Collections.unmodifiableMap(databases);
        }

        public ClientConfig addDatabase(String name, DatabaseConfig databaseConfig) {
            this.databases.put(name, databaseConfig);
            return this;
        }

        public static class DatabaseConfig {
            @SingleValue
            private String alias;

            public String getAlias() {
                return alias;
            }

            public DatabaseConfig setAlias(String alias) {
                this.alias = alias;
                return this;
            }
        }
    }
}
