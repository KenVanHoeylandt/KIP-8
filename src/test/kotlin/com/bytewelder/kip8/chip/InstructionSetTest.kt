package com.bytewelder.kip8.chip

import com.bytewelder.kip8.ui.ScreenBuffer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class InstructionSetTest {
	lateinit var screenBufferBytes: BooleanArray
	lateinit var screenBuffer: ScreenBuffer
	lateinit var vm: VirtualMachine
	lateinit var instructionSet: InstructionSet

	@Before
	fun setup() {
		screenBufferBytes = BooleanArray(64 * 32)
		screenBuffer = ScreenBuffer(64, 32, screenBufferBytes)
		vm = VirtualMachine(screenBuffer)
		instructionSet = InstructionSet(vm)
	}

	@Test
	fun clearScreen() {
		// given all pixels are on
		for (i in 0..screenBufferBytes.lastIndex) {
			screenBufferBytes[i] = true
		}

		// when reset instruction is called
		instructionSet.execute(0x00E0)

		// verify that all pixels are on
		for (i in 0..screenBufferBytes.lastIndex) {
			assertThat(screenBufferBytes[i], `is`(false))
		}
	}

	@Test
	fun jump() {
		instructionSet.execute(0x1123) // 0x1NNN

		assertThat(vm.currentInstructionAddress, `is`(0x123))
	}

	@Test
	fun callSubRoutine() {
		instructionSet.execute(0x2123) // 0x2NNN

		assertThat(vm.currentInstructionAddress, `is`(0x123))
	}

	@Test
	fun returnFromSubRoutine() {
		val originalAddress = vm.currentInstructionAddress
		instructionSet.execute(0x2123) // jump to 123
		instructionSet.execute(0x00EE) // return from subroutine

		assertThat(vm.currentInstructionAddress, `is`(originalAddress + 2))
	}

	@Test
	fun bcdAllDigitsNoRounding() {
		vm.registers[0] = 123

		instructionSet.execute(0xF033)

		assertThat(vm.memory[vm.i], `is`(1.toByte()))
		assertThat(vm.memory[vm.i + 1], `is`(2.toByte()))
		assertThat(vm.memory[vm.i + 2], `is`(3.toByte()))
	}

	@Test
	fun bcdSomeDigitsAndRounding() {
		vm.registers[0] = 96

		instructionSet.execute(0xF033)

		assertThat(vm.memory[vm.i], `is`(0.toByte()))
		assertThat(vm.memory[vm.i + 1], `is`(9.toByte()))
		assertThat(vm.memory[vm.i + 2], `is`(6.toByte()))
	}
}
