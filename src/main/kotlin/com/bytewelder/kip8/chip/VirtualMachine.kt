package com.bytewelder.kip8.chip

import com.bytewelder.kip8.ui.ScreenBuffer
import java.util.*

class VirtualMachine internal constructor(val screenBuffer: ScreenBuffer) {
	val programStartAddress = 0x200
	val registers = ByteArray(16)
	val memory = ByteArray(4096)
	val instructionSet = InstructionSet(this)
	val random = Random()
	val soundTimer = SoundTimer()
	val delayTimer = DelayTimer()
	val keyboard = Keyboard()
	var currentInstructionAddress = programStartAddress
	var programEndAddress = programStartAddress
	var i = 0
	private var lastTick = System.currentTimeMillis()

	fun run(program: ByteArray) {
		reset()
		loadProgram(program)
		runProgram()
	}

	private fun loadProgram(program: ByteArray) {
		currentInstructionAddress = programStartAddress
		programEndAddress = programStartAddress + program.lastIndex

		for (address in 0..program.lastIndex) {
			memory[currentInstructionAddress + address] = program[address]
		}
	}

	private fun runProgram() {
		do {

			val instruction = readInstruction(currentInstructionAddress)
			instructionSet.execute(instruction)

			waitForTick()

			delayTimer.update()
			soundTimer.update()
		} while (currentInstructionAddress <= programEndAddress) // TODO: remove
	}

	private fun waitForTick() {
		val currentMillis = System.currentTimeMillis()
		val millisPassed = currentMillis - lastTick
		val waitTime = Math.max(0, 17 - millisPassed)
		Thread.sleep(waitTime)
		lastTick = System.currentTimeMillis()
	}

	private fun reset() {
		// reset all memory variables
		currentInstructionAddress = -1
		programEndAddress = -1
		i = -1

		// reset main memory
		for (i in 0..memory.lastIndex) {
			memory[i] = 0
		}

		// reset registers
		for (i in 0..registers.lastIndex) {
			registers[i] = 0
		}

		// reset timers
		soundTimer.reset()
		delayTimer.reset()
	}

	private fun readInstruction(address: Int): Int {
		val leftByte = memory[address].toInt() and 0x000000FF
		val rightByte = memory[address + 1].toInt() and 0x000000FF
		val instruction = (leftByte shl 8) or rightByte
		return instruction
	}
}