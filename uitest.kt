@Composable
fun declareUi() {
    Box(Modifier.background { getDynamicBackgroundColour() }) {

    }
}

fun getDynamicBackgroundColour(): Color =
    if (Time.seconds % 2 == 0) Color.White
    else Color.White


@Composable
fun Box(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val composition: Composition = LocalCompositionScope.current
    composition.addNode(control, content)
}


open class Node {
    open fun applyModifier(modifier: Modfier = Modifier): Modifier {

    }
}

open class Control: Node {
    override fun applyModifier(modifier: Modifier): Modifier {
        val remaining: Modifier = super.applyModifier(modifier)

        val i = remaining.iterator()
        while (i.hasNext()) {
            val part = i.next()
            when (part) {
                is BackgroundModifier -> {
                    val rect = ColorRect()
                    rect.color = part.colour
                    addChild(rect)
                }
                else -> continue
            }

            i.remove()
        }
    }
}

data class Composition(val current_node: Node) {
    fun addNode(node: Node, content: @Composable () -> Unit) {
        current_node.addChild(node)

        CompositionLocalProvider(LocalCompositionScope provides this.copy(current_node = node)) {
            content()
        }
    }
}
