abstract class Modifier {
    abstract val next: Modifier?
    abstract fun applyTo(node: Node)

    abstract fun copy(next: Modifier?): Modifier
}

data class BackgroundModifier(
    val getColour: () -> Color,
    override val next: Modifier? = null
): Modifier {
    override fun copy(next: Modifier?): Modifier = copy(getColour, next)
}

fun Modifier.background(getColour: () -> Color): Modifier
    = copy(next = BackgroundModifier(getColour))

data class SizeModifier(
    val getSize: () -> DpSize,
    override val next: Modifier? = null
): Modifier {
    override fun copy(next: Modifier?): Modifier = copy(getColour, next)
}

fun Modifier.size(getSize: () -> DpSize): Modifier
    = copy(next = SizeModifier(getSize))
