Silhouette Persistence ReactiveMongo [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.mohiva/play-silhouette-persistence-reactivemongo_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.mohiva/play-silhouette-persistence-reactivemongo_2.11) [![Build Status](https://travis-ci.org/mohiva/play-silhouette-persistence-reactivemongo.png)](https://travis-ci.org/mohiva/play-silhouette-persistence-reactivemongo) [![Coverage Status](https://coveralls.io/repos/mohiva/play-silhouette-persistence-reactivemongo/badge.svg?branch=master&service=github)](https://coveralls.io/github/mohiva/play-silhouette-persistence-reactivemongo?branch=master) [![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/mohiva/play-silhouette?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
==========

An implementation of the [Silhouette] persistence layer using [ReactiveMongo].

## Usage

In your project/Build.scala:

```scala
libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette-persistence-reactivemongo" % "4.0.0-RC1"
)
```

An instance of the DAO can be created as follow:

```scala
implicit lazy val format = Json.format[OAuth1Info]
val dao = new MongoAuthInfoDAO[OAuth1Info](reactiveMongoApi, config)
```

The Json format is needed to serialize the auth info data into Json. It will be passed implicitly to the DAO instance. 
The ReactiveMongo API and the Play configuration instance should be provided through dependency injection.

To provide bindings for Guice, you should implement a provider for every auth info type:

```scala
/**
 * Provides the implementation of the delegable OAuth1 auth info DAO.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param config The Play configuration.
 * @return The implementation of the delegable OAuth1 auth info DAO.
 */
@Provides
def provideOAuth1InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth1Info] = {
  implicit lazy val format = Json.format[OAuth1Info]
  new MongoAuthInfoDAO[OAuth1Info](reactiveMongoApi, config)
}
```

## Configuration

To define the collection name under which the auth info data should be saved, you must provide a configuration setting 
in the form `silhouette.persistence.reactivemongo.collection.[AuthInfo]`.

As example:
```
silhouette {
  persistence.reactivemongo.collection.OAuth1Info = "auth.info.oauth1"
  persistence.reactivemongo.collection.OAuth2Info = "auth.info.oauth2"
  persistence.reactivemongo.collection.OpenIDInfo = "auth.info.oauth1"
  persistence.reactivemongo.collection.PasswordInfo = "auth.info.passwords"
}
```

If no configuration can be found, then the DAO uses automatically the name of the auth info class prefixed with `auth.`. 
So for the `OAuth1Info` type, it uses the collection name `auth.OAuth1Info`.

## License

The code is licensed under [Apache License v2.0] and the documentation under [CC BY 3.0].

[Silhouette]: http://silhouette.mohiva.com/
[ReactiveMongo]: http://reactivemongo.org/
[Apache License v2.0]: http://www.apache.org/licenses/LICENSE-2.0
[CC BY 3.0]: http://creativecommons.org/licenses/by/3.0/
