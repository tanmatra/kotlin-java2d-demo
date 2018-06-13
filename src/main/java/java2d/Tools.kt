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
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Image
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.print.PrinterJob
import java.security.AccessControlException
import java.text.DecimalFormat
import java.util.logging.Level
import javax.print.attribute.HashPrintRequestAttributeSet
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

/**
 * Tools to control individual demo graphic attributes.  Also, control for
 * start & stop on animated demos; control for cloning the demo; control for
 * printing the demo.  Expand and collapse the Tools panel with ToggleIcon.
 */
class Tools(private val surface: Surface) : JPanel(BorderLayout()), ActionListener, Runnable
{
    private val stopIcon: ImageIcon
    private val startIcon: ImageIcon
    private val roColor = Color(187, 213, 238)
    private var thread: Thread? = null
    private val toolbarPanel: JPanel
    private var sliderPanel: JPanel? = null
    private val bumpyIcon: ToggleIcon
    private val rolloverIcon: ToggleIcon
    private val decimalFormat = DecimalFormat("000")
    private var focus: Boolean = false
    var toggleButton: JToggleButton
    var printButton: JButton
    var screenCombo: JComboBox<String>
    var renderButton: JToggleButton
    var antialiasButton: JToggleButton
    var textureButton: JToggleButton
    var compositeB: JToggleButton
    var startStopButton: JButton? = null
    var cloneButton: JButton? = null
    var issueRepaint = true
    var toolbar: JToolBar
    var slider: JSlider? = null
    var doSlider: Boolean = false
    var isExpanded: Boolean = false

    init {
        stopIcon = ImageIcon(DemoImages.getImage("stop.gif", this))
        startIcon = ImageIcon(DemoImages.getImage("start.gif", this))
        bumpyIcon = ToggleIcon(this, Color.LIGHT_GRAY)
        rolloverIcon = ToggleIcon(this, roColor)
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
            addActionListener(this@Tools)
            margin = Insets(0, 0, -4, 0)
            isBorderPainted = false
            isFocusPainted = false
            isContentAreaFilled = false
            rolloverIcon = this@Tools.rolloverIcon
        }
        add(toggleButton, BorderLayout.NORTH)
        isExpanded = false

        toolbar = JToolBar().apply {
            preferredSize = Dimension(112, 26)
            isFloatable = false
        }

        var s = if (surface.antiAlias === RenderingHints.VALUE_ANTIALIAS_ON) "On" else "Off"
        antialiasButton = addTool("A", "Antialiasing $s", this)

        s = if (surface.rendering === RenderingHints.VALUE_RENDER_SPEED) "Speed" else "Quality"
        renderButton = addTool("R", "Rendering $s", this)

        s = if (surface.texture != null) "On" else "Off"
        textureButton = addTool("T", "Texture $s", this)

        s = if (surface.composite != null) "On" else "Off"
        compositeB = addTool("C", "Composite $s", this)

        val printBImg = DemoImages.getImage("print.gif", this)
        printButton = addTool(printBImg, "Print the Surface", this)

        if (surface is AnimatingSurface) {
            val stopImg = DemoImages.getImage("stop.gif", this)
            startStopButton = addTool(stopImg, "Stop Animation", this)
            toolbar.preferredSize = Dimension(132, 26)
        }

