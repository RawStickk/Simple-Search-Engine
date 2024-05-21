package search

import java.io.File
import ru.nsk.kstatemachine.*
import kotlinx.coroutines.*

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
}

fun readFile(input: String): File = File(input)

fun handleEnquiry(file: File): List<String> {
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

fun outputSearchResults(result: List<String>) {
    if (result.isEmpty()) {
        println("No matching results found.")
    } else {
        println("Search results:")
        result.forEach { println(it) }
    }
}

fun outputFileContents(file: File) {
    println("File contents:\n")
    file.forEachLine { println(it) }
}

sealed class States : DefaultState() {
    object Menu : States()
    object ReadFilePath : States()
    object Contents : States()
    object Searching : States()
    object FileLoad : States()
    object Exit : States(), FinalState
}

sealed class Events : Event {
    class MenuInput(val input: String) : Events()
    class FilePathInput(val filePath: String) : Events()
    data object Search : Events()
    data object DisplayContents : Events()
    class FileLoadingEvent(val filePath: String): Events()
}

fun main() = runBlocking {
    val searchEngine = createStateMachine(this) {
        lateinit var file: File

        addInitialState(States.Menu) {
            onEntry { displayMenu() }
            transitionConditionally<Events.MenuInput> {
                direction = {
                    when (event.input) {
                        "1" -> targetState(States.Searching)
                        "2" -> targetState(States.Contents)
                        "3" -> targetState(States.ReadFilePath)
                        "0" -> targetState(States.Exit)
                        else -> {
                            println("Wrong input")
                            noTransition()
                        }
                    }
                }
            }
        }
        addState(States.ReadFilePath) {
            onEntry { inputFilePathMessage() }
            transitionConditionally<Events.FilePathInput> {
                direction = {
                    if (event.filePath == "--exit") {
                        targetState(States.Menu)
                    } else {
                        targetState(States.FileLoad)
                    }
                }
            }
        }
        addState(States.FileLoad) {
            transitionConditionally<Events.FileLoadingEvent> {
                direction = {
                    val data = readFile(event.filePath)
                    if (data.exists() and data.isFile) {
                        println("File is read successfully")
                        file = data
                        targetState(States.Menu)
                    } else {
                        fileNotFoundMessage()
                        targetState(States.ReadFilePath)
                    }
                }
            }
        }
        addState(States.Searching) {
            onEntry {
                try {
                    outputSearchResults(handleEnquiry(file))
                } catch (error: UninitializedPropertyAccessException) {
                    println("To start searching, please, upload data first.")
                }
            }
            transition<Events.Search> {
                targetState = States.Menu
            }
        }
        addState(States.Contents) {
            onEntry {
                try {
                    outputFileContents(file)
                } catch (error: UninitializedPropertyAccessException) {
                    println("To display file contents, please, upload data first.")
                }
            }
            transition<Events.DisplayContents> {
                targetState = States.Menu
            }
        }
        addFinalState(States.Exit)
        onFinished { println("Bye!") }
    }

    while (!searchEngine.isFinished) {
        when {
            States.Menu.isActive -> {
                searchEngine.processEvent(Events.MenuInput(readln().trim()))
            }

            States.ReadFilePath.isActive -> {
                val filePath = readln().trim()
                searchEngine.processEvent(Events.FilePathInput(filePath))
                searchEngine.processEvent(Events.FileLoadingEvent(filePath))
            }

            States.Searching.isActive -> {
                searchEngine.processEvent(Events.Search)
            }

            States.Contents.isActive -> {
                searchEngine.processEvent(Events.DisplayContents)
            }
        }
    }
}
