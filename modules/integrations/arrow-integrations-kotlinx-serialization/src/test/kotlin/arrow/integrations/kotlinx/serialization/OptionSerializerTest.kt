package arrow.integrations.kotlinx.serialization

import arrow.Kind
import arrow.core.ForOption
import arrow.core.Option
import arrow.test.UnitSpec
import arrow.test.generators.genK
import arrow.test.generators.option
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class OptionSerializerTest : UnitSpec() {

  val SK = object : SerK<ForOption> {
    override fun <A : Any> serK(ser: KSerializer<A>): KSerializer<Kind<ForOption, A>> =
      Option.serializer(ser) as KSerializer<Kind<ForOption, A>>
  }

  init {
    testLaws(SerializationLaws.laws(SK, Option.genK()))

    "OptionData JSON roundtrip" {
      val json = Json(JsonConfiguration.Stable)

      assertAll(Gen.genOptionData()) { obj ->
        val asString = json.stringify(OptionData.serializer(), obj)
        val asObj = json.parse(OptionData.serializer(), asString)

        asObj shouldBe obj
      }
    }
  }
}

@Serializable
private data class OptionData(
  @Serializable(with = OptionSerializer::class)
  val anOption: Option<String>
)

private fun Gen.Companion.genOptionData(): Gen<OptionData> =
  Gen.option(string()).map {
    OptionData(it)
  }
