package com.bytewelder.kip8.chip

class Core {
	private val programStartAddress = 0x200
	private val registers = ByteArray(16)
	private val memory = ByteArray(4096)
	private var currentInstructionIndex = programStartAddress

	fun run(program: ByteArray) {
		resetMemory()
		loadProgram(program)
		runProgram()
	}

	private fun loadProgram(program: ByteArray) {
		for (address in 0..program.lastIndex) {
			memory[currentInstructionIndex + address] = program[address]
		}
	}

	private fun runProgram() {

	}

	private fun resetMemory() {
		for (index in 0..memory.lastIndex) {
			memory[index] = 0
		}
	}
}