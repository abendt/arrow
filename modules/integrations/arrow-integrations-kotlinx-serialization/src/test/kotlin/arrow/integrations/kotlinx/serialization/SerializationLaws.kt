package arrow.integrations.kotlinx.serialization

import arrow.Kind
import arrow.test.generators.GenK
import arrow.test.laws.Law
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.BooleanSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

interface SerK<F> {
  fun <A : Any> serK(ser: KSerializer<A>): KSerializer<Kind<F, A>>
}

object SerializationLaws {

  fun <F> laws(SK: SerK<F>, GENK: GenK<F>) = listOf(
    law("String", SK.serK(StringSerializer), GENK, Gen.string()),
    law("Boolean", SK.serK(BooleanSerializer), GENK, Gen.bool()),
    law("Int", SK.serK(IntSerializer), GENK, Gen.int()),
    law("SomeObject", SK.serK(SomeObject.serializer()), GENK, Gen.someObject())
  )

  private fun <F, A : Any> law(
    name: String,
    serializer: KSerializer<Kind<F, A>>,
    GENK: GenK<F>,
    G: Gen<A>
  ): Law =
    Law("JSON roundtrip ($name): deserialize serialized == identity") {
      val json = Json(JsonConfiguration.Stable)

      assertAll(GENK.genK(G)) { fa ->

        val asJsonString = json.stringify(serializer, fa)
        val asObject = json.parse(serializer, asJsonString)

        fa shouldBe asObject
      }
    }
}
