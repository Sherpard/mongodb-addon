/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.internal;

import org.seedstack.shed.exception.ErrorCode;

enum MongoDbErrorCode implements ErrorCode {
    DUPLICATE_DATABASE_NAME,
    INVALID_CREDENTIAL_SYNTAX,
    INVALID_SERVER_ADDRESS,
    MISSING_HOSTS_CONFIGURATION,
    UNSUPPORTED_AUTHENTICATION_MECHANISM
}
