package com.bytewelder.kip8.chip

import com.bytewelder.kip8.ui.ScreenBuffer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class InstructionSetTest {
	lateinit var screenBuffer: ScreenBuffer
	lateinit var vm: VirtualMachine
	lateinit var instructionSet: InstructionSet

	@Before
	fun setup() {
		screenBuffer = ScreenBuffer(64, 32)
		vm = VirtualMachine(screenBuffer)
		instructionSet = InstructionSet(vm)
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
