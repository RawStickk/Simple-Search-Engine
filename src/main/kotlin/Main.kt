package search

import java.io.File
import java.io.FileNotFoundException

fun displayMenu() {
    println("\n=== Menu ===")
    println("1. Search")
    println("2. Print full contents")
    println("3. Upload data")
    println("0. Exit\n")
}

fun readFile(): File {
    println("Please, input the file path:")
    val filePath = readln()
    return File(filePath)
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

enum class States {
    MENU, EXIT, FILE, SEARCH, DISPLAY
}

fun main() {
    lateinit var file: File
    var state = States.MENU
    var running = true
    //val invertedIndex: MutableMap<String, Int> =
    while (running) {
        when (state) {
            States.MENU -> {
                displayMenu()
                val input = readln().trim()//ignores extra spaces
                when (input) {
                    "1" -> state = States.SEARCH
                    "2" -> state = States.DISPLAY
                    "3" -> state = States.FILE
                    "0" -> state = States.EXIT
                    else -> println("Input is incorrect.")
                }
            }

            States.DISPLAY -> {
                try {
                    output(file)
                } catch (error: UninitializedPropertyAccessException) {
                    println("Please, upload the file to output its contents.")
                }
                state = States.MENU
            }

            States.SEARCH -> {
                try {
                    output(handleEnquiry(file))
                } catch (error: UninitializedPropertyAccessException) {
                    println("Please, upload the file to start searching.")
                }
                state = States.MENU
            }

            States.FILE -> {
                file = readFile()
                if (file.exists()) {
                    println("File is uploaded successfully.")
                    state = States.MENU
                } else {
                    println("File not found.")
                }
            }

            States.EXIT -> {
                running = false
                println("\nBye!")
            }
        }
    }
}