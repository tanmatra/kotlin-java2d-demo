/*
 *
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java2d

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Image
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.print.PrinterJob
import java.security.AccessControlException
import java.util.logging.Level
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.swing.AbstractButton
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.JToggleButton
import javax.swing.JToolBar
import javax.swing.SwingConstants
import javax.swing.Timer
import javax.swing.border.EtchedBorder
import kotlin.reflect.KMutableProperty0

/**
 * Tools to control individual demo graphic attributes.  Also, control for
 * start & stop on animated demos; control for cloning the demo; control for
 * printing the demo.  Expand and collapse the Tools panel with ToggleIcon.
 */
class Tools(private val java2Demo: Java2Demo?,
            private val surface: Surface
) : JPanel(BorderLayout()), ActionListener, Runnable
{
    private var thread: Thread? = null
    private val toolbarPanel: JPanel
    private val bumpyIcon = ToggleIcon(this, Color.LIGHT_GRAY)
    private val rolloverIcon = ToggleIcon(this, ROLLOVER_COLOR)
    private var focus: Boolean = false
    val toggleButton: JToggleButton
    val printButton: AbstractButton
    val screenCombo: JComboBox<String>
    val renderButton: AbstractButton
    val antialiasButton: AbstractButton
    val textureButton: AbstractButton
    val compositeButton: AbstractButton
    val startStopButton: AbstractButton?
    var cloneButton: AbstractButton? = null
    var issueRepaint = true
    val toolbar: JToolBar
    val slider: JSlider?
    var isExpanded: Boolean = false

    private var texture: Boolean
        get() = surface.texture != null
        set(value) {
            if (value) {
                surface.texture = TextureChooser.texture
            } else {
                surface.texture = null
                surface.clearSurface = true
            }
        }

    init {
        toggleButton = JToggleButton(bumpyIcon).apply {
            addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent?) {
                    focus = true
                    bumpyIcon.start()
                }
                override fun mouseExited(e: MouseEvent?) {
                    focus = false
                    bumpyIcon.stop()
                }
            })
            addActionListener {
                isExpanded = !isExpanded
                this@Tools.preferredSize = if (isExpanded) Dimension(200, 38) else Dimension(200, 6)
                val center = (this@Tools.layout as BorderLayout).getLayoutComponent(BorderLayout.CENTER)
                center.isVisible = isExpanded
                this@Tools.parent.validate()
                model.isRollover = false
            }
            margin = Insets(0, 0, -4, 0)
            isBorderPainted = false
            isFocusPainted = false
            isContentAreaFilled = false
            rolloverIcon = this@Tools.rolloverIcon
        }
        add(toggleButton, BorderLayout.NORTH)

        toolbar = JToolBar().apply {
            preferredSize = Dimension(112, 26)
            isFloatable = false
        }

        antialiasButton = addToggleTool(surface::isAntialiasing, "A",
            selectedToolTip = "Antialiasing On", unselectedToolTip = "Antialiasing Off")
        renderButton = addToggleTool(surface::isRenderingQuality, "R",
            selectedToolTip = "Rendering Quality", unselectedToolTip = "Rendering Speed")
        textureButton = addToggleTool(::texture, "T",
            selectedToolTip = "Texture On", unselectedToolTip = "Texture Off")
        compositeButton = addToggleTool(surface::isComposite, "C",
            selectedToolTip = "Composite On", unselectedToolTip = "Composite Off")

        val printBImg = DemoImages.getImage("print.gif", this)
        printButton = addTool(printBImg, "Print the Surface", this)

        startStopButton = if (surface is AnimatingSurface) {
            val stopImage = DemoImages.getImage("stop.gif", this)
            val stopIcon = ImageIcon(stopImage)
            val startIcon = ImageIcon(DemoImages.getImage("start.gif", this))
            toolbar.preferredSize = Dimension(132, 26)
            addToggleTool(stopImage, "Stop amination") { button ->
                if (button.isSelected) {
                    button.icon = stopIcon
                    button.toolTipText = "Stop Animation"
                    surface.animating?.start()
                } else {
                    button.icon = startIcon
                    button.toolTipText = "Start Animation"
                    surface.animating?.stop()
                }
            }
        } else {
            null
        }

        screenCombo = JComboBox<String>().apply {
            preferredSize = Dimension(100, 18)
            for (name in GlobalControls.SCREEN_NAMES) {
                addItem(name)
            }
            addActionListener {
                surface.imageType = selectedIndex
            }
        }
        toolbarPanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0)).apply {
            add(toolbar)
            add(screenCombo)
            border = EtchedBorder()
        }

        preferredSize = Dimension(200, 8)

        val center = if (surface is AnimatingSurface) {
            fun formatSliderText(value: Int) = " Sleep = %d ms".format(value)
            val sliderLabel = JLabel(formatSliderText(INITIAL_SLEEP))
            slider = JSlider(SwingConstants.HORIZONTAL, 0, 200, INITIAL_SLEEP).apply {
                addChangeListener {
                    sliderLabel.text = formatSliderText(value)
                    sliderLabel.repaint()
                    surface.sleepAmount = value.toLong()
                }
            }
            val sliderPanel = JPanel(BorderLayout()).apply {
                border = EtchedBorder()
                add(sliderLabel, BorderLayout.WEST)
                add(slider, BorderLayout.CENTER)
            }
            val cardLayout = CardLayout()
            val cardPanel = JPanel(cardLayout)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    cardLayout.next(cardPanel)
                }
            })
            cardPanel.add(toolbarPanel)
            cardPanel.add(sliderPanel)
            cardPanel
        } else {
            slider = null
            toolbarPanel
        }
        center.isVisible = false
        add(center, BorderLayout.CENTER)
    }

    fun addTool(
        img: Image,
        toolTip: String,
        al: ActionListener
    ): JButton {
        val button = object : JButton(ImageIcon(img)) {
            override fun getPreferredSize(): Dimension = TOOL_BUTTON_SIZE
            override fun getMaximumSize(): Dimension = TOOL_BUTTON_SIZE
            override fun getMinimumSize(): Dimension = TOOL_BUTTON_SIZE
        }.apply {
            isFocusPainted = false
            isSelected = true
            toolTipText = toolTip
            addActionListener(al)
        }
        toolbar.add(button)
        return button
    }

    private fun addToggleTool(
        image: Image,
        toolTip: String,
        action: (AbstractButton) -> Unit
    ): AbstractButton {
        val button = JToggleButton(ImageIcon(image)).apply {
            preferredSize = TOOL_BUTTON_SIZE
            maximumSize = TOOL_BUTTON_SIZE
            minimumSize = TOOL_BUTTON_SIZE
            isFocusPainted = false
            isSelected = true
            toolTipText = toolTip
            addActionListener {
                action(this)
            }
        }
        toolbar.add(button)
        return button
    }

    private fun addToggleTool(
        property: KMutableProperty0<Boolean>,
        name: String,
        selectedToolTip: String,
        unselectedToolTip: String
    ): AbstractButton {
        val button = JToggleButton(name).apply {
            isFocusPainted = false
            isSelected = property.get()
            toolTipText = if (isSelected) selectedToolTip else unselectedToolTip
            addActionListener {
                toolTipText = if (isSelected) selectedToolTip else unselectedToolTip
                property.set(isSelected)
                checkRepaint()
            }
        }
        toolbar.add(button)
        return button
    }

    override fun actionPerformed(e: ActionEvent) {
        val obj = e.source
        if (obj == printButton) {
            start()
            return
        }
        checkRepaint()
    }

    private fun checkRepaint() {
        if (issueRepaint) {
            if (surface is AnimatingSurface) {
                if (surface.sleepAmount != 0L && surface.isRunning) {
                    surface.doRepaint()
                }
            } else {
                surface.repaint()
            }
        }
    }

    fun start() {
        thread = Thread(this, "Printing ${surface.name}").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    @Synchronized
    fun stop() {
        thread = null
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as java.lang.Object).notifyAll()
    }

    override fun run() {
        var stopped = false
        val animating = surface.animating
        if (animating != null && animating.isRunning) {
            stopped = true
            startStopButton!!.doClick()
        }

        try {
            val printJob = PrinterJob.getPrinterJob()
            printJob.setPrintable(surface)
            var pDialogState = true
            val aset = HashPrintRequestAttributeSet()

            if (java2Demo?.isDefaultPrinter != true) {
                pDialogState = printJob.printDialog(aset)
            }
            if (pDialogState) {
                printJob.print(aset)
            }
        } catch (ace: AccessControlException) {
            val errmsg = ("Applet access control exception; to allow "
                    + "access to printer, run policytool and set\n"
                    + "permission for \"queuePrintJob\" in "
                    + "RuntimePermission.")
            JOptionPane.showMessageDialog(
                this, errmsg, "Printer Access Error",
                JOptionPane.ERROR_MESSAGE)
        } catch (ex: Exception) {
            getLogger<Tools>().log(Level.SEVERE, null, ex)
        }

        if (stopped) {
            startStopButton!!.doClick()
        }
        thread = null
    }

    /**
     * Expand and Collapse the Tools Panel with this bumpy button.
     */
    class ToggleIcon(private val tools: Tools, private val fillColor: Color) : Icon
    {
        companion object {
            private val SHADOW_COLOR = Color(102, 102, 153)
        }
        private var timer: Timer? = null

        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            val w = iconWidth
            val h = iconHeight
            g.color = fillColor
            g.fillRect(0, 0, w, h)
            for (cx in x until w - 2 step 4) {
                g.color = Color.WHITE
                g.fillRect(cx,     1, 1, 1)
                g.fillRect(cx + 2, 3, 1, 1)
                g.color = SHADOW_COLOR
                g.fillRect(cx + 1, 2, 1, 1)
                g.fillRect(cx + 3, 4, 1, 1)
            }
        }

        override fun getIconWidth(): Int = tools.size.width

        override fun getIconHeight(): Int = 6

        fun start() {
            if (timer == null) {
                timer = Timer(400) {
                    if (tools.focus) {
                        tools.toggleButton.doClick()
                        timer = null
                    }
                }.apply {
                    isRepeats = false
                    start()
                }
            }
        }

        fun stop() {
            timer?.run {
                stop()
                timer = null
            }
        }
    }

    companion object
    {
        private val TOOL_BUTTON_SIZE = Dimension(21, 22)
        private const val INITIAL_SLEEP = 30
        private val ROLLOVER_COLOR = Color(187, 213, 238)
    }
}