        screenCombo = JComboBox<String>().apply {
            preferredSize = Dimension(100, 18)
            for (name in GlobalControls.SCREEN_NAMES) {
                addItem(name)
            }
            addActionListener(this@Tools)
        }
        toolbarPanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0)).apply {
            setLocation(0, 6)
            isVisible = false
            add(toolbar)
            add(screenCombo)
            border = EtchedBorder()
        }
        add(toolbarPanel)

        preferredSize = Dimension(200, 8)

        if (surface is AnimatingSurface) {
            val sliderLabel = JLabel(" Sleep = 030 ms").apply {
                foreground = BLACK
            }
            slider = JSlider(SwingConstants.HORIZONTAL, 0, 200, 30).apply {
                addChangeListener {
                    sliderLabel.text = " Sleep = ${decimalFormat.format(value)} ms"
                    sliderLabel.repaint()
                    surface.sleepAmount = value.toLong()
                }
            }
            sliderPanel = JPanel(BorderLayout()).apply {
                border = EtchedBorder()
                add(sliderLabel, BorderLayout.WEST)
                add(slider)
            }
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (toolbarPanel.isVisible) {
                        invalidate()
                        doSlider = !doSlider
                        if (doSlider) {
                            remove(toolbarPanel)
                            add(sliderPanel)
                        } else {
                            remove(sliderPanel)
                            add(toolbarPanel)
                        }
                        validate()
                        repaint()
                    }
                }
            })
        }
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

    private fun addTool(
        name: String,
        toolTip: String,
        al: ActionListener
    ): JToggleButton {
        val button = object : JToggleButton(name) {
            override fun getPreferredSize(): Dimension = TOOL_BUTTON_SIZE
            override fun getMaximumSize(): Dimension = TOOL_BUTTON_SIZE
            override fun getMinimumSize(): Dimension = TOOL_BUTTON_SIZE
        }.apply {
            isFocusPainted = false
            isSelected =
                    toolTip == "Rendering Quality" ||
                    toolTip == "Antialiasing On" ||
                    toolTip == "Texture On" ||
                    toolTip == "Composite On"
            toolTipText = toolTip
            addActionListener(al)
        }
        toolbar.add(button)
        return button
    }

    override fun actionPerformed(e: ActionEvent) {
        val obj = e.source
        if (obj is JButton) {
            obj.isSelected = !obj.isSelected
            if (obj.icon == null) {
                obj.background = if (obj.isSelected) Color.GREEN else Color.LIGHT_GRAY
            }
        }
        if (obj == toggleButton) {
            isExpanded = !isExpanded
            preferredSize = if (isExpanded) {
                Dimension(200, 38)
            } else {
                Dimension(200, 6)
            }
            toolbarPanel.isVisible = isExpanded
            sliderPanel?.isVisible = isExpanded
            parent.validate()
            toggleButton.model.isRollover = false
            return
        }
        if (obj == printButton) {
            start()
            return
        }

        if (obj == startStopButton) {
            if (startStopButton!!.toolTipText == "Stop Animation") {
                startStopButton!!.icon = startIcon
                startStopButton!!.toolTipText = "Start Animation"
                surface.animating?.stop()
            } else {
                startStopButton!!.icon = stopIcon
                startStopButton!!.toolTipText = "Stop Animation"
                surface.animating?.start()
            }
        } else if (obj == antialiasButton) {
            if (antialiasButton.toolTipText == "Antialiasing On") {
                antialiasButton.toolTipText = "Antialiasing Off"
            } else {
                antialiasButton.toolTipText = "Antialiasing On"
            }
            surface.setAntiAlias(antialiasButton.isSelected)
        } else if (obj == renderButton) {
            if (renderButton.toolTipText == "Rendering Quality") {
                renderButton.toolTipText = "Rendering Speed"
            } else {
                renderButton.toolTipText = "Rendering Quality"
            }
            surface.setRendering(renderButton.isSelected)
        } else if (obj == textureButton) {
            if (textureButton.toolTipText == "Texture On") {
                textureButton.toolTipText = "Texture Off"
                surface.setTexture(null)
                surface.clearSurface = true
            } else {
                textureButton.toolTipText = "Texture On"
                surface.setTexture(TextureChooser.texture)
            }
        } else if (obj == compositeB) {
            if (compositeB.toolTipText == "Composite On") {
                compositeB.toolTipText = "Composite Off"
            } else {
                compositeB.toolTipText = "Composite On"
            }
            surface.setComposite(compositeB.isSelected)
        } else if (obj == screenCombo) {
            surface.imageType = screenCombo.selectedIndex
        }

        if (issueRepaint) {
            val animating = surface.animating
            if (animating != null) {
                if (surface.sleepAmount != 0L) {
                    if (animating.isRunning) {
                        animating.doRepaint()
                    }
                }
            } else if (issueRepaint) {
                surface.repaint()
            }
        } else if (issueRepaint) {
            surface.repaint()
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

            if (!Java2Demo.printCB.isSelected) {
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
    }
}
