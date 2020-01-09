package arrow.integrations.kotlinx.serialization

import io.kotlintest.properties.Gen
import kotlinx.serialization.Serializable

@Serializable
data class SomeObject(val someString: String, val someInt: Int)

fun Gen.Companion.someObject(): Gen<SomeObject> =
  bind(string(), int()) { str, int ->
    SomeObject(str, int)
  }
