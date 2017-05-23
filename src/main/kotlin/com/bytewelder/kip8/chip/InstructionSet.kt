package com.bytewelder.kip8.chip

class InstructionSet(private val vm: VirtualMachine) {

	private val callStack = mutableListOf<Int>()

	fun execute(instruction: Int) {
//		println("executing " + String.format("%02X", instruction))
		val header = instruction and 0xF000 // first 4 bits
		val footer = instruction and 0x000F // last 4 bits

		when {
			(instruction == 0x00E0)               -> doClearScreen()
			(instruction == 0x00EE)               -> doReturnFromSubRoutine()
			(header == 0x1000)                    -> doJumpToAddress(instruction)
			(header == 0x2000)                    -> doCallSubRoutine(instruction)
			(header == 0x3000)                    -> doSkipNextIfRegisterEquals(instruction)
			(header == 0x4000)                    -> doSkipNextIfRegisterNotEquals(instruction)
			(header == 0x5000) && (footer == 0x0) -> doSkipIfRegisterEqualsRegister(instruction)
			(header == 0x6000)                    -> doSetRegister(instruction)
			(header == 0x7000)                    -> doAddToRegister(instruction)
			(header == 0x8000) && (footer == 0x0) -> doSetRegisterToRegister(instruction)
			(header == 0x8000) && (footer == 0x1) -> doSetRegistersBitwiseOr(instruction)
			(header == 0x8000) && (footer == 0x2) -> doSetRegistersBitwiseAnd(instruction)
			(header == 0x8000) && (footer == 0x3) -> doSetRegistersBitwiseXor(instruction)
			(header == 0x8000) && (footer == 0x4) -> doAddRegisterToRegister(instruction)
			(header == 0x8000) && (footer == 0x5) -> doSubtractRegisterFromRegister(instruction)
			(header == 0x8000) && (footer == 0x6) -> doShiftRegisterOneRight(instruction)
			(header == 0x8000) && (footer == 0x7) -> doSubtractRegisterFromRegisterReverse(instruction)
			(header == 0x8000) && (footer == 0xE) -> doShiftRegisterOneLeft(instruction)
			(header == 0x9000)                    -> doSkipNextIfRegistersNotEqual(instruction)
			(header == 0xA000)                    -> doSetI(instruction)
			(header == 0xB000)                    -> doJumpWithRegister0(instruction)
			(header == 0xC000)                    -> doSetRegisterRandom(instruction)
			(header == 0xD000)                    -> drawSprite(instruction)
			((instruction and 0xF0FF) == 0xE09E)  -> doSkipNextIfKeyPressed(instruction)
			((instruction and 0xF0FF) == 0xE0A1)  -> doSkipNextIfKeyNotPressed(instruction)
			((instruction and 0xF0FF) == 0xF007)  -> doStoreDelayTimerInRegister(instruction)
			((instruction and 0xF0FF) == 0xF00A)  -> doWaitForKeyPress(instruction)
			((instruction and 0xF0FF) == 0xF015)  -> doSetDelayTimerFromRegister(instruction)
			((instruction and 0xF0FF) == 0xF018)  -> doSetSoundTimerFromRegister(instruction)
			((instruction and 0xF0FF) == 0xF01E)  -> doAddRegisterToI(instruction)
			((instruction and 0xF0FF) == 0xF029)  -> doSetFontAddressToI(instruction)
			((instruction and 0xF0FF) == 0xF033)  -> doStoreBinaryCodedDecimal(instruction)
			((instruction and 0xF0FF) == 0xF055)  -> doCopyFromAllRegisters(instruction)
			((instruction and 0xF0FF) == 0xF065)  -> doCopyToAllRegisters(instruction)
			else -> {
				vm.currentInstructionAddress += 2
				println("unknown instruction: " + String.format("%02X", instruction))
			}
		}
	}

	// region Instructions

	/**
	 * 00E0 - Clear the screen
	 */
	fun doClearScreen() {
		vm.screenBuffer.resetBuffer()
		vm.currentInstructionAddress += 2
	}

	/**
	 * 00EE - Return from subroutine
	 */
	fun doReturnFromSubRoutine() {
		vm.currentInstructionAddress = callStack[callStack.lastIndex] + 2
		callStack.removeAt(callStack.lastIndex)
	}

	/**
	 * 1NNN - Go to address NNN
	 */
	private fun doJumpToAddress(instruction: Int) {
		vm.currentInstructionAddress = 0x0FFF and instruction
	}

