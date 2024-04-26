/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia;

import org.seedstack.coffig.Config;

@Config("mongoDb.morphia")
public class MorphiaConfig {
    private boolean ensureCapsAtStartup = true;
    private boolean ensureIndexesAtStartup = true;

    public boolean isEnsureCapsAtStartup() {
        return ensureCapsAtStartup;
    }

    public MorphiaConfig setEnsureCapsAtStartup(boolean ensureCapsAtStartup) {
        this.ensureCapsAtStartup = ensureCapsAtStartup;
        return this;
    }

    public boolean isEnsureIndexesAtStartup() {
        return ensureIndexesAtStartup;
    }

    public MorphiaConfig setEnsureIndexesAtStartup(boolean ensureIndexesAtStartup) {
        this.ensureIndexesAtStartup = ensureIndexesAtStartup;
        return this;
    }
}
