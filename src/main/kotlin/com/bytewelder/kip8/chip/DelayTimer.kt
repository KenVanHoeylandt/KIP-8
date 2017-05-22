package com.bytewelder.kip8.chip

/**
 * 60 Hz timer
 */
class DelayTimer {
	private var lastTick = System.currentTimeMillis()
	private var value = 0

	fun update() {
		if (value > 0) {
			val current = System.nanoTime()
			if (current - lastTick >= 16777777) {
				lastTick = System.nanoTime()
				value -= 1
			}
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