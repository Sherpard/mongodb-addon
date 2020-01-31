/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia.fixtures.product;

import dev.morphia.annotations.Embedded;
import org.seedstack.business.domain.BaseValueObject;

@Embedded
public class PictureURL extends BaseValueObject {
    private String url;
    private boolean secured = false;

    public PictureURL() {
    }

    public PictureURL(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }
}
