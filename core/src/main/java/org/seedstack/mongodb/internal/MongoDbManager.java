/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.internal;

import com.google.inject.Module;
import org.seedstack.coffig.Coffig;
import org.seedstack.mongodb.MongoDbConfig;

interface MongoDbManager {
    void registerClient(String clientName, MongoDbConfig.ClientConfig clientConfiguration, Coffig coffig);

    void registerDatabase(String clientName, String dbName, String alias);

    Module getModule();

    void shutdown();
}
