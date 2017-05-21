package com.bytewelder.kip8.chip

import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.util.concurrent.locks.ReentrantLock


class Keyboard {
	val keyWaitLock = ReentrantLock()
	var lastKey = 0
	val keyStates = mutableMapOf<Int, Boolean>()

	init {
		val keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
		keyboardFocusManager.addKeyEventDispatcher { keyEvent ->
			when (keyEvent.id) {
				KeyEvent.KEY_PRESSED -> onKeyPressed(keyEvent.keyCode)
				KeyEvent.KEY_RELEASED -> onKeyReleased(keyEvent.keyCode)
			}
			false
		}
	}

	private fun onKeyReleased(keyCode: Int) {
		synchronized(keyStates) {
			keyStates.put(keyCode, false)
		}
	}

	private fun onKeyPressed(keyCode: Int) {
		if (keyWaitLock.isLocked) {
			keyWaitLock.unlock()
			lastKey = keyCode
		}

		synchronized(keyStates) {
			keyStates.put(keyCode, true)
		}
	}

	fun waitForKeyPress(): Int {
		keyWaitLock.lock()
		return lastKey
	}

	fun isKeyPressed(keyCode: Int): Boolean {
		return keyStates[keyCode]?: false
	}
}