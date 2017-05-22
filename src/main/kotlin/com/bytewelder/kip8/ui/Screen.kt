package com.bytewelder.kip8.ui

/**
 * Implementation of a Panel that can render a ScreenBuffer.
 */
class Screen internal constructor(val screenBuffer: ScreenBuffer, val pixelSize: Int) : javax.swing.JPanel(), java.awt.event.ActionListener {
	val timer: javax.swing.Timer
	private val DELAY = 50

	init {
		timer = javax.swing.Timer(DELAY, this)
		timer.start()
		setSize(pixelSize * screenBuffer.columns, pixelSize * screenBuffer.rows)
	}

	override fun paintComponent(graphics: java.awt.Graphics) {
		super.paintComponent(graphics)

		val graphics2d = graphics as java.awt.Graphics2D

		drawBackground(graphics2d)
		drawPixels(graphics2d)
	}

	override fun actionPerformed(e: java.awt.event.ActionEvent) {
		repaint()
	}

	private fun drawPoint(graphics: java.awt.Graphics2D, x: Int, y: Int) {
		val fromX = pixelSize * x
		val fromY = pixelSize * y
		graphics.fillRect(fromX, fromY, pixelSize, pixelSize)
	}

	private fun drawBackground(graphics: java.awt.Graphics2D) {
		graphics.paint = java.awt.Color.black
		graphics.fillRect(0, 0, width, height)
	}

	private fun drawPixels(graphics: java.awt.Graphics2D) {
		graphics.paint = java.awt.Color.green
		screenBuffer.forEachPixel { x, y, isOn ->
			if (isOn) {
				drawPoint(graphics, x, y)
			}
		}
	}
}