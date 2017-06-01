package com.bytewelder.kip8.chip

import com.bytewelder.kip8.lang.toIntUnsigned

/**
 * 60 Hz timer
 */
class DelayTimer {
	private var value = 0

	fun update() {
		if (value > 0) {
			value -= 1
		}
	}

	fun reset() {
		value = 0
	}

	fun set(newValue: Byte) {
		value = newValue.toIntUnsigned()
	}

	fun get(): Byte {
		return value.toByte()
	}
}