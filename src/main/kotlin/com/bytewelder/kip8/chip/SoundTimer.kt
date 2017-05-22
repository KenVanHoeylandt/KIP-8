package com.bytewelder.kip8.chip

import javax.sound.midi.MidiSystem


/**
 * 60 Hz timer
 */
class SoundTimer {
	private var lastTick = System.currentTimeMillis()
	private var value = 0.toByte()

	val midiSynthesizer = MidiSystem.getSynthesizer()!!

	init {
		midiSynthesizer.open()
		val instruments = midiSynthesizer.defaultSoundbank.instruments
		midiSynthesizer.loadInstrument(instruments[0])
	}

	fun update() {
		val newValue = updateValue()

		if (newValue.toInt() == 0 && value.toInt() != 0) {
			midiSynthesizer.channels[0].noteOff(60)
		}

		value = newValue
	}

	private fun updateValue(): Byte {
		if (value > 0) {
			val current = System.nanoTime()
			if (current - lastTick >= 16777777) {
				lastTick = System.nanoTime()
				return (value - 1).toByte()
			}
		}

		return value
	}

	fun reset() {
		value = 0
	}

	fun set(newValue: Byte) {
		if (newValue.toInt() != 0 && value.toInt() == 0) {
			midiSynthesizer.channels[0].noteOn(60, 100)
		} else if (newValue.toInt() == 0 && value.toInt() != 0) {
			midiSynthesizer.channels[0].noteOff(60)
		}

		value = newValue
	}
}