package com.bytewelder.kip8

import com.bytewelder.kip8.chip.Core
import com.bytewelder.kip8.chip.programOf
import com.bytewelder.kip8.ui.Frame
import com.bytewelder.kip8.ui.Screen
import com.bytewelder.kip8.ui.ScreenBuffer
import java.awt.EventQueue

open class Application(val programFile: String) {
	private val core = Core()
	private val screenBuffer = ScreenBuffer(64, 32)
	private val screen = Screen(screenBuffer, 8)

	fun run() {

		EventQueue.invokeLater {
			val frame = Frame(screen)
			frame.isVisible = true
		}

		val program = programOf(programFile)
		core.run(program)
	}
}