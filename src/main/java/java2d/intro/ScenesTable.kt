package java2d.intro

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSlider
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder
import javax.swing.table.AbstractTableModel

/**
 * ScenesTable is the list of scenes known to the Director.
 * Scene participation, scene name and scene pause amount columns.
 * Global animation delay for scene's steps.
 */
internal class ScenesTable(private val surface: Intro.Surface) : JPanel(BorderLayout())
{
    private val dataModel = object : AbstractTableModel()
    {
        private val columnNames = arrayOf("", "Scenes", "Pause")

        override fun getColumnCount(): Int = columnNames.size

        override fun getRowCount(): Int = surface.director.size

        override fun getValueAt(row: Int, col: Int): Any? {
            val scene = surface.director[row]
            return when (col) {
                0 -> scene.participate
                1 -> scene.name
                2 -> scene.pauseAmt
                else -> null
            }
        }

        override fun getColumnName(col: Int): String = columnNames[col]

        override fun getColumnClass(c: Int): Class<*> = getValueAt(0, c)!!.javaClass

        override fun isCellEditable(row: Int, col: Int): Boolean {
            return col != 1
        }

        override fun setValueAt(value: Any?, row: Int, col: Int) {
            val scene = surface.director[row]
            when (col) {
                0 -> scene.participate = value as Boolean
                1 -> scene.name = value as String
                2 -> scene.pauseAmt = value as Long
            }
        }
    }

    init {
        background = Color.WHITE

        val table = JTable(dataModel)
        table.getColumn("").apply {
            width = 16
            minWidth = 16
            maxWidth = 20
        }
        table.getColumn("Pause").apply {
            width = 60
            minWidth = 60
            maxWidth = 60
        }
        table.sizeColumnsToFit(0)

        add(JScrollPane(table), BorderLayout.CENTER)

        val panel = JPanel(BorderLayout())
        val button = JButton("Unselect All").apply {
            horizontalAlignment = SwingConstants.LEFT
            addActionListener {
                isSelected = !isSelected
                text = if (isSelected) "Select All" else "Unselect All"
                for (scene in surface.director) {
                    scene.participate = !isSelected
                }
                dataModel.fireTableDataChanged()
            }
        }
        panel.add(button, BorderLayout.WEST)

        val slider = JSlider(JSlider.HORIZONTAL, 0, 200, surface.sleepAmt.toInt()).apply {
            fun formatTitle(value: Int) = "Anim delay = $value ms"
            border = TitledBorder(EtchedBorder()).apply {
                title = formatTitle(surface.sleepAmt.toInt())
            }
            addChangeListener {
                (border as TitledBorder).title = formatTitle(value)
                surface.sleepAmt = value.toLong()
                repaint()
            }
            preferredSize = Dimension(140, 40)
            minimumSize = Dimension(100, 40)
            maximumSize = Dimension(180, 40)
        }
        panel.add(slider, BorderLayout.EAST)

        add(panel, BorderLayout.SOUTH)
    }
}
