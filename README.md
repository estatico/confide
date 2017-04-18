# Confide

Automatic configuration decoding for Scala

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

final class AppConfig extends RelConf {
  val config = load()
  val api = get[ApiConf]
}

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

Extending `RelConf` gives us the `get[A]` method macro. This macro will look up
the config value using the identifier name, so in this case, the `get` macro is
being assigned to `api`, so it will look for the config value at the path
`"api"` relative to its `config`.

The `@Conf` macro will derive an instance of `FromConf` for the annotated
case class. `FromConf[A]` is simply a type class which describes how to decode
config values of type `A`. Derivation leverages the wonderful
[shapeless](https://github.com/milessabin/shapeless) library.
All the `@Conf` macro does is inject an implicit `FromConf.derive` into the
case class' companion object.

```scala
scala> val c = new AppConfig
c: AppConfig = AppConfig@8ff2adc

scala> c.api
res0: ApiConf = ApiConf(CacheConf(true,300000000000 nanoseconds),List(Hello, Hola, Bonjour))
```
