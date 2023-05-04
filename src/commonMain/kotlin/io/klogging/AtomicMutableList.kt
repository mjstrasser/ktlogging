/*

   Copyright 2021-2023 Michael Strasser.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package io.klogging

import kotlinx.atomicfu.atomic

/**
 * A multiplatform, thread-safe [MutableList], implemented using AtomicFU.
 */
internal class AtomicMutableList<E>(
    vararg elements: E
) : MutableList<E> {

    private val list = atomic(mutableListOf(*elements))

    override val size: Int
        get() = list.value.size

    override fun clear(): Unit = list.value.clear()

    override fun addAll(elements: Collection<E>): Boolean = list.value.addAll(elements)

    override fun addAll(index: Int, elements: Collection<E>): Boolean = list.value.addAll(index, elements)

    override fun add(index: Int, element: E) = list.value.add(index, element)

    override fun add(element: E): Boolean = list.value.add(element)

    override fun get(index: Int): E = list.value.get(index)

    override fun isEmpty(): Boolean = list.value.isEmpty()

    override fun iterator(): MutableIterator<E> = list.value.iterator()

    override fun listIterator(): MutableListIterator<E> = list.value.listIterator()

    override fun listIterator(index: Int): MutableListIterator<E> = list.value.listIterator(index)

    override fun removeAt(index: Int): E = list.value.removeAt(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = list.value.subList(fromIndex, toIndex)

    override fun set(index: Int, element: E): E = list.value.set(index, element)

    override fun retainAll(elements: Collection<E>): Boolean = list.value.retainAll(elements)

    override fun removeAll(elements: Collection<E>): Boolean = list.value.removeAll(elements)

    override fun remove(element: E): Boolean = list.value.remove(element)

    override fun lastIndexOf(element: E): Int = list.value.lastIndexOf(element)

    override fun indexOf(element: E): Int = list.value.indexOf(element)

    override fun containsAll(elements: Collection<E>): Boolean = list.value.containsAll(elements)

    override fun contains(element: E): Boolean = list.value.contains(element)
}
