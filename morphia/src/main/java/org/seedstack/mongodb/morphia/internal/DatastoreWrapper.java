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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class DatastoreWrapper extends DatastoreImpl {

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "Manually verified that mapper is initialized")
    public DatastoreWrapper(Datastore datastore, SeedEntityListener seedListener) {
        super((DatastoreImpl) datastore);
        // TODO: Find a way to wrap mapper instead of adding an interceptor
        getMapper().addInterceptor(seedListener);

    }

}
