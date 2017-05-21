package com.bytewelder.kip8

import com.bytewelder.kip8.chip.VirtualMachine
import com.bytewelder.kip8.chip.programOf
import com.bytewelder.kip8.ui.Frame
import com.bytewelder.kip8.ui.Screen
import com.bytewelder.kip8.ui.ScreenBuffer
import java.awt.EventQueue

open class Application(val programFile: String) {
	fun run() {
		val screenBuffer = ScreenBuffer(64, 32)
		val screen = Screen(screenBuffer, 8)

		EventQueue.invokeLater {
			val frame = Frame(screen)
			frame.isVisible = true
		}

		val virtualMachine = VirtualMachine(screenBuffer)
		val program = programOf(programFile)
		virtualMachine.run(program)
	}
}