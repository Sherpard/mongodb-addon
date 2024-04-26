/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */

package org.seedstack.mongodb.morphia.fixtures.product;

import org.seedstack.business.domain.BaseEntity;
import org.seedstack.business.domain.Identity;
import org.seedstack.business.util.SequenceGenerator;
import org.seedstack.business.util.inmemory.InMemory;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

@Entity
public class Picture extends BaseEntity<Long> {

    @Id
    @Identity(generator = SequenceGenerator.class)
    @InMemory
    private Long id; 
    private PictureURL url;
    private Long productId;

    public Picture(String url, Long productId) {
        super();
        this.url = new PictureURL(url);
        this.productId = productId;
    }

    public Picture() {

    }

    @Override
    public Long getId() {
        return id;
    }

    public PictureURL getUrl() {
        return url;
    }

    public void setUrl(PictureURL url) {
        this.url = url;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
