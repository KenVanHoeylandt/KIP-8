package com.bytewelder.kip8.chip

import java.io.File

fun programOf(filename: String): ByteArray {
	return programOf(File(filename))
}

fun programOf(file: File): ByteArray {
	return file.readBytes()
}