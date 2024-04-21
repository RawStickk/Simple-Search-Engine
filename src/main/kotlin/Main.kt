package search

import java.io.File
import java.io.FileNotFoundException
import ru.nsk.kstatemachine.*
import kotlinx.coroutines.*
import java.util.*

fun displayMenu() {
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

fun handleEnquiry(file: File): List<String> { //TODO: spaces in enquiry
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

sealed class States : DefaultState() {
    object Menu : States()
    object ReadFile : States()
    object Contents : States()
    object Searching : States()
    object Exit : States(), FinalState
}

sealed class Events : Event {
    object InputMenu : Events()
    object InputFile : Events()
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(): Unit = runBlocking {
    val scanner = Scanner(System.`in`)
    val searchEngine = createStateMachine(this) {
        addInitialState(States.Menu) {
            onEntry {
                displayMenu()
            }
            transitionConditionally<Events.InputMenu> {
                direction = {
                    val menuInput = scanner.nextLine().trim()
                    //type = TransitionType.EXTERNAL
                    //println("Menu Input: $menuInput")
                    when (menuInput) {
                        "1" -> targetState(States.Contents)
                        "2" -> targetState(States.Searching)
                        "0" -> targetState(States.Exit)
                        "--data" -> targetState(States.ReadFile)
                        else -> {
                            println("Wrong input")
                            stay()
                        }
                    }
                }
            }
        }
        addState(States.ReadFile) {}
        addState(States.Searching) {}
        addState(States.Contents) {}
        addState(States.Exit) {}
    }
//    val running = true
//    while (running) {
//        val inputMenu = readln()
//
//    }
    searchEngine.processEvent(Events.InputMenu)
}


//            println("Enter file path:")
//            val fileInput = scanner.nextLine().trim()
//            //println("File Input: $fileInput")
//            transition<Events.InputFile> {
//                val file = File(fileInput)
//                guard = {file.exists()}
//                targetState = States.Menu
//            }