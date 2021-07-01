/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import javax.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.seedstack.business.domain.Repository;
import org.seedstack.mongodb.morphia.fixtures.dummyobject.Dummy1;
import org.seedstack.mongodb.morphia.fixtures.dummyobject.Dummy2;
import org.seedstack.mongodb.morphia.fixtures.dummyobject.Dummy3;
import org.seedstack.mongodb.morphia.fixtures.dummyobject.Dummy4;
import org.seedstack.mongodb.morphia.fixtures.dummyobject.Dummy5;
import org.seedstack.mongodb.morphia.fixtures.dummyobject.Dummy6;
import org.seedstack.mongodb.morphia.fixtures.user.Address;
import org.seedstack.mongodb.morphia.fixtures.user.User;
import org.seedstack.mongodb.morphia.internal.MorphiaErrorCode;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class MorphiaIT {
    @Inject
    @MorphiaDatastore(clientName = "client1", dbName = "db")
    private Datastore datastore;
    @Inject
    private Injector injector;

    @Test
    public void datastoreAccess() {
        User user = new User(1L,
                "Gerard",
                "menvuça",
                new Address("France", "78300", "Poissy", "avenue de l'europe", 1));
        Key<User> keyUser = datastore.save(user);
        Assertions.assertThat(keyUser).isNotNull();
    }

    @Test(expected = ConstraintViolationException.class)
    public void validationIsWorking() {
        User user = new User(1L, null, "menvuça", new Address("France", "78300", "Poissy", "avenue de l'europe", 1));
        datastore.save(user);
        fail("should not have saved");
    }

    @Test
    public void repositoryInjectionTestNoClientForAggregate() {
        try {
            injector.getInstance(getMorphiaRepositoryOf(Dummy1.class));
        } catch (ProvisionException e) {
            assertThat(e.getCause().getMessage())
                    .isEqualTo(SeedException.createNew(MorphiaErrorCode.CLIENT_NAME_NOT_CONFIGURED).getMessage());
        }
    }

    private com.google.inject.Key<?> getMorphiaRepositoryOf(Class entity) {
        return com.google.inject.Key.get(TypeLiteral.get(Types.newParameterizedType(Repository.class,
                entity,
                Long.class)), Morphia.class);
    }

    @Test
    public void repositoryInjectionTestNoDbNameForAggregate() {
        try {
            injector.getInstance(getMorphiaRepositoryOf(Dummy2.class));
        } catch (ProvisionException e) {
            assertThat(e.getCause().getMessage())
                    .isEqualTo(SeedException.createNew(MorphiaErrorCode.UNKNOWN_DATABASE).getMessage());
        }
    }

    @Test
    public void repositoryInjectionTestNoMongoDbClient() {
        try {
            injector.getInstance(getMorphiaRepositoryOf(Dummy3.class));
        } catch (ProvisionException e) {
            assertThat(e.getCause().getMessage())
                    .isEqualTo(SeedException.createNew(MorphiaErrorCode.UNKNOWN_CLIENT).getMessage());
        }
    }

    @Test
    public void repositoryInjectionTestNoMongoDbDatabase() {
        try {
            injector.getInstance(getMorphiaRepositoryOf(Dummy4.class));
        } catch (ProvisionException e) {
            assertThat(e.getCause().getMessage())
                    .isEqualTo(SeedException.createNew(MorphiaErrorCode.UNKNOWN_DATABASE).getMessage());
        }
    }

    @Test
    public void repositoryInjectionTestNoMongodbForAggregate() {
        try {
            injector.getInstance(getMorphiaRepositoryOf(Dummy5.class));
        } catch (ProvisionException e) {
            assertThat(e.getCause().getMessage())
                    .isEqualTo(SeedException.createNew(MorphiaErrorCode.PERSISTED_CLASS_NOT_CONFIGURED).getMessage());
        }
    }

    @Test
    public void repositoryInjectionAsyncClient() {
        try {
            injector.getInstance(getMorphiaRepositoryOf(Dummy6.class));
        } catch (ProvisionException e) {
            assertThat(e.getCause().getMessage())
                    .isEqualTo(SeedException.createNew(MorphiaErrorCode.ASYNC_CLIENT_NOT_SUPPORTED).getMessage());
        }
    }
}
