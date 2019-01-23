/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license 
 * that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("UArraysKt")

package kotlin.collections

//
// NOTE: THIS FILE IS AUTO-GENERATED by the GenerateStandardLib.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//


/**
 * Fills original array with the provided value.
 */
@SinceKotlin("1.3")
@ExperimentalUnsignedTypes
public fun UIntArray.fill(element: UInt, fromIndex: Int = 0, toIndex: Int = size): Unit {
    storage.fill(element.toInt(), fromIndex, toIndex)
}

/**
 * Fills original array with the provided value.
 */
@SinceKotlin("1.3")
@ExperimentalUnsignedTypes
public fun ULongArray.fill(element: ULong, fromIndex: Int = 0, toIndex: Int = size): Unit {
    storage.fill(element.toLong(), fromIndex, toIndex)
}

/**
 * Fills original array with the provided value.
 */
@SinceKotlin("1.3")
@ExperimentalUnsignedTypes
public fun UByteArray.fill(element: UByte, fromIndex: Int = 0, toIndex: Int = size): Unit {
    storage.fill(element.toByte(), fromIndex, toIndex)
}

/**
 * Fills original array with the provided value.
 */
@SinceKotlin("1.3")
@ExperimentalUnsignedTypes
public fun UShortArray.fill(element: UShort, fromIndex: Int = 0, toIndex: Int = size): Unit {
    storage.fill(element.toShort(), fromIndex, toIndex)
}

