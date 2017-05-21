package com.bytewelder.kip8.chip

import java.awt.Toolkit

/**
 * 60 Hz timer
 */
class SoundTimer {
	val lastTick = System.currentTimeMillis()
	var value = 0

	fun update() {
		if (value > 0) {
			val current = System.currentTimeMillis()
			if (current - lastTick >= 17) {
				value -= 1
			}
		}

		if (value > 0) {
			Toolkit.getDefaultToolkit().beep()
		}
	}

	fun reset() {
		value = 0
	}
}