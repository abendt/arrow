package arrow.integrations.kotlinx.serialization

import arrow.core.Option
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.internal.nullable

@Serializer(forClass = Option::class)
class OptionSerializer<T : Any>(private val dataSerializer: KSerializer<T>) : KSerializer<Option<T>> {

  override val descriptor: SerialDescriptor =
    SerialClassDescImpl(Option::class.qualifiedName!!)

  override fun serialize(encoder: Encoder, obj: Option<T>) =
    obj.fold({ encoder.encodeNull() }, { dataSerializer.serialize(encoder, it) })

  override fun deserialize(decoder: Decoder): Option<T> =
    Option.fromNullable(dataSerializer.nullable.deserialize(decoder))
}

fun <T : Any> Option.Companion.serializer(ser: KSerializer<T>): OptionSerializer<T> =
  OptionSerializer(ser)
