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
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.WHITE
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Graphics
import java.awt.Image
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.print.PrinterJob
import java.text.DecimalFormat
import java.util.logging.Level
import java.util.logging.Logger
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
import javax.swing.border.EtchedBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Tools to control individual demo graphic attributes.  Also, control for
 * start & stop on animated demos; control for cloning the demo; control for
 * printing the demo.  Expand and collapse the Tools panel with ToggleIcon.
 */
class Tools(private val surface: Surface) : JPanel(), ActionListener, ChangeListener, Runnable {

    private val stopIcon: ImageIcon
    private val startIcon: ImageIcon
    private val roColor = Color(187, 213, 238)
    private var thread: Thread? = null
    private val toolbarPanel: JPanel
    private var sliderPanel: JPanel? = null
    private var label: JLabel? = null
    private val bumpyIcon: ToggleIcon
    private val rolloverIcon: ToggleIcon
    private val decimalFormat = DecimalFormat("000")
    private var focus: Boolean = false
    var toggleB: JToggleButton
    var printB: JButton
    var screenCombo: JComboBox<String>
    var renderB: JToggleButton
    var aliasB: JToggleButton
    var textureB: JToggleButton
    var compositeB: JToggleButton
    var startStopB: JButton? = null
    var cloneB: JButton? = null
    var issueRepaint = true
    var toolbar: JToolBar
    var slider: JSlider? = null
    var doSlider: Boolean = false
    var isExpanded: Boolean = false

