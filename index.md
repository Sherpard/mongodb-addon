---
title: "MongoDB"
addon: "MongoDB"
repo: "https://github.com/seedstack/mongodb-addon"
author: Adrien LAUER
description: "Provides configuration and injection for sync and async MongoDB clients."
tags:
    - persistence
zones:
    - Addons
menu:
    MongoDB:
        parent: "contents"
        weight: 10
---

SeedStack MongoDB add-on provides integration of MongoDB Java clients your application to connect with MongoDB instances. 
<!--more-->

## Dependencies

Add the following dependency:

{{< dependency g="org.seedstack.addons.mongodb" a="mongodb" >}}
    
You also need to add the MongoDB Java client:

{{< dependency g="org.mongodb" a="mongo-java-driver" v="..." >}}

You can choose to use the MongoDB asynchronous client instead (or in addition as you can mix asynchronous and synchronous
clients in the same application):
   
{{< dependency g="org.mongodb" a="mongodb-driver-async" v="..." >}}
     
## Configuration

Configuration is done by declaring one or more clients:

{{% config p="mongoDb" %}}
```yaml
mongoDb:
  # Configured clients with the name of the client as key
  clients:
    client1:
      # If true, the client is considered asynchronous (defaults to false)
      async: (boolean)
      # URI to connect the client to
      uri: (String)
      # List of hosts to connect the client to (only considered if no uri is specified)
      hosts: (List<String>)
      # Only when using hosts: list of credentials to use (formatted as '[authMechanism/]dbName:user:password')
      credentials: (List<String>)
      # Only for synchronous clients: options based on MongoClientOptions class
      options:
        ...
      # Only for asynchronous clients: options based on the various Settings classes
      settings:
        ...
      # Databases available through this client with the name of the database as key
      databases:
        db1:
          # If specified the database will be injectable through this alias instead of its original name
          # This is useful for database name collisions between multiple clients
          alias: (String)
```
{{% /config %}}  

