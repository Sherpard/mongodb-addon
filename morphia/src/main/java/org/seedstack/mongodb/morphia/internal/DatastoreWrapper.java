/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import dev.morphia.Datastore;
import dev.morphia.DatastoreImpl;

class DatastoreWrapper extends DatastoreImpl {

    public DatastoreWrapper(Datastore datastore, SeedEntityListener seedListener) {
        super((DatastoreImpl) datastore);
        // TODO: Find a way to wrap mapper instead of adding an interceptor
        getMapper().addInterceptor(seedListener);

    }

}
