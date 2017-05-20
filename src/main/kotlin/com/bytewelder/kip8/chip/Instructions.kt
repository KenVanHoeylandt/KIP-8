package com.bytewelder.kip8.chip

fun readInstruction(data: ByteArray, index: Int): Int {
	val dataIndex = index
	val leftByte = data[dataIndex].toInt()
	val rightByte = data[dataIndex + 1].toInt()
	val instruction = (leftByte shl 8) or rightByte and 0x0000FFFF
	return instruction
}