	/**
	 * 2NNN - Call subroutine at address NNN
	 */
	private fun doCallSubRoutine(instruction: Int) {
		callStack.add(vm.currentInstructionAddress)
		vm.currentInstructionAddress = 0x0FFF and instruction
	}

	/**
	 * 3XNN - Skip next instruction if VX equals NN
	 */
	private fun doSkipNextIfRegisterEquals(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val n = (instruction and 0x00FF).toByte()
		if (vm.registers[register] == n) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	/**
	 * 4XNN - Skip next instruction if VX does not equal NN
	 */
	private fun doSkipNextIfRegisterNotEquals(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val n = (instruction and 0x00FF).toByte()
		if (vm.registers[register] != n) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	/**
	 * 5XY0 - Skip next instruction if VX equals VY
	 */
	private fun doSkipIfRegisterEqualsRegister(instruction: Int) {
		val firstRegister = (instruction and 0x0F00) shr 8
		val secondRegister = (instruction and 0x00F0) shr 4
		if (vm.registers[firstRegister] == vm.registers[secondRegister]) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	/**
	 * 6XNN - Set VX to NN
	 */
	private fun doSetRegister(instruction: Int) {
		val register = (0x0F00 and instruction) shr 8
		val value = (0x00FF and instruction).toByte()
		vm.registers[register] = value
		vm.currentInstructionAddress += 2
	}

	/**
	 * 7XNN - Add NN to VX
	 */
	private fun doAddToRegister(instruction: Int) {
		val register = (0x0F00 and instruction) shr 8
		val value = (0x00FF and instruction).toByte()
		vm.registers[register] = (vm.registers[register] + value).toByte()
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY0 - Set VX to value of VY
	 */
	private fun doSetRegisterToRegister(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = vm.registers[second]
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY1 - Set VX to (VX OR VY)
	 */
	private fun doSetRegistersBitwiseOr(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = (vm.registers[first].toInt() or vm.registers[second].toInt()).toByte()
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY2 - Set VX to (VX AND VY)
	 */
	private fun doSetRegistersBitwiseAnd(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = (vm.registers[first].toInt() and vm.registers[second].toInt()).toByte()
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY3 - Set VX to (VX XOR VY)
	 */
	private fun doSetRegistersBitwiseXor(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = (vm.registers[first].toInt() xor vm.registers[second].toInt()).toByte()
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY4 - Add VY to VX
	 * VF is set to 1 when on carry happens or 0 when no carry happens.
	 */
	private fun doAddRegisterToRegister(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = vm.registers[first] + vm.registers[second]
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = if (result > 0xF) 1 else 0
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY5 - (VX -= VY)
	 * VF becomes 0 when borrow happens, 1 when no borrow happens
	 */
	private fun doSubtractRegisterFromRegister(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = vm.registers[first] - vm.registers[second]
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = if (result < 0) 0 else 1
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY6 - VX >> 1
	 * VF is then set to least significant bit of VX from before shift
	 */
	private fun doShiftRegisterOneRight(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = second shr 1
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = (second and 0x1).toByte()
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XY7 - (VX = VY - VX)
	 * VF becomes 0 when borrow happens, 1 when no borrow happens
	 */
	private fun doSubtractRegisterFromRegisterReverse(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = vm.registers[second] - vm.registers[first]
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = if (result < 0) 0 else 1
		vm.currentInstructionAddress += 2
	}

	/**
	 * 8XYE - VX << 1
	 * VF is then set to the most significant bit of VX from before shift
	 */
	private fun doShiftRegisterOneLeft(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = second shl 1
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = (second and 0x8000).toByte()
		vm.currentInstructionAddress += 2
	}

	/**
	 * 9XY0 - Skip next if registers not equal
	 */
	private fun doSkipNextIfRegistersNotEqual(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		if (vm.registers[first] != vm.registers[second]) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	/**
	 * ANNN - Set I to NNN
	 */
	private fun doSetI(instruction: Int) {
		val value = 0x0FFF and instruction
		vm.i = value
		vm.currentInstructionAddress += 2
	}

	/**
	 * BNNN - Jump to address (NNN + V0)
	 */
	private fun doJumpWithRegister0(instruction: Int) {
		val address = 0x0FFF and instruction
		vm.currentInstructionAddress = address + vm.registers[0]
	}

	/**
	 * CXNN - Set VX to bitwise operation of random number and NN
	 */
	private fun doSetRegisterRandom(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val mask = 0x00FF and instruction
		val result = (vm.random.nextInt() and mask)
		vm.registers[register] = (result and 0xF).toByte()
		vm.currentInstructionAddress += 2
	}

	/**
	 * DXYN - Draw sprite at coordinate (VX, VY) with N bytes of information.
	 * Each byte represents a row of 8 pixels.
	 * If any pixels are flipped to 0, VF is set to 1.
	 * If no pixels are flipped, VX is set to 0.
	 */
	private fun drawSprite(instruction: Int) {
		// Parse instruction parts
		val registerX = (instruction and 0x0F00) shr 8
		val registerY = (instruction and 0x00F0) shr 4
		val spriteBytes = instruction and 0x000F

		// Read position on screen
		val startPosX = vm.registers[registerX]
		val startPosY = vm.registers[registerY]

		if (startPosX < 0 || startPosX > 0x3F) {
			throw IllegalArgumentException("X is out of bounds")
		} else if (startPosY < 0 || startPosY > 0x1F) {
			throw IllegalArgumentException("Y is out of bounds")
		}

		var spriteAddressCurrent = vm.i
		val spriteAddressLast = spriteAddressCurrent + spriteBytes

		var relativePosY = 0
		var anyPixelsTurnedOff = false

		while (spriteAddressCurrent < spriteAddressLast) {
			val currentByte = vm.memory[spriteAddressCurrent]

			for (relativePosX in 0..7) {
				val pixelBit = (currentByte.toInt() shr (7 - relativePosX)) and 0x1
				val isOn = pixelBit != 0
				if (!isOn) {
					anyPixelsTurnedOff = true
				}
				vm.screenBuffer.set(startPosX + relativePosX, startPosY + relativePosY, isOn)
			}

			relativePosY++
			spriteAddressCurrent++
		}

		vm.registers[0xF] = if (anyPixelsTurnedOff) 0x01 else 0x00
		vm.currentInstructionAddress += 2
	}

	/**
	 * EX9E - Skip next instruction when key is pressed.
	 * Key value is defined by VX.
	 */
	private fun doSkipNextIfKeyPressed(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val keyCode = vm.registers[register]
		if (vm.keyboard.isKeyPressed(keyCode)) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	/**
	 * EXA1 - Skip next instruction when key is not pressed.
	 * Key value is defined by VX.
	 */
	private fun doSkipNextIfKeyNotPressed(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val keyCode = vm.registers[register]
		if (!vm.keyboard.isKeyPressed(keyCode)) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	/**
	 * FX07 - Set VX to delay timer value
	 */
	private fun doStoreDelayTimerInRegister(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.registers[register] = vm.delayTimer.get()
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX0A - Wait for key press and store result in VX (blocking operation)
	 */
	private fun doWaitForKeyPress(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val key = vm.keyboard.waitForKeyPress()
		vm.registers[register] = key
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX15 - Set the delay timer to VX
	 */
	private fun doSetDelayTimerFromRegister(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.delayTimer.set(vm.registers[register])
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX18 - Set the sound timer to VX
	 */
	private fun doSetSoundTimerFromRegister(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.soundTimer.set(vm.registers[register])
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX1E - Add VX to I
	 */
	private fun doAddRegisterToI(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.i = vm.i + vm.registers[register]
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX55 - Store [V0, VX] in memory starting at address I
	 * I is set to I + X + 1 after copying
	 */
	private fun doCopyFromAllRegisters(instruction: Int) {
		val lastRegister = (instruction and 0x0F00) shr 8
		for (register in 0..lastRegister) {
			vm.memory[vm.i + register] = vm.registers[register]
		}
		vm.i = vm.i + lastRegister + 1
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX65 - Write [V0, VX] from memory starting at address I
	 * I is set to I + X + 1 after copying
	 *
	 * Test with: BRIX
	 */
	private fun doCopyToAllRegisters(instruction: Int) {
		val lastRegister = (instruction and 0x0F00) shr 8
		for (register in 0..lastRegister) {
			vm.registers[register] = vm.memory[vm.i + register]
		}
		vm.i = vm.i + lastRegister + 1
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX29 - Set font address from character in VX to I
	 */
	private fun doSetFontAddressToI(instruction: Int) {
		println("FX29 is not implemented")
		vm.currentInstructionAddress += 2
	}

	/**
	 * FX33 - Store BCD from register VX in I, I+1, and I+2
	 */
	private fun doStoreBinaryCodedDecimal(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val value = vm.registers[register]

		vm.memory[vm.i]     = (value / 100).toByte()
		vm.memory[vm.i + 1] = ((value % 100) / 10).toByte()
		vm.memory[vm.i + 2] = (value % 10).toByte()

		vm.currentInstructionAddress += 2
	}
	// endregion
}