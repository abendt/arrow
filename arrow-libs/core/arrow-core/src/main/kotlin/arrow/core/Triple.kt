@file:JvmMultifileClass
@file:JvmName("TupleNKt")

package arrow.core

operator fun <A : Comparable<A>, B : Comparable<B>, C : Comparable<C>> Triple<A, B, C>.compareTo(other: Triple<A, B, C>): Int {
  val first = first.compareTo(other.first)
  return if (first == 0) {
    val second = second.compareTo(other.second)
    if (second == 0) third.compareTo(other.third)
    else second
  } else first
}