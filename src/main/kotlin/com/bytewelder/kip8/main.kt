package com.bytewelder.kip8

fun main(args: Array<String>) {
	if (args.isEmpty()) {
		error("Error: must specify ROM in argument")
	}

	println("Loading " + args[0])

	val application = Application(args[0])
	application.run()
}