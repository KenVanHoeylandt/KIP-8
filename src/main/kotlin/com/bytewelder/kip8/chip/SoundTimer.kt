package com.bytewelder.kip8.chip

import javax.sound.midi.MidiSystem


/**
 * 60 Hz timer
 */
class SoundTimer {
	val lastTick = System.currentTimeMillis()
	var value = 0.toByte()
	val midiSynthesizer = MidiSystem.getSynthesizer()!!

	init {
		midiSynthesizer.open()
		val instruments = midiSynthesizer.defaultSoundbank.instruments
		midiSynthesizer.loadInstrument(instruments[0])
	}

	fun update() {
		val newValue = updateValue()

		if (newValue != value) {
			updateSynthForValueChange(newValue)
			value = newValue
		}
	}

	private fun updateSynthForValueChange(newValue: Byte) {
		if (newValue > 0) {
			midiSynthesizer.channels[0].noteOn(60, 100)
		} else {
			midiSynthesizer.channels[0].noteOff(60)
		}
	}

	private fun updateValue(): Byte {
		if (value > 0) {
			val current = System.currentTimeMillis()
			if (current - lastTick >= 17) {
				return (value - 1).toByte()
			} else {
				return value
			}
		} else {
			return value
		}
	}

	fun reset() {
		value = 0
	}
}