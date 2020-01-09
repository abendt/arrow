package arrow.integrations.kotlinx.serialization

import arrow.Kind
import arrow.core.ForNonEmptyList
import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.test.UnitSpec
import arrow.test.generators.genK
import arrow.test.generators.nonEmptyList
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class NonEmptyListSerializerTest : UnitSpec() {

  val SK = object : SerK<ForNonEmptyList> {
    override fun <A : Any> serK(ser: KSerializer<A>): KSerializer<Kind<ForNonEmptyList, A>> =
      NonEmptyList.serializer(ser) as KSerializer<Kind<ForNonEmptyList, A>>
  }

  init {
    testLaws(SerializationLaws.laws(SK, Nel.genK()))

    "NelData JSON roundtrip" {
      val json = Json(JsonConfiguration.Stable)

      assertAll(Gen.genNelData()) { obj ->
        val asString = json.stringify(NelData.serializer(), obj)
        val asObj = json.parse(NelData.serializer(), asString)

        asObj shouldBe obj
      }
    }
  }
}

@Serializable
private data class NelData(
  @Serializable(with = NonEmptyListSerializer::class)
  val nel1: Nel<String>,
  @Serializable(with = NelSerializer::class)
  val nel2: Nel<String>
)

private fun Gen.Companion.genNelData(): Gen<NelData> =
  bind(Gen.nonEmptyList(string()), Gen.nonEmptyList(string())) { a, b ->
    NelData(a, b)
  }