    init {
        layout = BorderLayout()

        stopIcon = ImageIcon(DemoImages.getImage("stop.gif", this))
        startIcon = ImageIcon(DemoImages.getImage("start.gif", this))
        bumpyIcon = ToggleIcon(this, LIGHT_GRAY)
        rolloverIcon = ToggleIcon(this, roColor)
        toggleB = JToggleButton(bumpyIcon)
        toggleB.addMouseListener(object : MouseAdapter() {

            override fun mouseEntered(e: MouseEvent?) {
                focus = true
                bumpyIcon.start()
            }

            override fun mouseExited(e: MouseEvent?) {
                focus = false
                bumpyIcon.stop()
            }
        })
        isExpanded = false
        toggleB.addActionListener(this)
        toggleB.margin = Insets(0, 0, -4, 0)
        toggleB.isBorderPainted = false
        toggleB.isFocusPainted = false
        toggleB.isContentAreaFilled = false
        toggleB.rolloverIcon = rolloverIcon
        add("North", toggleB)

        toolbar = JToolBar()
        toolbar.preferredSize = Dimension(112, 26)
        toolbar.isFloatable = false

        var s = if (surface.AntiAlias === RenderingHints.VALUE_ANTIALIAS_ON)
            "On"
        else
            "Off"
        aliasB = addTool("A", "Antialiasing $s", this)

        s = if (surface.Rendering === RenderingHints.VALUE_RENDER_SPEED)
            "Speed"
        else
            "Quality"
        renderB = addTool("R", "Rendering $s", this)

        s = if (surface.texture != null) "On" else "Off"
        textureB = addTool("T", "Texture $s", this)

        s = if (surface.composite != null) "On" else "Off"
        compositeB = addTool("C", "Composite $s", this)

        val printBImg = DemoImages.getImage("print.gif", this)
        printB = addTool(printBImg, "Print the Surface", this)

        if (surface is AnimatingSurface) {
            val stopImg = DemoImages.getImage("stop.gif", this)
            startStopB = addTool(stopImg, "Stop Animation", this)
            toolbar.preferredSize = Dimension(132, 26)
        }

        screenCombo = JComboBox()
        screenCombo.preferredSize = Dimension(100, 18)
        screenCombo.font = FONT
        for (name in GlobalControls.screenNames) {
            screenCombo.addItem(name)
        }
        screenCombo.addActionListener(this)
        toolbarPanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0))
        toolbarPanel.setLocation(0, 6)
        toolbarPanel.isVisible = false
        toolbarPanel.add(toolbar)
        toolbarPanel.add(screenCombo)
        toolbarPanel.border = EtchedBorder()
        add(toolbarPanel)

        preferredSize = Dimension(200, 8)

        if (surface is AnimatingSurface) {
            sliderPanel = JPanel(BorderLayout())
            label = JLabel(" Sleep = 030 ms")
            label!!.foreground = BLACK
            sliderPanel!!.add(label!!, BorderLayout.WEST)
            slider = JSlider(SwingConstants.HORIZONTAL, 0, 200, 30).apply {
                addChangeListener(this@Tools)
            }
            sliderPanel!!.border = EtchedBorder()
            sliderPanel!!.add(slider)

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (toolbarPanel.isVisible) {
                        invalidate()
                        doSlider = !doSlider
                        if (doSlider) {
                            remove(toolbarPanel)
                            add(sliderPanel)
                        } else {
                            remove(sliderPanel!!)
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
        val b = object : JButton(ImageIcon(img)) {

            internal var prefSize = Dimension(21, 22)

            override fun getPreferredSize(): Dimension {
                return prefSize
            }

            override fun getMaximumSize(): Dimension {
                return prefSize
            }

            override fun getMinimumSize(): Dimension {
                return prefSize
            }
        }
        toolbar.add(b)
        b.isFocusPainted = false
        b.isSelected = true
        b.toolTipText = toolTip
        b.addActionListener(al)
        return b
    }

    private fun addTool(
        name: String,
        toolTip: String,
        al: ActionListener
    ): JToggleButton {
        val b = object : JToggleButton(name) {

            internal var prefSize = Dimension(21, 22)

            override fun getPreferredSize(): Dimension {
                return prefSize
            }

            override fun getMaximumSize(): Dimension {
                return prefSize
            }

            override fun getMinimumSize(): Dimension {
                return prefSize
            }
        }
        toolbar.add(b)
        b.isFocusPainted = false
        if (toolTip == "Rendering Quality" || toolTip == "Antialiasing On" || toolTip == "Texture On" || toolTip == "Composite On") {
            b.isSelected = true
        } else {
            b.isSelected = false
        }
        b.toolTipText = toolTip
        b.addActionListener(al)
        return b
    }

    override fun actionPerformed(e: ActionEvent) {
        val obj = e.source
        if (obj is JButton) {
            val b = obj
            b.isSelected = !b.isSelected
            if (b.icon == null) {
                b.background = if (b.isSelected) GREEN else LIGHT_GRAY
            }
        }
        if (obj == toggleB) {
            isExpanded = !isExpanded
            preferredSize = if (isExpanded) {
                Dimension(200, 38)
            } else {
                Dimension(200, 6)
            }
            toolbarPanel.isVisible = isExpanded
            if (sliderPanel != null) {
                sliderPanel!!.isVisible = isExpanded
            }
            parent.validate()
            toggleB.model.isRollover = false
            return
        }
        if (obj == printB) {
            start()
            return
        }

        if (obj == startStopB) {
            if (startStopB!!.toolTipText == "Stop Animation") {
                startStopB!!.icon = startIcon
                startStopB!!.toolTipText = "Start Animation"
                surface.animating.stop()
            } else {
                startStopB!!.icon = stopIcon
                startStopB!!.toolTipText = "Stop Animation"
                surface.animating.start()
            }
        } else if (obj == aliasB) {
            if (aliasB.toolTipText == "Antialiasing On") {
                aliasB.toolTipText = "Antialiasing Off"
            } else {
                aliasB.toolTipText = "Antialiasing On"
            }
            surface.setAntiAlias(aliasB.isSelected)
        } else if (obj == renderB) {
            if (renderB.toolTipText == "Rendering Quality") {
                renderB.toolTipText = "Rendering Speed"
            } else {
                renderB.toolTipText = "Rendering Quality"
            }
            surface.setRendering(renderB.isSelected)
        } else if (obj == textureB) {
            if (textureB.toolTipText == "Texture On") {
                textureB.toolTipText = "Texture Off"
                surface.setTexture(null)
                surface.clearSurface = true
            } else {
                textureB.toolTipText = "Texture On"
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
            surface.setImageType(screenCombo.selectedIndex)
        }

        if (issueRepaint && surface.animating != null) {
            if (surface.getSleepAmount() != 0L) {
                if (surface.animating.running()) {
                    surface.animating.doRepaint()
                }
            }
        } else if (issueRepaint) {
            surface.repaint()
        }
    }

    override fun stateChanged(e: ChangeEvent) {
        val value = slider!!.value
        label!!.text = " Sleep = " + decimalFormat.format(value.toLong()) + " ms"
        label!!.repaint()
        surface.setSleepAmount(value.toLong())
    }

    fun start() {
        thread = Thread(this)
        thread!!.priority = Thread.MAX_PRIORITY
        thread!!.name = "Printing " + surface.name
        thread!!.start()
    }

    @Synchronized
    fun stop() {
        thread = null
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as java.lang.Object).notifyAll()
    }

    override fun run() {
        var stopped = false
        if (surface.animating != null && surface.animating.running()) {
            stopped = true
            startStopB!!.doClick()
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
        } catch (ace: java.security.AccessControlException) {
            val errmsg = ("Applet access control exception; to allow "
                    + "access to printer, run policytool and set\n"
                    + "permission for \"queuePrintJob\" in "
                    + "RuntimePermission.")
            JOptionPane.showMessageDialog(
                this, errmsg, "Printer Access Error",
                JOptionPane.ERROR_MESSAGE
                                         )
        } catch (ex: Exception) {
            Logger.getLogger(Tools::class.java.name).log(Level.SEVERE, null, ex)
        }

        if (stopped) {
            startStopB!!.doClick()
        }
        thread = null
    }

    /**
     * Expand and Collapse the Tools Panel with this bumpy button.
     */
    internal class ToggleIcon(private val tools: Tools, private val fillColor: Color) : Icon, Runnable
    {
        private val shadowColor = Color(102, 102, 153)
        private var thread: Thread? = null

        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            var cx = x
            val w = iconWidth
            val h = iconHeight
            g.color = fillColor
            g.fillRect(0, 0, w, h)
            while (cx < w - 2) {
                g.color = WHITE
                g.fillRect(cx, 1, 1, 1)
                g.fillRect(cx + 2, 3, 1, 1)
                g.color = shadowColor
                g.fillRect(cx + 1, 2, 1, 1)
                g.fillRect(cx + 3, 4, 1, 1)
                cx += 4
            }
        }

        override fun getIconWidth(): Int {
            return tools.size.width
        }

        override fun getIconHeight(): Int {
            return 6
        }

        fun start() {
            thread = Thread(this)
            thread!!.priority = Thread.MIN_PRIORITY
            thread!!.name = "ToggleIcon"
            thread!!.start()
        }

        @Synchronized
        fun stop() {
            if (thread != null) {
                thread!!.interrupt()
            }
            thread = null
        }

        override fun run() {
            try {
                Thread.sleep(400)
            } catch (e: InterruptedException) {
            }

            if (tools.focus && thread != null) {
                tools.toggleB.doClick()
            }
            thread = null
        }
    }

    companion object
    {
        private val FONT = Font(Font.SERIF, Font.PLAIN, 10)
    }
}
