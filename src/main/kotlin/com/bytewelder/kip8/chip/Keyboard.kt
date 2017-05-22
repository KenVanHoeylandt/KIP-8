package com.bytewelder.kip8.chip

import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent


class Keyboard {
	val waitLock = Object()
	var lastKey = 0.toByte()
	val keyStates = mutableMapOf<Byte, Boolean>()
	val keyCodeMap = mapOf<Int, Byte>(
			Pair(KeyEvent.VK_0, 0x0),
			Pair(KeyEvent.VK_1, 0x1),
			Pair(KeyEvent.VK_2, 0x2),
			Pair(KeyEvent.VK_3, 0x3),
			Pair(KeyEvent.VK_4, 0x4),
			Pair(KeyEvent.VK_5, 0x5),
			Pair(KeyEvent.VK_6, 0x6),
			Pair(KeyEvent.VK_7, 0x7),
			Pair(KeyEvent.VK_8, 0x8),
			Pair(KeyEvent.VK_9, 0x9),
			Pair(KeyEvent.VK_0, 0x0),
			Pair(KeyEvent.VK_A, 0xA),
			Pair(KeyEvent.VK_B, 0xB),
			Pair(KeyEvent.VK_C, 0xC),
			Pair(KeyEvent.VK_D, 0xD),
			Pair(KeyEvent.VK_E, 0xE),
			Pair(KeyEvent.VK_F, 0xF)
	)

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
		onKeyReleased(translateKeyCode(keyCode))
	}

	private fun onKeyReleased(keyCode: Byte) {
		synchronized(keyStates) {
			keyStates[keyCode] = false
		}
	}

	private fun onKeyPressed(keyCode: Int) {
		onKeyPressed(translateKeyCode(keyCode))
	}

	private fun onKeyPressed(keyCode: Byte) {
		synchronized(waitLock) {
			waitLock.notifyAll()
		}

		lastKey = keyCode

		synchronized(keyStates) {
			keyStates.put(keyCode, true)
		}
	}

	fun waitForKeyPress(): Byte {
		try {
			synchronized(waitLock) {
				waitLock.wait()
			}
		} catch (caught: InterruptedException) {
			// ignore
		}
		return lastKey
	}

	fun isKeyPressed(keyCode: Byte): Boolean {
		synchronized(keyStates) {
			return keyStates[keyCode]?: false
		}
	}

	fun translateKeyCode(keyCode: Int): Byte {
		return keyCodeMap[keyCode]?: 0
	}
}