package arrow.integrations.kotlinx.serialization

import arrow.core.Nel
import arrow.core.NonEmptyList
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.list

typealias NelSerializer<T> = NonEmptyListSerializer<T>

@Serializer(forClass = NonEmptyList::class)
class NonEmptyListSerializer<T : Any>(private val dataSerializer: KSerializer<T>) : KSerializer<Nel<T>> {
  override val descriptor: SerialDescriptor =
    SerialClassDescImpl(NonEmptyList::class.qualifiedName!!)

  override fun serialize(encoder: Encoder, obj: Nel<T>) =
    dataSerializer.list.serialize(encoder, obj.all)

  override fun deserialize(decoder: Decoder): Nel<T> =
    Nel.fromListUnsafe(dataSerializer.list.deserialize(decoder))
}

fun <T : Any> NonEmptyList.Companion.serializer(ser: KSerializer<T>): NonEmptyListSerializer<T> =
  NonEmptyListSerializer(ser)
