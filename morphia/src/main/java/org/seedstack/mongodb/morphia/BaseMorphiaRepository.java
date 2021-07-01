/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mongodb.morphia;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Spliterators.spliteratorUnknownSize;

import com.mongodb.DBCollection;
import dev.morphia.Datastore;
import dev.morphia.query.CountOptions;
import dev.morphia.query.CriteriaContainer;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.internal.MorphiaCursor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

/**
 * This class can serve as a base class for Morphia repositories. It provides methods for common CRUD operations as
 * well as access to the data store through the {@link #getDatastore()} ()} protected method.
 *
 * @param <A>  Aggregate root class.
 * @param <ID> Identifier class.
 */
public abstract class BaseMorphiaRepository<A extends AggregateRoot<ID>, ID> extends BaseRepository<A, ID> {
    public static final String ID_KEY = "_id";
    private Datastore datastore;
    private SpecificationTranslator<MorphiaTranslationContext, CriteriaContainer> specificationTranslator;

    public BaseMorphiaRepository() {

    }

    public BaseMorphiaRepository(Class<A> aggregateRootClass, Class<ID> kClass) {
        super(aggregateRootClass, kClass);
    }

    @Inject
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Called by Guice")
    private void init(DatastoreFactory datastoreFactory,
            SpecificationTranslator<MorphiaTranslationContext, CriteriaContainer> specificationTranslator) {
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
        final MorphiaCursor<A> cursor = buildQuery(specification, options).find(buildFindOptions(options));
        return StreamSupport.stream(spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
                .onClose(cursor::close);
    }

    @Override
    public Optional<A> get(ID id) {
        return Optional.ofNullable(datastore.find(getAggregateRootClass()).filter(ID_KEY, id).first());
    }

    @Override
    public boolean contains(Specification<A> specification) {
        return buildQuery(specification).count(new CountOptions().limit(1)) > 0;
    }

    @Override
    public boolean contains(ID id) {
        return datastore.find(getAggregateRootClass()).filter(ID_KEY, id).count(new CountOptions().limit(1)) > 0;
    }

    @Override
    public long count(Specification<A> specification) {
        return buildQuery(specification).count();
    }

    @Override
    public long size() {
        return datastore.createQuery(getAggregateRootClass()).count();
    }

    @Override
    public long remove(Specification<A> specification) throws AggregateNotFoundException {
        return datastore.delete(buildQuery(specification)).getN();
    }

    @Override
    public void remove(ID id) throws AggregateNotFoundException {
        checkExactlyOneAggregateRemoved(
                datastore.delete(datastore.find(getAggregateRootClass()).filter(ID_KEY, id)).getN(),
                id);
    }

    private void checkExactlyOneAggregateRemoved(int n, ID id) {
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
        DBCollection collection = datastore.getCollection(getAggregateRootClass());
        collection.drop();
        collection.dropIndexes();
    }

    private Query<A> buildQuery(Specification<A> specification, Option... options) {
        Query<A> query = datastore.createQuery(getAggregateRootClass());
        specificationTranslator.translate(
                specification,
                new MorphiaTranslationContext<>(query)
        );
        for (Option option : options) {
            if (option instanceof SortOption) {
                applySort(query, ((SortOption) option));
            }
        }
        return query;
    }

    private FindOptions buildFindOptions(Option... options) {
        FindOptions findOptions = new FindOptions();
        for (Option option : options) {
            if (option instanceof OffsetOption) {
                applyOffset(findOptions, ((OffsetOption) option));
            } else if (option instanceof LimitOption) {
                applyLimit(findOptions, ((LimitOption) option));
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

    private void applySort(Query<?> query, SortOption sortOption) {
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
        query.order(sorts.toArray(new Sort[0]));
    }
}