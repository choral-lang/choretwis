# Choral Redis-based Twitter clone

# RetwisJ

Under the folder `retwisj` there is a copy of the code of [RetwisJ](https://docs.spring.io/spring-data/data-keyvalue/examples/retwisj/current/). RetwisJ is a sample project published by the Spring team to showcase how one can use the Spring APIs to build a Twitter clone built on top of Redis using Spring Data Redis. RetwisJ is itself  inspired by the original [Redis](https://redis.io/docs/manual/patterns/twitter-clone/), purposed to illustrate the design and implementation of a simple Twitter clone written using PHP with Redis as database. 

# ChorRetwis

Under `src/main` there are two folders. One, `choral/retwis` contains the Choral code of the RetwisJ re-implementation in Choral. The Choral implementation assume a three-tiered architecture, where three kinds of participants interact: Client, Server, and Repository. 

The other folder, `java` contains the Java files to test the Choral implementation.

Under `retwis` we can find all the Java source code generated from the Choral sources, e.g., Retwis_Client, Retwis_Repository, and Retwis_Server, along with demo files. These showcase how one can combine the Choral-generated classes to implement different deployments of the same system. For example, `DemoLocal` uses in-memory channels to deploy Retwis as a monolith and `DemoHTTP` uses HTTP channels to enable socket-based communication, so that users can distribute the execution of the different participants that make up the architecture.

The other folders (commandInterfaces, databases, emitters, etc.) contain utilities used by the files in the retwis folder. In particular, `org/springframework/data/redis/samples/retwisj` contains source files of the original RetwisJ implementation, which we re-use in our Choral re-implementation to illustrate how one can use Choral to integrate pre-existing Java projects.
