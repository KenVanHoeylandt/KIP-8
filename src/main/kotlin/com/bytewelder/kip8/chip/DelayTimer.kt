package com.bytewelder.kip8.chip

/**
 * 60 Hz timer
 */
class DelayTimer {
	val lastTick = System.currentTimeMillis()
	var value = 0

	fun update() {
		if (value > 0) {
			val current = System.currentTimeMillis()
			if (current - lastTick >= 17) {
				value -= 1
			}
		}
	}
}