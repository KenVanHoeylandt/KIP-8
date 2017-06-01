package com.bytewelder.kip8.lang

fun Byte.toIntUnsigned(): Int {
	return this.toInt() and 0xFF
}