{{% callout info %}}
As MongoDB has a different Java driver for synchronous and asynchronous clients, the type of a client will determine how 
it can be configured and used. Clients use the [synchronous driver](http://mongodb.github.io/mongo-java-driver/3.0/driver/) 
by default, to switch to the [asynchronous driver](http://mongodb.github.io/mongo-java-driver/3.0/driver-async/), set
the `async` configuration property to `true`.
{{% /callout %}}  

### URI connection string          

The `uri` property is formatted as below:
  
```yaml
mongoDb:
  clients:
    client1:
      uri: mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]      
```

URI allows to directly specify a set of options common to synchronous and asynchronous clients. More information about 
the URI and its options can be found [here](http://docs.mongodb.org/manual/reference/connection-string/).
 
{{% callout info %}}
When no port is specified, the default MongoDB port is used (27017).
{{% /callout %}}

### Explicit hosts 

As an alternative a client can be configured by directly specifying the MongoDB host(s):

```yaml
mongoDb:
  clients:
    client1:
      hosts: [ host1:27017, host2 ]      
```

In this case, the client options must be specified using additional properties, which a are different for synchronous and
asynchronous clients. See the [Synchronous client options](#synchronous-client-options) and [Asynchronous client options](#asynchronous-client-options) 
sections below for more information.

{{% callout info %}}
When no port is specified, the default MongoDB port is used (27017).
{{% /callout %}}

When configuring the connection with explicit hosts, connection credentials can be specified as the following:

```yaml
mongoDb:
  clients:
    client1:
      hosts: [ host1:27017, host2 ]
      credentials: [ db1:user1:password1 ]           
```

This will authenticate with the username `user1` and the password `password1`. The user will be looked up in the `db1`
database. The authentication mechanism will be automatically selected. To force an authentication mechanism use the
following syntax:

```yaml
mongoDb:
  clients:
    client1:
      hosts: [ host1:27017, host2 ]
      credentials: [ mechanism/db1:user1:password1 ]           
```

The available authentication mechanisms are `PLAIN`, `MONGODB_CR`, `SCRAM_SHA_1`, `MONGODB_X509` and `GSSAPI`.
 
{{% callout tips %}}
It is recommended to avoid specifying the authentication mechanism as it will be automatically selected. Also note that 
often, only one credential is enough.
{{% /callout %}}

### Databases

You can choose to inject and use the `MongoClient` object(s) directly and access the database(s) programmatically. As a 
convenience, the add-on also allows to inject the `MongoDatabase` object(s) provided they are configured as follow:
  
```yaml
mongoDb:
  clients:
    client1:
      uri: ... 
      databases: [ db1, db2 ]
```
    
Each declared database can then be injected (see the [usage](#usage) section to know how to inject them). Database names 
must be unique across the application so you can encounter a situation when multiple configured clients may need to access 
databases with the same name. In that case, you can use the alias feature. Consider the following clients:

```yaml
mongoDb:
  clients:
    client1:
      uri: ... 
      databases: [ db1, db2 ]
    client2:
      uri: ... 
      databases: [ db2, db3 ]
```
    
The database named `db2` exists in MongoDB instances accessed by both `client1` and `client2`. To resolve this ambiguity, 
one of the `db2` databases must be aliased in the application:
  
```yaml
mongoDb:
  clients:
    client1:
      uri: ... 
      databases: [ db1, db2 ]
    client2:
      uri: ... 
      databases:
        db2:
          alias: db2bis
        db3:  
```
    
In this example, the `db2` database present on the MongoDB instance accessed by `client2` will be referred in the
application by the `db2bis` name. Note that you can use this feature even when there are no name collision.
  
### Synchronous client options

Additional options can be specified on **synchronous** clients with the `options` property of the client.
 
All the options from the [MongoClientOptions.Builder](http://api.mongodb.org/java/3.0/com/mongodb/MongoClientOptions.Builder.html) 
class are available. Each method of the builder translates to an option of the same name. Consider the following example:

```yaml
mongoDb:
  clients:
    client1:
      uri: ... 
      options:
        connectionsPerHost: 75
```       
    
This will invoke the `connectionsPerHost()` method on the option builder with the value `75` converted to an integer.
                   
{{% callout tips %}}
If you use an URI configuration, you can combine the URI options with the `options` property. The latter will  
will complement their URI counterpart and override them if present in both.
{{% /callout %}}                   

### Asynchronous client options 
 
Additional options can be specified on **asynchronous** clients with the `settings` property:

```yaml
mongoDb:
  clients:
    client1:
      async: true
      uri: ... 
      settings:
        namespace1: 
          setting1: value1
        namespace2:
          setting2: value2
          setting3: value3
```   
    
All the settings from the `MongoClientSettings.Builder` builder and its sub-builders are available. Each sub-builder translates
to a setting namespace and each of the builders method translates to a particular setting. The list of the builders and
their corresponding namespace is:

<table class="table table-striped">
<tbody>
<tr>
    <th>Namespace</th>
    <th>Builder</th>
</tr>
<tr>
    <td>cluster</td>
    <td><a href="http://api.mongodb.org/java/3.0/com/mongodb/connection/ClusterSettings.Builder.html">ClusterSettings.Builder</a></td>
</tr>
<tr>
    <td>connectionPool</td>
    <td><a href="http://api.mongodb.org/java/3.0/com/mongodb/connection/ConnectionPoolSettings.Builder.html">ConnectionPoolSettings.Builder</a></td>
</tr>
<tr>
    <td>socket</td>
    <td><a href="http://api.mongodb.org/java/3.0/com/mongodb/connection/SocketSettings.Builder.html">SocketSettings.Builder</a></td>
</tr>
<tr>
    <td>heartbeatSocket</td>
    <td><a href="http://api.mongodb.org/java/3.0/com/mongodb/connection/SocketSettings.Builder.html">SocketSettings.Builder</a></td>
</tr>
<tr>
    <td>server</td>
    <td><a href="http://api.mongodb.org/java/3.0/com/mongodb/connection/ServerSettings.Builder.html">ServerSettings.Builder</a></td>
</tr>
<tr>
    <td>ssl</td>
    <td><a href="http://api.mongodb.org/java/3.0/com/mongodb/connection/SslSettings.Builder.html">SslSettings.Builder</a></td>
</tr>
</tbody>
</table>

Consider the following example:

```yaml
mongoDb:
  clients:
    client1:
      async: true
      uri: ... 
      settings:
        connectionPool: 
          maxSize: 75
```  
        
This will invoke the `maxSize()` method on a `ConnectionPoolSettings.Builder` instance with the value `75` converted to
an integer. This builder instance will in turn be be set on a `MongoClientSettings.Builder` instance via the `connectionPoolSettings()`
method. 

{{% callout info %}}
* The global settings directly available on `MongoClientSettings.Builder` can be specified without namespace. More information 
on the global builder [here](http://api.mongodb.org/java/current/com/mongodb/async/client/MongoClientSettings.Builder.html).
* The `cluster.hosts` and `credentialList` settings are ignored since they are already mapped from the `hosts` and the
`credentials` properties.
{{% /callout %}}

## Usage
 
As MongoDB does not support transactions, usage simply consists in injecting a `MongoClient` or a `MongoDatabase` object 
and using it accordingly to the MongoDB documentation. As an example you can inject the client as the following:

```java
public class SomeClass {
    @Inject
    @Named("client1")
    private MongoClient client1;
}
```

This will inject the configured MongoDB client named `client1`. You can also inject a database directly as the following:
    
```java
public class SomeClass {
    @Inject
    @Named("db1")
    private MongoDatabase db1;
}
```

This will inject the configured MongoDB database named `db1`. Note that you must use the aliased name instead of the 
real database name if you aliased it in the configuration (see the [databases](#databases) section for information
about aliases).

{{% callout info %}}
* If your client or database is configured as synchronous (the default) you must inject the `com.mongodb.MongoClient` and
`com.mongodb.client.MongoDatabase` classes. 
* If your client or database is configured as asynchronous, you must inject the `com.mongodb.async.client.MongoClient` and 
`com.mongodb.async.client.MongoDatabase` classes instead.
{{% /callout %}}

