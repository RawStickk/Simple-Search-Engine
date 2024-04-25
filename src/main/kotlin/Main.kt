package search

import java.io.File
import ru.nsk.kstatemachine.*
import kotlinx.coroutines.*
import ru.nsk.kstatemachine.Event

fun displayMenu() {
    println("\n=== Menu ===")
    println("1. Search")
    println("2. Print full contents")
    println("3. Upload data file")
    println("0. Exit\n")
}

fun inputFilePathMessage() {
    println("Please, enter the file path.")
    println("To get back to menu, please, use --exit command.")
}

fun fileNotFoundMessage() {
    println("File not found.")
    println("To get back to menu, please, use --exit command.")
}

fun readFile(input: String): File = File(input)

fun handleEnquiry(file: File): List<String> { //TODO: spaces in enquiry
    println("Enter enquiry, please:")
    var enquiry = ""
    while (enquiry == "") {
        enquiry = readln().trim()
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

object SwitchEvent : Event

fun main() = runBlocking {
    lateinit var file: File

    val searchEngine = createStateMachine(this) {
        addInitialState(States.Menu) {
            onEntry { displayMenu() }
            transitionConditionally<SwitchEvent> {
                direction = {
                    val menuInput = readln().trim()
                    //println("Menu Input: $menuInput")
                    when (menuInput) {
                        "1" -> targetState(States.Searching)
                        "2" -> targetState(States.Contents)
                        "3" -> targetState(States.ReadFile)
                        "0" -> targetState(States.Exit)
                        else -> {
                            println("Wrong input")
                            stay()
                        }
                    }
                }
            }
        }
        addState(States.ReadFile) {
            onEntry { inputFilePathMessage() }
            transitionConditionally<SwitchEvent> {
                direction = {
                    //println("Menu Input: $menuInput")
                    when (val filePath = readln().trim()) {
                        "--exit" -> {
                            targetState(States.Menu)
                        }
                        else -> {
                            val data = readFile(filePath)
                            when(data.exists()) {
                                true -> {
                                    println("File is read successfully")
                                    file = data
                                    targetState(States.Menu)
                                }
                                false -> {
                                    fileNotFoundMessage()
                                    stay()
                                }
                            }
                        }
                    }
                }
            }
        }
        addState(States.Searching) {
            onEntry {
                try {
                    output(handleEnquiry(file))
                } catch (error: UninitializedPropertyAccessException) {
                    println("To start searching, please, upload data first.")
                }
            }
            transition<SwitchEvent> {
                targetState = States.Menu
            }
        }
        addState(States.Contents) {
            onEntry {
                try {
                    output(file)
                } catch (error: UninitializedPropertyAccessException) {
                    println("To display file contents, please, upload data first.")
                }
            }
            transition<SwitchEvent> {
                targetState = States.Menu
            }
        }
        addFinalState(States.Exit)
        onFinished { println("Bye!") }
    }
    while (!searchEngine.isFinished) {
        searchEngine.processEvent(SwitchEvent)
    }
}
