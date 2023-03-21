package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

const val END_MARKER = "\u0000\u0000\u0003"

fun main() {
    while (true) {
        println("Task (hide, show, exit):")

        when (val input = readln()) {
            "hide" -> {
                println("Input image file:")
                val inputFile = readln()
                println("Output image file:")
                val outputFile = readln()
                println("Message to hide:")
                val message = readln()
                println("Password:")
                val password = readln()

                try {
                    val image = ImageIO.read(File(inputFile))
                    val encryptedMessage = message.xor(password)

                    binaryStringOf(encryptedMessage).forEachIndexed { index, c ->
                        val x = index % image.width
                        val y = index / image.width
                        val bit = c.digitToInt()

                        val color = Color(image.getRGB(x, y))
                        val newColor = Color(
                            color.red,
                            color.green,
                            getNewBlueValue(color.blue, bit)
                        )

                        image.setRGB(x, y, newColor.rgb)
                    }

                    ImageIO.write(image, "png", File(outputFile))

                    println("Input Image: $inputFile")
                    println("Output Image: $outputFile")
                    println("Message saved in $outputFile image.")
                } catch (e: IndexOutOfBoundsException) {
                    println("The input image is not large enough to hold this message.")
                } catch (e: Exception) {
                    println("Can't read input file!")
                }
            }
            "show" -> {
                println("Input image file:")
                val inputFile = readln()
                println("Password:")
                val password = readln()

                try {
                    println("Message:\n")
                    val image = ImageIO.read(File(inputFile))

                    extractBinaryString(image)
                        .chunked(8)
                        .map { byte -> byte.toInt(2).toChar() }
                        .joinToString("")
                        .split(END_MARKER)
                        .first()
                        .xor(password)
                        .let { println(it) }
                } catch (e: Exception) {
                    println("Can't read input file!")
                }
            }
            "exit" -> {
                println("Bye!")
                return
            }
            else -> println("Wrong task: $input")
        }
    }
}

fun String.xor(password: String): String {
    return this.withIndex().map { (index, char) ->
        val passwordChar = password[index % password.length]

        char.code.xor(passwordChar.code)
    }.joinToString("") { it.toChar().toString() }
}

fun binaryStringOf(message: String): String {
    return (message + END_MARKER)
        .encodeToByteArray()
        .joinToString("") {
                byte -> byte.toString(2).padStart(8, '0')
        }
}

fun getNewBlueValue(value: Int, i: Int): Int {
    val leastIsOne = value % 2 == 1
    val newLSB: Boolean = i == 1

    return if (leastIsOne xor newLSB) value xor 1 else value
}

fun extractBinaryString(image: BufferedImage): String {
    val binaryString = StringBuffer()

    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val color = Color(image.getRGB(x, y))
            val lastBit = color.blue.toString(2).last()
            binaryString.append(lastBit)
        }
    }

    return binaryString.toString()
}