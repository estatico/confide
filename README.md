# Confide

[![Build Status](https://travis-ci.org/estatico/confide.svg?branch=master)](https://travis-ci.org/estatico/confide)

Automatic configuration decoding for Scala

## Setup

Maven:

To be able to use the @Conf macro, you'll need the Paradise compiler plugin. Example:
```xml
<pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.scala-tools</groupId>
                    <artifactId>maven-scala-plugin</artifactId>
                    <version>${scala.plugin.version}</version>
                    <configuration>
                        <compilerPlugins>
                            <compilerPlugin>
                                <groupId>org.scalamacros</groupId>
                                <artifactId>paradise_${scala.version}</artifactId>
                                <version>2.1.0</version>
                            </compilerPlugin>
                        </compilerPlugins>
                    </configuration>
                </plugin>
            .
            .
            .
            </plugins>
.
.
.
</pluginManagement>
```

## Usage

Given the configuration file below -

```hocon
api {
  cache {
    enable: true
    ttl: 5m
  }
  greetings = ["Hello", "Hola", "Bonjour"]
}
```

We can define the following Scala classes to automatically decode the config for us -

```scala
import io.estatico.confide._

@Conf final case class AppConfig(
  api: ApiConf
)

@Conf final case class ApiConf(
  cache: CacheConf,
  greetings: List[String]
)

@Conf final case class CacheConf(
  enable: Boolean,
  ttl: FiniteDuration
)
```

The case classes are pretty self-evident; they simply define the structure of the
config we wish to decode.

The `@Conf` macro will derive an instance of `FromConfObj` for the annotated
case class. `FromConf[A]` and `FromConfObj[A]` are type classes which describes how to decode
config values to type `A`. Derivation leverages the wonderful
[shapeless](https://github.com/milessabin/shapeless) library.
All the `@Conf` macro does is inject an implicit `FromConfObj.derive` into the
case class' companion object.

```scala
scala> ConfideFactory.load[AppConfig]()
AppConfig(ApiConf(CacheConf(true,300000000000 nanoseconds),List(Hello, Hola, Bonjour)))
```
