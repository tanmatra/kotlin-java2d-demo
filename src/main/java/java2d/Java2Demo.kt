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

import java2d.intro.Intro
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JCheckBoxMenuItem
import javax.swing.JColorChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.JSeparator
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.EtchedBorder
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

/**
 * A demo that shows Java 2D(TM) features.
 *
 * @author Brian Lichtenwalter  (Framework, Intro, demos)
 * @author Jim Graham           (demos)
 * @author Alexander Kouznetsov (code beautification)
 */
class Java2Demo(
    private val progressLabel: JLabel,
    private val progressBar: JProgressBar,
    private val applet: Boolean = false
) : JPanel(BorderLayout())
{
    // private JMenuItem ccthreadMI, verboseMI;
    private var runWindow: RunWindow? = null
    private var runFrame: JFrame? = null
    private var cloningFrame: JFrame? = null

    private val verboseCheckBox = JCheckBoxMenuItem("Verbose")
    var isVerbose: Boolean by verboseCheckBox.selectedProperty()

    private val defaultPrinterCheckBox = JCheckBoxMenuItem("Default Printer")
    var isDefaultPrinter: Boolean by defaultPrinterCheckBox.selectedProperty()

    val memoryMonitor = MemoryMonitor()
    val performanceMonitor = PerformanceMonitor()
    val globalControls = GlobalControls(this)
    lateinit var memoryMonitorCheckBox: JCheckBoxMenuItem
    lateinit var performanceMontiorCheckBox: JCheckBoxMenuItem

    var backgroundColor: Color? = null

    private val tabbedPane: JTabbedPane
    var tabbedPaneIndex: Int
        get() = tabbedPane.selectedIndex
        set(value) { tabbedPane.selectedIndex = value }
    val tabbedPaneCount: Int
        get() = tabbedPane.tabCount

    val groups: List<DemoGroup>

    internal val intro: Intro

    /**
     * Construct the Java2D Demo.
     */
    init {
        border = EtchedBorder()

        add(createMenuBar(), BorderLayout.NORTH)

        // hard coding 14 = 11 demo dirs + images + fonts + Intro
        progressBar.maximum = 13
        progressLabel.text = "Loading images"
        DemoImages.preloadImages(this)
        progressBar.value = progressBar.value + 1
        progressLabel.text = "Loading fonts"
        DemoFonts.preloadFonts()
        progressBar.value = progressBar.value + 1
        progressLabel.text = "Loading Intro"
        intro = Intro()
        progressBar.value = progressBar.value + 1
        UIManager.put("Button.margin", Insets(0, 0, 0, 0))

        val globalPanel = GlobalPanel(this)

        tabbedPane = JTabbedPane().apply {
            font = Font("serif", Font.PLAIN, 12)
            addTab("", J2DIcon(), globalPanel)
            addChangeListener {
                globalPanel.onDemoTabChanged(tabbedPaneIndex)
            }
        }

        groups = GroupInfo.LIST.map { groupInfo ->
            val groupName = groupInfo.groupName
            progressLabel.text = "Loading demos.$groupName"
            val demoGroup = DemoGroup(this, groupInfo)
            tabbedPane.addTab(groupName, null)
            progressBar.value = progressBar.value + 1
            demoGroup
        }

        add(tabbedPane, BorderLayout.CENTER)
    }

    private fun createMenuBar(): JMenuBar {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false)
        val menuBar = JMenuBar()

        if (!applet) {
            val file = menuBar.add(JMenu("File"))
            file.add(JMenuItem("Exit").apply {
                addActionListener {
                    System.exit(0)
                }
            })
        }

        val optionsMenu = menuBar.add(JMenu("Options"))

        optionsMenu.add(JCheckBoxMenuItem("Global Controls", true).apply {
            addItemListener {
                val newVisibility = !globalControls.isVisible
                globalControls.isVisible = newVisibility
                for (cmp in globalControls.textureChooser.components) {
                    cmp.isVisible = newVisibility
                }
            }
        }) as JCheckBoxMenuItem

        memoryMonitorCheckBox = optionsMenu.add(JCheckBoxMenuItem("Memory Monitor", true).apply {
            addItemListener {
                val visible = !memoryMonitor.isVisible
                memoryMonitor.isVisible = visible
                memoryMonitor.surface.isVisible = visible
                if (visible) memoryMonitor.surface.start() else memoryMonitor.surface.stop()
            }
        }) as JCheckBoxMenuItem

        performanceMontiorCheckBox = optionsMenu.add(JCheckBoxMenuItem("Performance Monitor", true).apply {
            addItemListener {
                performanceMonitor.run {
                    if (isVisible) {
                        isVisible = false
                        surface.isVisible = false
                        stop()
                    } else {
                        isVisible = true
                        surface.isVisible = true
                        start()
                    }
                }
            }
        }) as JCheckBoxMenuItem

        optionsMenu.add(JSeparator())

        ccthreadCB = optionsMenu.add(JCheckBoxMenuItem("Custom Controls Thread").apply {
            addItemListener {
                val state = if (ccthreadCB.isSelected) CustomControlsContext.State.START
                    else CustomControlsContext.State.STOP
                if (tabbedPaneIndex != 0) {
                    val p = groups[tabbedPaneIndex - 1].panel
                    for (i in 0 until p.componentCount) {
                        val dp = p.getComponent(i) as DemoPanel
                        dp.customControlsContext?.handleThread(state)
                    }
                }
            }
        }) as JCheckBoxMenuItem

        optionsMenu.add(defaultPrinterCheckBox)
        optionsMenu.add(verboseCheckBox)

        optionsMenu.add(JSeparator())

        optionsMenu.add(JMenuItem("Background Color").apply {
            addActionListener {
                selectBackgroundColor()
            }
        })
        optionsMenu.add(JMenuItem("Run Window").apply {
            addActionListener {
                createRunWindow()
            }
        })
        optionsMenu.add(JMenuItem("Cloning Feature").apply {
            addActionListener {
                createCloningFeature()
            }
        })

        return menuBar
    }

    fun createRunWindow() {
        runFrame?.toFront() ?: run {
            val runWindow = RunWindow(this).also { runWindow = it }
            runFrame = JFrame("Run").apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        runWindow.stop()
                        this@Java2Demo.runWindow = null
                        dispose()
                    }
                    override fun windowClosed(e: WindowEvent?) {
                        runFrame = null
                    }
                })
                contentPane.add(runWindow, BorderLayout.CENTER)
                pack()
                size = if (!applet) Dimension(200, 125) else Dimension(200, 150)
                isVisible = true
            }
        }
    }

    fun startRunWindow() {
        runWindow?.let { EventQueue.invokeLater { it.doRunAction() } }
    }

    private fun createCloningFeature() {
        cloningFrame?.toFront() ?: run {
            val cloningFeature = CloningFeature(this)
            cloningFrame = JFrame("Cloning Demo").apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        cloningFeature.stop()
                        dispose()
                    }
                    override fun windowClosed(e: WindowEvent?) {
                        cloningFrame = null
                    }
                })
                contentPane.add(cloningFeature, BorderLayout.CENTER)
                pack()
                size = Dimension(320, 330)
                isVisible = true
            }
        }
    }

    private fun selectBackgroundColor() {
        backgroundColor = JColorChooser.showDialog(this, "Background Color", Color.WHITE)
        for (i in 1 until tabbedPaneCount) {
            val p = groups[i - 1].panel
            for (j in 0 until p.componentCount) {
                val dp = p.getComponent(j) as DemoPanel
                if (dp.surface != null) {
                    dp.surface.background = backgroundColor
                }
            }
        }
    }

    fun start() {
        if (tabbedPaneIndex == 0) {
            intro.start()
        } else {
            groups[tabbedPaneIndex - 1].setup(false)
            if (memoryMonitor.surface.thread == null && memoryMonitorCheckBox.isSelected) {
                memoryMonitor.surface.start()
            }
            performanceMonitor.run {
                if (!isRunning && performanceMontiorCheckBox.isSelected) {
                    start()
                }
            }
        }
    }

    fun stop() {
        if (tabbedPane.selectedIndex == 0) {
            intro.stop()
        } else {
            memoryMonitor.surface.stop()
            performanceMonitor.stop()
            val i = tabbedPane.selectedIndex - 1
            groups[i].shutDown(groups[i].panel)
        }
    }

    fun setupSelectedGroup(issueRepaint: Boolean, eventSource: KProperty<*>?) {
        groups.getOrNull(tabbedPaneIndex - 1)?.setup(issueRepaint, eventSource)
    }

    /**
     * The Icon for the Intro tab.
     */
    internal class J2DIcon : Icon
    {
        private val fontRenderContext =
            FontRenderContext(null, systemTextAntialiasing, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)
        private val textLayout = TextLayout("Java2D", FONT, fontRenderContext)
        private val iconWidth = textLayout.bounds.width.roundToInt()
        private val iconHeight = (textLayout.ascent + textLayout.descent).roundToInt()

        override fun paintIcon(component: Component, g: Graphics, x: Int, y: Int) {
            val g2 = g as Graphics2D
            g2.textAntialiasing = systemTextAntialiasing
            g2.font = FONT
            val tabbedPane = component as JTabbedPane
            g2.color = if (tabbedPane.selectedIndex == 0) BLUE else BLACK
            textLayout.draw(g2, x.toFloat(), (y + textLayout.ascent))
        }

        override fun getIconWidth(): Int = iconWidth

        override fun getIconHeight(): Int = iconHeight

        companion object
        {
            private val BLUE = Color(94, 105, 176)
            private val BLACK = Color(20, 20, 20)
            private val FONT = Font(Font.SERIF, Font.BOLD, 12)
        }
    }

    companion object
    {
        lateinit var ccthreadCB: JCheckBoxMenuItem

        private fun initFrame(args: Array<String>) {
            val frame = JFrame("Java 2D(TM) Demo").apply {
                accessibleContext.accessibleDescription = "A sample application to demonstrate Java2D features"
                setSize(400, 200)
                setLocationRelativeTo(null)
                cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        System.exit(0)
                    }
                })
                JOptionPane.setRootFrame(this)
            }

            val progressPanel = object : JPanel() {
                override fun getInsets() = Insets(40, 30, 20, 30)
            }.apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
            }
            frame.contentPane.add(progressPanel, BorderLayout.CENTER)

            val progressLabel = JLabel("Loading, please wait...").apply {
                alignmentX = Component.CENTER_ALIGNMENT
                val labelSize = Dimension(400, 20)
                maximumSize = labelSize
                preferredSize = labelSize
            }
            progressPanel.add(progressLabel)
            progressPanel.add(Box.createRigidArea(Dimension(1, 20)))

            val progressBar = JProgressBar().apply {
                isStringPainted = true
                alignmentX = Component.CENTER_ALIGNMENT
                minimum = 0
                value = 0
                accessibleContext.accessibleName = "Java2D loading progress"
            }
            progressPanel.add(progressBar)
            progressLabel.labelFor = progressBar

            frame.isVisible = true

            val java2Demo = Java2Demo(progressLabel, progressBar)

            frame.run {
                addWindowListener(object : WindowAdapter() {
                    override fun windowDeiconified(e: WindowEvent?) {
                        java2Demo.start()
                    }
                    override fun windowIconified(e: WindowEvent?) {
                        java2Demo.stop()
                    }
                })
                contentPane.removeAll()
                contentPane.layout = BorderLayout()
                contentPane.add(java2Demo, BorderLayout.CENTER)
                setSize(730, 570)
                setLocationRelativeTo(null)
                cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
            }

            for (arg in args) {
                val value = arg.substringAfter('=', "")
                val globalControls = java2Demo.globalControls
                val globalOptions = globalControls.options
                when {
                    arg.startsWith("-runs=") -> {
                        RunWindow.numRuns = value.toInt()
                        RunWindow.exit = true
                        java2Demo.createRunWindow()
                    }
                    arg.startsWith("-screen=") -> globalControls.selectedScreenIndex = value.toInt()
                    arg.startsWith("-antialias=") -> globalOptions.antialiasing = value.toBoolean()
                    arg.startsWith("-rendering=") -> globalOptions.renderQuality = value.toBoolean()
                    arg.startsWith("-texture=") -> globalOptions.texture = value.toBoolean()
                    arg.startsWith("-composite=") -> globalOptions.composite = value.toBoolean()
                    arg.startsWith("-verbose") -> java2Demo.isVerbose = true
                    arg.startsWith("-print") -> {
                        java2Demo.isDefaultPrinter = true
                        RunWindow.printCheckBox.isSelected = true
                    }
                    arg.startsWith("-columns=") -> DemoGroup.columns = value.toInt()
                    arg.startsWith("-buffers=") -> {
                        // usage -buffers=3,10
                        RunWindow.buffersFlag = true
                        val (v1, v2) = value.split(',')
                        RunWindow.bufBeg = v1.toInt()
                        RunWindow.bufEnd = v2.toInt()
                    }
                    arg.startsWith("-ccthread") -> Java2Demo.ccthreadCB.isSelected = true
                    arg.startsWith("-zoom") -> RunWindow.zoomCheckBox.isSelected = true
                    arg.startsWith("-maxscreen") -> {
                        frame.setLocation(0, 0)
                        val screenSize = Toolkit.getDefaultToolkit().screenSize
                        frame.setSize(screenSize.width, screenSize.height)
                    }
                }
            }

            frame.validate()
            frame.repaint()
            frame.focusTraversalPolicy.getDefaultComponent(frame).requestFocus()
            java2Demo.start()

            if (RunWindow.exit) {
                java2Demo.startRunWindow()
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            for (i in args.indices) {
                if (args[i].startsWith("-h") || args[i].startsWith("-help")) {
                    printHelp()
                    System.exit(0)
                } else if (args[i].startsWith("-delay=")) {
                    val s = args[i].substring(args[i].indexOf('=') + 1)
                    RunWindow.delay = Integer.parseInt(s)
                }
            }

            SwingUtilities.invokeLater { initFrame(args) }
        }

        private fun printHelp() {
            println(
                "\njava -jar Java2Demo.jar -runs=5 -delay=5 -screen=5 "
                + "-antialias=true -rendering=true -texture=true "
                + "-composite=true -verbose -print -columns=3 "
                + "-buffers=5,10 -ccthread -zoom -maxscreen\n"
                + "    -runs=5       Number of runs to execute\n"
                + "    -delay=5      Sleep amount between tabs\n"
                + "    -antialias=   true or false for antialiasing\n"
                + "    -rendering=   true or false for quality or speed\n"
                + "    -texture=     true or false for texturing\n"
                + "    -composite=   true or false for compositing\n"
                + "    -verbose      output Surface graphic states \n"
                + "    -print        during run print the Surface, use the Default Printer\n"
                + "    -columns=3    # of columns to use in clone layout\n"
                + "    -screen=3     demos all use this screen type\n"
                + "    -buffers=5,10 during run - clone to see screens five through ten\n"
                + "    -ccthread     Invoke the Custom Controls Thread\n"
                + "    -zoom         mouseClick on surface for zoom in\n"
                + "    -maxscreen    take up the entire monitor screen\n"
                + "Examples:\n"
                + "    Print all of the demos:\n"
                + "        java -jar Java2Demo.jar -runs=1 -delay=60 -print \n"
                + "    Run zoomed in with custom control thread:\n"
                + "        java -jar Java2Demo.jar -runs=10 -zoom -ccthread\n")
        }
    }
}
