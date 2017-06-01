package com.bytewelder.kip8.chip

import com.bytewelder.kip8.lang.toIntUnsigned
import javax.sound.midi.MidiSystem


/**
 * 60 Hz timer
 */
class SoundTimer {
	private var value = 0.toByte()

	val midiSynthesizer = MidiSystem.getSynthesizer()!!

	init {
		midiSynthesizer.open()
		val instruments = midiSynthesizer.defaultSoundbank.instruments
		midiSynthesizer.loadInstrument(instruments[0])
	}

	fun update() {
		val newValue = updateValue()

		if (newValue.toIntUnsigned() == 0 && value.toIntUnsigned() != 0) {
			midiSynthesizer.channels[0].noteOff(60)
		}

		value = newValue
	}

	private fun updateValue(): Byte {
		if (value > 0) {
			return (value - 1).toByte()
		} else {
			return value
		}
	}

	fun reset() {
		value = 0
	}

	fun set(newValue: Byte) {
		if (newValue.toIntUnsigned() != 0 && value.toIntUnsigned() == 0) {
			midiSynthesizer.channels[0].noteOn(60, 100)
		} else if (newValue.toIntUnsigned() == 0 && value.toIntUnsigned() != 0) {
			midiSynthesizer.channels[0].noteOff(60)
		}

		value = newValue
	}
}