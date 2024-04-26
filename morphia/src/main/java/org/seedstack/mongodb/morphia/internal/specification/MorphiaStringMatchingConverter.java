/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.internal.specification;

import org.seedstack.business.specification.StringMatchingSpecification;


class MorphiaStringMatchingConverter extends MorphiaStringConverter<StringMatchingSpecification> {
    @Override
    String buildRegexMatchingPart(String value) {
        return value
                .replace(StringMatchingSpecification.SINGLE_CHARACTER_WILDCARD, ".")
                .replace(StringMatchingSpecification.MULTI_CHARACTER_WILDCARD, ".*");
    }

    @Override
    boolean isRegex() {
        return true;
    }
}
