/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal;

import org.seedstack.shed.exception.ErrorCode;

public enum MorphiaErrorCode implements ErrorCode {
    ASYNC_CLIENT_NOT_SUPPORTED,
    CLIENT_NAME_NOT_CONFIGURED,
    DATABASE_NOT_CONFIGURED,
    PERSISTED_CLASS_NOT_CONFIGURED,
    UNKNOWN_CLIENT,
    UNKNOWN_DATABASE
}
