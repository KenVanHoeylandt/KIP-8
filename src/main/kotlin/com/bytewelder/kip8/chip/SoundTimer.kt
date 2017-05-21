package com.bytewelder.kip8.chip

import java.awt.Toolkit
import javax.sound.midi.MidiSystem
import javax.sound.midi.MidiChannel
import javax.sound.midi.Instrument



/**
 * 60 Hz timer
 */
class SoundTimer {
	val lastTick = System.currentTimeMillis()
	var value = 0
	val midiSynth = MidiSystem.getSynthesizer()!!

	init {
		midiSynth.open()
		val instruments = midiSynth.defaultSoundbank.instruments
		midiSynth.loadInstrument(instruments[0])
	}

	fun update() {
		val newValue = updateValue()

		if (newValue != value) {
			updateSynthForValueChange(newValue)
			value = newValue
		}
	}

	private fun updateSynthForValueChange(newValue: Int) {
		if (newValue > 0) {
			midiSynth.channels[0].noteOn(60, 100)
		} else {
			midiSynth.channels[0].noteOff(60)
		}
	}

	private fun updateValue(): Int {
		if (value > 0) {
			val current = System.currentTimeMillis()
			if (current - lastTick >= 17) {
				return value - 1
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