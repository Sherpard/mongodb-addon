---
title: "Morphia"
repo: "https://github.com/seedstack/mongodb-addon"
weight: -1
tags:
    - persistence
zones:
    - Addons
menu:
    AddonMongoDB:
        weight: 20
---

[Morphia](https://github.com/mongodb/morphia) is an Object/Document mapper. It provides annotation-based POJO mapping, 
and fluent query/update APIs. SeedStack MongoDb add-on provides a module for Morphia.<!--more-->
 
# Dependency 

{{< dependency g="org.seedstack.addons.mongodb" a="mongodb-morphia" >}}

# Configuration

{{% callout info %}}
Morphia only works with synchronous MongoDB clients.
{{% /callout %}}

To configure Morphia, just specify the synchronous client and which one of its database to use for a mapping a specific class.
This is done with SeedStack [class configuration]({{< ref "docs/seed/configuration.md#class-configuration" >}}): 

```yaml
mongoDb:
  clients:
    clients1:
      uri: ...
      databases: db1
classes:
  org:
    myorg:
      myapp:
        domain:
          model:
            mongoDbClient: client1
            mongoDbDatabase: db1
```

The configuration above will use the MongoDb client `client1` and its database `db1` for mapping classes in the 
`org.myorg.myapp.domain.model` package and its subpackages.

# Basic usage

## Mapping

Mapping is done with annotations:

```java
@Entity
public class User {
	@Id
	private long id;
	private String firstName;
	private String lastName;
    @Embedded    
    private Address address;
    
	// ...
}

@Embedded
public class Address {
	private String country;
	private String zipCode;
	private String city;
	private String street;
	private Integer number;
	
	// ...
}
```

## Datastore

A Morphia `Datastore` can be injected by qualifying the injection with the {{< java "org.seedstack.mongodb.morphia.MorphiaDatastore" "@" >}}
annotation with the client and the database name as parameters::

```java
public class SomeClass {
	@Inject
	@MorphiaDatastore(clientName = "client1",dbName="db1")
	private Datastore datastore; 
	
	@Test
	public void someMethod(){
		User user = new User();
		Key<User> keyUser = datastore.save(user);
	}
}
```

# Business framework usage

The Morphia add-on also provides repositories for the [Business Framework]({{< ref "docs/business/index.md" >}}).

## Base for custom repositories

A base repository is provided for extension:
 
```java
public interface SomeAggregateRepository extends Repository<SomeAggregate, String> {
    // ...
}

public class SomeAggregateMongoRepository extends BaseMorphiaRepository<SomeAggregate, String> 
                                          implements SomeAggregateRepository {
    //...
}
```

This can then be injected as usual:

```java
public class SomeClass {
    @Inject
    private SomeAggregateRepository someAggregateRepository;
}
```

{{% callout tips %}}
You can access the datastore of a Morphia repository by calling its `getDatastore()` method.
{{% /callout %}}

## Default repositories 

If you don't need repository custom methods, default repositories for all aggregates are automatically registered and 
can be injected using {{< java "org.seedstack.business.domain.Repository" >}} interface with the 
{{< java "org.seedstack.mongodb.morphia.Morphia" "@" >}} qualifier:

```java
public class SomeClass {
	@Inject
	@Morphia
	private Repository<SomeAggregate, String> someAggregateRepository;
}
```
