package com.bytewelder.kip8.ui

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

class Frame(val screen: Screen) : JFrame() {

	init {
		add(screen)
		addWindowListener(object : WindowAdapter() {
			override fun windowClosing(e: WindowEvent?) {
				val timer = screen.timer
				timer.stop()
			}
		})

		title = "KIP-8"
		defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		isResizable = false
		setSize(64 * screen.pixelSize, 32 * screen.pixelSize + y)
		setLocationRelativeTo(null)
	}
}