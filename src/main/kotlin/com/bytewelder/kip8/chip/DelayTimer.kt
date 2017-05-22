package com.bytewelder.kip8.chip

/**
 * 60 Hz timer
 */
class DelayTimer {
	val lastTick = System.currentTimeMillis()
	var value = 0.toByte()

	fun update() {
		if (value > 0) {
			val current = System.currentTimeMillis()
			if (current - lastTick >= 17) {
				value = (value - 1).toByte()
			}
		}
	}

	fun reset() {
		value = 0
	}
}