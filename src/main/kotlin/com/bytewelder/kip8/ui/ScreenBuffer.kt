package com.bytewelder.kip8.ui

/**
 * Thread-safe screen buffer that holds pixel on/off values
 */
class ScreenBuffer(val columns: Int, val rows: Int) {
	val pixelBuffer = BooleanArray(columns * rows)

	init {
		resetBuffer()
	}

	fun resetBuffer() {
		synchronized(this) {
			for (i in 0..pixelBuffer.lastIndex) {
				pixelBuffer[i] = false
			}
		}
	}

	fun set(x: Int, y: Int, isOn: Boolean) {
		// Don't draw out of bounds
		if (x < 0 || x >= columns
				|| y < 0 || y >= rows) {
			return
		}

		synchronized(this) {
			val index = getIndex(x, y)
			pixelBuffer[index] = isOn
		}
	}

	fun getIndex(column: Int, row: Int): Int {
		return column + (row * columns)
	}

	fun forEachPixel(closure: (Int, Int, Boolean) -> Unit) {
		synchronized (this) {
			for (row in 0..(rows - 1)) {
				for (column in 0..(columns - 1)) {
					val index = getIndex(column, row)
					val isOn = pixelBuffer[index]
					closure.invoke(column, row, isOn)
				}
			}
		}
	}
}