package com.bytewelder.kip8.chip

fun readInstruction(data: ByteArray, index: Int): Short {
	val dataIndex = index * 2
	val leftByte = data[dataIndex].toInt()
	val rightByte = data[dataIndex + 1].toInt()
	val instruction = (leftByte shl 8) or rightByte
	return instruction.toShort()
}
