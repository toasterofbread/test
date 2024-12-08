package dev.toastbits.lifelog.core.specification.model

import dev.toastbits.lifelog.core.specification.model.UserContent.Mod
import dev.toastbits.lifelog.core.specification.model.UserContent.Part
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

data class UserContent(
    val parts: List<Part>
) {
    sealed interface Part {
        val parts: List<Part>
        val mods: Set<Mod>

        fun asText(): String
        fun withModifiers(mods: Set<Mod>): Part
        fun isEmpty(): Boolean
        fun isBlank(): Boolean

        data class Single(
            val text: String,
            override val mods: Set<Mod> = emptySet()
        ): Part {
            override val parts: List<Part> get() = listOf(this)
            override fun asText(): String = text
            override fun withModifiers(mods: Set<Mod>): Part = copy(mods = mods)
            override fun isEmpty(): Boolean = text.isEmpty()
            override fun isBlank(): Boolean = text.isBlank()
        }

        data class Composite(
            override val parts: List<Part>,
            override val mods: Set<Mod> = emptySet()
        ): Part {
            override fun asText(): String = parts.joinToString("") { it.asText() }
            override fun withModifiers(mods: Set<Mod>): Part = copy(mods = mods)
            override fun isEmpty(): Boolean = parts.all { it.isEmpty() }
            override fun isBlank(): Boolean = parts.all { it.isBlank() }
        }

        data class Image(
            val location: String,
            override val mods: Set<Mod> = emptySet()
        ): Part {
            override val parts: List<Part> get() = listOf(this)
            override fun asText(): String = "<image at $location>"
            override fun withModifiers(mods: Set<Mod>): Part = copy(mods = mods)
            override fun isEmpty(): Boolean = location.isEmpty()
            override fun isBlank(): Boolean = location.isBlank()
        }
    }

    sealed interface Mod {
        data object Italic: Mod
        data object Bold: Mod
        data object Strikethrough: Mod
        data object Code: Mod
        data object CodeBlock: Mod
        data class Heading(val level: Int): Mod
        data class Reference(val reference: LogEntityReference): Mod
    }

    fun asText(): String = parts.joinToString("") { it.asText() }
    
    fun normalised(): UserContent = UserContent(parts.normalised())

    fun isEmpty(): Boolean =
        parts.all { it.isEmpty() }

    fun isBlank(): Boolean =
        parts.all { it.isBlank() }

    companion object {
        fun single(text: String, mods: Set<Mod> = emptySet()): UserContent =
            UserContent(listOf(Part.Single(text, mods)))

        val EMPTY: UserContent
            get() = UserContent(emptyList())
    }
}

fun UserContent.containsText(text: String, ignoreCase: Boolean = false): Boolean =
    anyPart(
        singlePredicate = { it.text.contains(text, ignoreCase = ignoreCase) },
        imagePredicate = { false }
    )

fun UserContent.anyPart(
    singlePredicate: (Part.Single) -> Boolean,
    imagePredicate: (Part.Image) -> Boolean
): Boolean =
    parts.any { it.anyPart(singlePredicate, imagePredicate) }

fun Part.anyPart(
    singlePredicate: (Part.Single) -> Boolean,
    imagePredicate: (Part.Image) -> Boolean
): Boolean =
    when (this) {
        is Part.Composite -> parts.any { it.anyPart(singlePredicate, imagePredicate) }
        is Part.Image -> imagePredicate(this)
        is Part.Single -> singlePredicate(this)
    }

fun Iterable<Mod>.sorted(): List<Mod> =
    sortedBy {
        when (it) {
            Mod.Bold -> 0
            Mod.Italic -> 1
            Mod.Strikethrough -> 2
            Mod.Code -> 3
            Mod.CodeBlock -> 4
            is Mod.Heading -> 5
            is Mod.Reference -> 6
        }
    }

private fun List<Part>.normalised(): List<Part> {
    val newParts: MutableList<Part> = mutableListOf()
    for (part in this) {
        val previous: Part? = newParts.lastOrNull()
        if (previous != null && part.mods.matches(previous.mods) && part !is Part.Image && previous !is Part.Image) {
            newParts[newParts.size - 1] = part.appendTo(previous, part.mods)
        }
        else {
            newParts.add(part)
        }
    }

    val i: MutableListIterator<Part> = newParts.listIterator()
    while (i.hasNext()) {
        val part: Part = i.next()

        if (part !is Part.Composite) {
            continue
        }

        val normalisedParts: List<Part> = part.parts.normalised()
        if (normalisedParts.isEmpty()) {
            i.remove()
        }
        else if (normalisedParts.size == 1) {
            val singlePart: Part = normalisedParts.single()
            i.set(singlePart.withModifiers(singlePart.mods + part.mods))
        }
        else {
            i.set(part.copy(parts = normalisedParts))
        }
    }

    return newParts
}

private fun Part.appendTo(other: Part, newMods: Set<Mod>): Part {
    if (this is Part.Single && other is Part.Single) {
        return Part.Single(other.text + text, newMods)
    }

    return Part.Composite(other.parts + parts, newMods)
}

private fun Set<Mod>.matches(other: Set<Mod>): Boolean =
    size == other.size && sorted() == other.sorted()
