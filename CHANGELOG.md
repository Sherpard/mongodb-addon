# Version 4.0.0 (2022-12-12)

* [brk] Remove support for deprecated async driver
* [brk] Require at least MongoDb driver support to 4.1
* [chg] Upgrade Morphia to 1.6.1 (which makes it compatible with AWS DocumentDB).

# Version 3.1.3 (2021-07-31)

* [fix] Avoid calling `ensureIndexes()` and `ensureCaps()` for each datastore instantiation which adds a considerable
  overhead in some circumstances. It is now done at application startup and can be controlled with
  `mongoDb.morphia.ensureCapsAtStartup` and `mongoDb.morphia.ensureIndexesAtStartup` config options.

# Version 3.1.2 (2020-08-05)

* [chg] Updated for seed 3.10+

# Version 3.1.1 (2020-02-05)

* [fix] Fix `BaseMorphiaRepository.get(id)` always returning the first document from the collection (ignoring given id).

# Version 3.1.0 (2020-01-31)

* [new] Morphia repository now has the ability to stream results from the database as consumed (using a cursor instead
  of a list behind the scenes).
* [chg] Update the Morphia module to work with business framework 4.3.0+.
* [chg] Updated Morphia to 1.5.8 (its package has changed to `dev.morphia`, update project code accordingly if
  necessary).

# Version 3.0.2 (2019-01-10)

* [fix] Fix issue #11: an exception occurred at startup because the way of accessing the ValidationFactory changed.

# Version 3.0.1 (2019-01-10)

* [fix] During translation of composite specifications (or, and), set the field for each member (avoid "No field has
  been set" exception).

# Version 3.0.0 (2017-11-30)

* [new] Implements automatic translation of business specifications to Morphia queries.

# Version 2.0.0 (2017-01-13)

* [brk] Update to new configuration system.

# Version 1.1.0 (2016-04-26)

* [new] Automatically build indexes
* [new] Add Bean Validation support to Morphia (at pre-persist) when SeedStack validation add-on is present in the
  classpath.
* [new] Add `exists()` and `count()` in `BaseMorphiaRepository` according to change in business framework.
* [chg] Update for SeedStack 16.4.
* [brk] Remove `do*()` methods in `BaseMorphiaRepository` according to change in business framework.
* [fix] Correctly take MongoDb database aliases into account when injecting Morphia data stores.

# Version 1.0.1 (2016-02-09)

* [fix] Flawed release process made this add-on unusable by clients.

# Version 1.0.0 (2015-07-30)

* [new] Initial Open-Source release.
