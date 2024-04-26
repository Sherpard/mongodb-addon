/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.seedstack.business.domain.AggregateExistsException;
import org.seedstack.business.domain.AggregateNotFoundException;
import org.seedstack.business.domain.AggregateRoot;
import org.seedstack.business.domain.BaseRepository;
import org.seedstack.business.domain.LimitOption;
import org.seedstack.business.domain.OffsetOption;
import org.seedstack.business.domain.SortOption;
import org.seedstack.business.specification.Specification;
import org.seedstack.business.spi.SpecificationTranslator;
import org.seedstack.mongodb.morphia.internal.DatastoreFactory;
import org.seedstack.mongodb.morphia.internal.specification.MorphiaTranslationContext;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

import com.mongodb.client.MongoCollection;

import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.query.CountOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.MorphiaCursor;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class can serve as a base class for Morphia repositories. It provides methods for common
 * CRUD operations as well as access to the data store through the {@link #getDatastore()} ()}
 * protected method.
 *
 * @param <A>
 *            Aggregate root class.
 * @param <ID>
 *            Identifier class.
 */
public abstract class BaseMorphiaRepository<A extends AggregateRoot<ID>, ID> extends BaseRepository<A, ID> {
    public static final String ID_KEY = "_id";
    private Datastore datastore;
    private SpecificationTranslator<MorphiaTranslationContext, Filter> specificationTranslator;

    @Logging
    private Logger logger;

    public BaseMorphiaRepository() {

    }

    public BaseMorphiaRepository(Class<A> aggregateRootClass, Class<ID> kClass) {
        super(aggregateRootClass, kClass);
    }

    @Inject
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Called by Guice")
    private void init(DatastoreFactory datastoreFactory,
            SpecificationTranslator<MorphiaTranslationContext, Filter> specificationTranslator) {
        this.datastore = datastoreFactory.createDatastore(getAggregateRootClass());
        this.specificationTranslator = specificationTranslator;
    }

    /**
     * Provides access to the Morphia data store for implementing custom data access methods.
     *
     * @return the Morphia data store.
     */
    protected Datastore getDatastore() {
        return datastore;
    }

    @Override
    public void add(A aggregate) throws AggregateExistsException {
        datastore.save(aggregate);
    }

    @Override
    public Stream<A> get(Specification<A> specification, Option... options) {
        MorphiaCursor<A> cursor = buildQuery(specification).iterator(buildFindOptions(options));
        return StreamSupport.stream(spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
                .onClose(cursor::close);
    }

    @Override
    public Optional<A> get(ID id) {
        return Optional.ofNullable(datastore.find(getAggregateRootClass()).filter(Filters.eq(ID_KEY, id)).first());
    }

    @Override
    public boolean contains(Specification<A> specification) {
        return buildQuery(specification).count(new CountOptions().limit(1)) > 0;
    }

    @Override
    public boolean contains(ID id) {
        return this.get(id).isPresent();
    }

    @Override
    public long count(Specification<A> specification) {
        return buildQuery(specification).count();
    }

    @Override
    public long size() {
        return datastore.find(getAggregateRootClass()).count();
    }

    @Override
    public long remove(Specification<A> specification) throws AggregateNotFoundException {
        return buildQuery(specification).delete(new DeleteOptions().multi(true)).getDeletedCount();
    }

    @Override
    public void remove(ID id) throws AggregateNotFoundException {
        long deletedCount = datastore.find(getAggregateRootClass()).filter(Filters.eq(ID_KEY, id)).delete()
                .getDeletedCount();
        checkExactlyOneAggregateRemoved(deletedCount, id);
    }

    private void checkExactlyOneAggregateRemoved(long n, ID id) {
        if (n == 0) {
            throw new AggregateNotFoundException("Non-existent aggregate " + getAggregateRootClass()
                    .getSimpleName() + " identified with " + id + " cannot be removed");
        } else if (n > 1) {
            throw new IllegalStateException("More than one aggregate " + getAggregateRootClass()
                    .getSimpleName() + " identified with " + id + " have been removed");
        }
    }

    @Override
    public A update(A aggregate) throws AggregateNotFoundException {
        if (!contains(aggregate)) {
            throw new AggregateNotFoundException("Non-existent aggregate " + getAggregateRootClass()
                    .getSimpleName() + " identified with " + aggregate.getId() + " cannot be updated");
        }
        datastore.merge(aggregate);
        return aggregate;
    }

    @Override
    public A addOrUpdate(A aggregate) {
        datastore.save(aggregate);
        return aggregate;
    }

    @Override
    public void clear() {
        MongoCollection<A> collection = datastore.getCollection(getAggregateRootClass());
        collection.dropIndexes();
        collection.drop();
    }

    private Query<A> buildQuery(Specification<A> specification) {
        Query<A> query = datastore.find(getAggregateRootClass());
        Filter filter = specificationTranslator.translate(
                specification,
                new MorphiaTranslationContext<>(query));
        logger.info("Querying {} with filter {}",getAggregateRootClass(),filter);
        return query.filter(filter);
    }

    private FindOptions buildFindOptions(Option... options) {
        FindOptions findOptions = new FindOptions();
        for (Option option : options) {
            if (option instanceof OffsetOption) {
                applyOffset(findOptions, ((OffsetOption) option));
            } else if (option instanceof LimitOption) {
                applyLimit(findOptions, ((LimitOption) option));
            } else if (option instanceof SortOption) {
                applySort(findOptions, ((SortOption) option));
            }
        }
        return findOptions;
    }

    private void applyOffset(FindOptions findOptions, OffsetOption offsetOption) {
        long offset = offsetOption.getOffset();
        checkArgument(offset <= Integer.MAX_VALUE,
                "Morphia only supports offsetting results up to " + Integer.MAX_VALUE);
        findOptions.skip((int) offset);
    }

    private void applyLimit(FindOptions findOptions, LimitOption limitOption) {
        long limit = limitOption.getLimit();
        checkArgument(limit <= Integer.MAX_VALUE,
                "Morphia only supports limiting results up to " + Integer.MAX_VALUE);
        findOptions.limit((int) limit);
    }

    private void applySort(FindOptions findOptions, SortOption sortOption) {
        List<Sort> sorts = new ArrayList<>();
        for (SortOption.SortedAttribute sortedAttribute : sortOption.getSortedAttributes()) {
            switch (sortedAttribute.getDirection()) {
            case ASCENDING:
                sorts.add(Sort.ascending(sortedAttribute.getAttribute()));
                break;
            case DESCENDING:
                sorts.add(Sort.descending(sortedAttribute.getAttribute()));
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported sort direction " + sortedAttribute.getDirection());
            }
        }

        findOptions.sort(sorts.toArray(new Sort[0]));
    }
}
