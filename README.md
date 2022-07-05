# ohc4s
ohc4s is a lightweight, idiomatic, type-safe Scala wrapper around java ohc library with automatic codec derivation.

## Using

First, you should import the given instances for the supported types, and then
you can create a cache instance with the capacity you need (in bytes).

```scala
import com.thebinarysoul.ohc4s.cache.Cache
import com.thebinarysoul.ohc4s.codec.given

case class User(name: String, age: Int)

val cache = Cache.create[String, User](64 * 1024 * 1024)
```

You can put your data or receive it in a safe way.

```scala
cache.put("Luna", User("Luna", 7))

val maybeValue: Option[User] = cache.get("Luna")
```

## Supported types

 - Byte, Short, Int, Long, Float, Double, Boolean, String
 - Option[T], List[T], Map[K, V], Array[Byte]
 - Tuples and case classes

## Limitations

Currently, you cannot use recursive case classes

## License
ohc4s is made available under the [Apache 2.0 License](/LICENSE).


