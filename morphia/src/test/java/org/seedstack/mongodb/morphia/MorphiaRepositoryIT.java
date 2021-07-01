/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.google.inject.Inject;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.business.domain.AggregateNotFoundException;
import org.seedstack.business.domain.Repository;
import org.seedstack.mongodb.morphia.fixtures.user.Address;
import org.seedstack.mongodb.morphia.fixtures.user.User;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class MorphiaRepositoryIT {
    @Inject
    @Morphia
    private Repository<User, Long> userRepository;

    @Before
    public void setUp() throws Exception {
        userRepository.clear();
    }

    @Test
    public void addAndGet() throws Exception {
        User user1 = createUser(1L, "someFirstName1", "someLastName1");
        User user2 = createUser(2L, "someFirstName2", "someLastName2");
        userRepository.add(user1);
        userRepository.add(user2);
        Optional<User> loaded = userRepository.get(2L);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getName()).isEqualTo("someFirstName2");
        assertThat(loaded.get().getLastname()).isEqualTo("someLastName2");
        assertThat(userRepository.get(3L)).isNotPresent();
    }

    @Test
    public void addRemoveAndGet() throws Exception {
        User user1 = createUser(1L, "someFirstName", "someLastName");
        userRepository.add(user1);
        assertThat(userRepository.get(1L)).isPresent();
        userRepository.remove(user1);
        assertThat(userRepository.get(1L)).isNotPresent();
    }

    @Test
    public void update() {
        userRepository.add(createUser(200L, "Robert", "SMITH"));
        userRepository.update(createUser(200L, "Jane", "SMITH"));
        assertThat(userRepository.get(200L).get().getName()).isEqualTo("Jane");
    }

    @Test
    public void addOrUpdate() {
        userRepository.addOrUpdate(createUser(200L, "Robert", "SMITH"));
        assertThat(userRepository.contains(200L)).isTrue();
        assertThat(userRepository.get(200L).get().getName()).isEqualTo("Robert");
        userRepository.addOrUpdate(createUser(200L, "Jane", "SMITH"));
        assertThat(userRepository.contains(200L)).isTrue();
        assertThat(userRepository.get(200L).get().getName()).isEqualTo("Jane");
    }

    @Test(expected = AggregateNotFoundException.class)
    public void updateNonExistent() {
        userRepository.update(createUser(100L, "Robert", "SMITH"));
        fail("should not have updated");
    }

    @Test
    public void clear() {
        userRepository.add(createUser(400L, "Robert", "SMITH"));
        userRepository.add(createUser(401L, "Jayne", "SMITH"));
        assertThat(userRepository.get(400L)).isPresent();
        assertThat(userRepository.get(401L)).isPresent();
        userRepository.clear();
        assertThat(userRepository.get(400L)).isNotPresent();
        assertThat(userRepository.get(401L)).isNotPresent();
    }

    @Test
    public void contains() {
        userRepository.add(createUser(300L, "Robert", "SMITH"));
        assertThat(userRepository.contains(300L)).isTrue();
        assertThat(userRepository.contains(3010L)).isFalse();
    }

    @Test
    public void size() {
        userRepository.add(createUser(300L, "Robert", "SMITH"));
        userRepository.add(createUser(301L, "Roberta", "SMITH"));
        assertThat(userRepository.size()).isEqualTo(2);
    }

    private User createUser(long id, String firstname, String lastName) {
        return new User(id, firstname, lastName, new Address("France", "75001", "Paris", "Champ Elysee avenue", 1));
    }
}
