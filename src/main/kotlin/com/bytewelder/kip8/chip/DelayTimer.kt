package com.bytewelder.kip8.chip

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
		value = newValue.toInt()
	}

	fun get(): Byte {
		return value.toByte()
	}
}