package search

import java.io.File
import java.io.FileNotFoundException

fun menu() {
    println("\n=== Menu ===")
    println("1. Search")
    println("2. Print full contents")
    println("0. Exit\n")
    println("To upload data, please, use --data command with the file path")
}

fun readFile(input: String): File {
    val file = File(input)

    println(
        if (file.exists()) "File is successfully uploaded!"
        else "Warning: file not found."
    )

    return file
}

fun handleEnquiry(file: File): List<String> {
    println("Enter enquiry, please:")
    var enquiry = ""
    while (enquiry == "") {
        enquiry = readln()
    }
    val result = search(file, enquiry)

    return result
}

fun search(file: File, enquiry: String): List<String> {
    val result: MutableList<String> = mutableListOf()

    file.forEachLine {
        if (it.contains(enquiry, ignoreCase = true)) {
            result.add(it)
        }
    }

    return result.toList()
}

fun output(result: List<String>) {
    if (result.isEmpty()) {
        println("No matching results found.")
    } else {
        println("Search results:")
        result.forEach { println(it) }
    }
}

fun output(file: File) {
    println("File contents:\n")
    file.forEachLine { println(it) }
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    lateinit var file: File

    do {
        menu()
        val state: List<String> = readln().trim().split("\\s+".toRegex()) //ignores extra spaces
        when {
            (state.size == 1) and (state[0] == "1") -> {
                try {
                    output(handleEnquiry(file))
                } catch (error: FileNotFoundException) {
                    println("File not found. Please, input the existing filename.")
                } catch (error: UninitializedPropertyAccessException) {
                    println("Please, enter the filename to start searching.")
                }
            }

            (state.size == 1) and (state[0] == "2") -> {
                try {
                    output(file)
                } catch (error: FileNotFoundException) {
                    println("File not found. Please, input the existing filename.")
                } catch (error: UninitializedPropertyAccessException) {
                    println("Please, enter the filename to output its contents.")
                }
            }

            (state.size == 1) and (state[0] == "0") -> println("\nBye!")
            (state.size == 2) and (state[0] == "--data") -> file = readFile(state[1])
            else -> println("Input is incorrect.")
        }
    } while (!((state.size == 1) and (state[0] == "0")))
}