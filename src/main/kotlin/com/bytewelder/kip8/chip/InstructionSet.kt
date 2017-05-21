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
			else -> {
				vm.currentInstructionAddress += 2
				println("unknown instruction: " + String.format("%02X", instruction))
			}
		}
	}

	// region Instructions

	private fun drawSprite(instruction: Int) {
		val registerX = (instruction and 0x0F00) shr 8
		val registerY = (instruction and 0x00F0) shr 4
		val posX = vm.registers[registerX]
		val posY = vm.registers[registerY]

		if (posX < 0 || posX > 0x3F) {
			throw IllegalArgumentException("X is out of bounds")
		} else if (posY < 0 || posY > 0x1F) {
			throw IllegalArgumentException("Y is out of bounds")
		}

		val spriteBytes = instruction and 0x000F

		var currentAddress = vm.i
		val lastAddress = currentAddress + spriteBytes

		var currentY = 0
		var anyOff = false

		while (currentAddress < lastAddress) {
			val currentByte = vm.memory[currentAddress]

			for (x in 0..7) {
				val isOn = ((currentByte.toInt() shr (7 - x)) and 0x1) != 0
				if (!isOn) {
					anyOff = true
				}
				vm.screenBuffer.set(posX + x, posY + currentY, isOn)
			}

			currentY++
			currentAddress++
		}

		vm.registers[0xF] = if (anyOff) 0x01 else 0x00
		vm.currentInstructionAddress += 2
	}

	private fun doAddRegisterToI(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.i += vm.registers[register]
		vm.currentInstructionAddress += 2
	}

	private fun doSkipNextIfKeyPressed(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val keyCode = vm.registers[register].toInt()
		if (vm.keyboard.isKeyPressed(keyCode)) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	private fun doSkipNextIfKeyNotPressed(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val keyCode = vm.registers[register].toInt()
		if (!vm.keyboard.isKeyPressed(keyCode)) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	private fun doWaitForKeyPress(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val key = vm.keyboard.waitForKeyPress()
		vm.registers[register] = key.toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doSetSoundTimerFromRegister(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.soundTimer.value = vm.registers[register].toInt()
		vm.currentInstructionAddress += 2
	}

	private fun doStoreDelayTimerInRegister(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.registers[register] = vm.delayTimer.value.toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doSetDelayTimerFromRegister(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		vm.delayTimer.value = vm.registers[register].toInt()
		vm.currentInstructionAddress += 2
	}

	private fun doSetRegisterRandom(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val mask = 0x00FF and instruction
		val result = (vm.random.nextInt() and mask)
		vm.registers[register] = result.toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doJumpWithRegister0(instruction: Int) {
		val address = 0x0FFF and instruction
		vm.i = address + vm.registers[0]
		vm.currentInstructionAddress += 2
	}

	private fun doSetI(instruction: Int) {
		val value = 0x0FFF and instruction
		vm.i = value
		vm.currentInstructionAddress += 2
	}

	private fun doSkipNextIfRegistersNotEqual(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		if (vm.registers[first] != vm.registers[second]) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	private fun doShiftRegisterOneLeft(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = second shl 1
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = (second and 0x8000).toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doShiftRegisterOneRight(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = second shr 1
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = (second and 0x1).toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doSubtractRegisterFromRegister(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = vm.registers[first] - vm.registers[second]
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = if (result < 0) 1 else 0 // TODO: check if this is correct
		vm.currentInstructionAddress += 2
	}

	private fun doSubtractRegisterFromRegisterReverse(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = vm.registers[second] - vm.registers[first]
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = if (result < 0) 1 else 0 // TODO: check if this is correct
		vm.currentInstructionAddress += 2
	}

	private fun doAddRegisterToRegister(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		val result = vm.registers[first] + vm.registers[second]
		vm.registers[first] = result.toByte()
		vm.registers[0xF] = if (result > 0xF) 1 else 0
		vm.currentInstructionAddress += 2
	}

	private fun doSetRegistersBitwiseXor(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = (vm.registers[first].toInt() xor vm.registers[second].toInt()).toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doSetRegistersBitwiseAnd(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = (vm.registers[first].toInt() and vm.registers[second].toInt()).toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doSetRegistersBitwiseOr(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = (vm.registers[first].toInt() or vm.registers[second].toInt()).toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doSetRegisterToRegister(instruction: Int) {
		val first = (0x0F00 and instruction) shr 8
		val second = (0x00F0 and instruction) shr 4
		vm.registers[first] = vm.registers[second]
		vm.currentInstructionAddress += 2
	}

	private fun doAddToRegister(instruction: Int) {
		val register = (0x0F00 and instruction) shr 8
		val value = (0x00FF and instruction).toByte()
		vm.registers[register] = (vm.registers[register] + value).toByte()
		vm.currentInstructionAddress += 2
	}

	private fun doSetRegister(instruction: Int) {
		val register = (0x0F00 and instruction) shr 8
		val value = (0x00FF and instruction).toByte()
		vm.registers[register] = value
		vm.currentInstructionAddress += 2
	}

	private fun doSkipNextIfRegisterEquals(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val n = (instruction and 0x00FF).toByte()
		if (vm.registers[register] == n) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	private fun doSkipNextIfRegisterNotEquals(instruction: Int) {
		val register = (instruction and 0x0F00) shr 8
		val n = (instruction and 0x00FF).toByte()
		if (vm.registers[register] != n) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	private fun doSkipIfRegisterEqualsRegister(instruction: Int) {
		val firstRegister = (instruction and 0x0F00) shr 8
		val secondRegister = (instruction and 0x00F0) shr 4
		if (vm.registers[firstRegister] == vm.registers[secondRegister]) {
			vm.currentInstructionAddress += 4
		} else {
			vm.currentInstructionAddress += 2
		}
	}

	private fun doJumpToAddress(instruction: Int) {
		vm.currentInstructionAddress = 0x0FFF and instruction
	}

	private fun doCallSubRoutine(instruction: Int) {
		callStack.add(vm.currentInstructionAddress)
		vm.currentInstructionAddress = 0x0FFF and instruction
	}

	fun doClearScreen() {
		vm.screenBuffer.resetBuffer()
		vm.currentInstructionAddress += 2
	}

	fun doReturnFromSubRoutine() {
		vm.currentInstructionAddress = callStack[callStack.lastIndex] + 2
		callStack.removeAt(callStack.lastIndex)
	}

	// endregion
}