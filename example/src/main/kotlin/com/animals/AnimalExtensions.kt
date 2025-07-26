package com.animals

fun Animal.asciiDescription(): String {
    val animalName = "Animal: ${this.name}"
    val animalText = "A ${this.name} makes ${makeSound()} when it gets hungry."
    val info = if (isExtinct) "But don't you worry, it does not exist anymore" else "If you meet one, you could feed it."
    val minLineLength = maxOf(animalName.length, animalText.length, info.length)

    return buildString {
        appendLine("┌─" + "─".repeat(minLineLength) + "─┐")
        appendLine("│ " + animalName + " ".repeat(minLineLength - animalName.length) + " │")
        appendLine("│ " + animalText + " ".repeat(minLineLength - animalText.length) + " │")
        appendLine("│ " + info + " ".repeat(minLineLength - info.length) + " │")
        appendLine("└─" + "─".repeat(minLineLength) + "─┘")
    }
}