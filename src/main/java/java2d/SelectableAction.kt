package java2d

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action

class SelectableAction(
    name: String,
    selected: Boolean = false,
    private val handler: (Action) -> Unit
) : AbstractAction(name)
{
    init {
        isSelected = selected
    }
    override fun actionPerformed(e: ActionEvent?) {
        handler(this)
    }
}
