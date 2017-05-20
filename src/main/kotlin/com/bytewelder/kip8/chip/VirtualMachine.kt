package com.bytewelder.kip8.chip

import com.bytewelder.kip8.ui.ScreenBuffer

class VirtualMachine internal constructor(private val screenBuffer: ScreenBuffer) {
	private val programStartAddress = 0x200
	private val registers = ByteArray(16)
	private val memory = ByteArray(4096)
	private var currentInstructionAddress = programStartAddress

	fun run(program: ByteArray) {
		resetMemory()
		loadProgram(program)
		runProgram()
	}

	private fun loadProgram(program: ByteArray) {
		for (address in 0..program.lastIndex) {
			memory[currentInstructionAddress + address] = program[address]
		}
	}

	private fun runProgram() {
		do {
			val instruction = readInstruction(memory, currentInstructionAddress)
			currentInstructionAddress += 2
			runInstruction(instruction)
		} while (instruction != 0)
	}

	private fun runInstruction(instruction: Int) {
		print(String.format("%02X - ", instruction))

		when (instruction) {
			0x00E0 -> print("clear screen")
			0x00EE -> print("return from subroutine")
			in 0x0000..0x0FFF-> print("RCA 1802 program")
			in 0x1000..0x1FFF -> print("jump to address")
			in 0x2000..0x2FFF -> print("call subroutine")
			in 0x3000..0x3FFF -> print("skip next instruction if VX equals NN")
			in 0x4000..0x4FFF -> print("skip next instruction if VX doesn't equal NN")
			in 0x5000..0x5FFF -> print("skip next instruction if VX equals VY")
			in 0x6000..0x6FFF -> print("set VX to NN")
			in 0x7000..0x7FFF -> print("add NN to VX")
			in 0x8000..0x8FFF -> print("bitwise operator")
			in 0x9000..0x9FFF -> print("conditional skip")
			in 0xA000..0xAFFF -> print("set I to address NNN")
			in 0xB000..0xBFFF -> print("jumps to address NNN plus V0")
			in 0xC000..0xCFFF -> print("set VX to result of a bitwise AND operation on a random number (typically 0 to 255) and NN")
			in 0xD000..0xDFFF -> print("draw sprite")
			in 0xE000..0xEFFF -> print("key press instruction skipping")
			in 0xF000..0xFFFF -> print("misc")
		}

		print("\n")
	}

	private fun resetMemory() {
		for (index in 0..memory.lastIndex) {
			memory[index] = 0
		}
	}
}