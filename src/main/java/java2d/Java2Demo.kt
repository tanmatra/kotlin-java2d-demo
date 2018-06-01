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

import java2d.CustomControlsContext.State.START
import java2d.CustomControlsContext.State.STOP
import java2d.DemoFonts.newDemoFonts
import java2d.DemoImages.newDemoImages
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
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

/**
 * A demo that shows Java 2D(TM) features.
 *
 * @author Brian Lichtenwalter  (Framework, Intro, demos)
 * @author Jim Graham           (demos)
 * @author Alexander Kouznetsov (code beautification)
 */
class Java2Demo : JPanel(), ItemListener, ActionListener {
    private var controlsCB: JCheckBoxMenuItem? = null
    private var runMI: JMenuItem? = null
    private var cloneMI: JMenuItem? = null
    private var fileMI: JMenuItem? = null
    private var backgMI: JMenuItem? = null
    // private JMenuItem ccthreadMI, verboseMI;
    private var runwindow: RunWindow? = null
    private var cloningfeature: CloningFeature? = null
    private var rf: JFrame? = null
    private var cf: JFrame? = null

    val memoryMonitor = MemoryMonitor()

    /**
     * Construct the Java2D Demo.
     */
    init {
        layout = BorderLayout()
        border = EtchedBorder()

        add(createMenuBar(), BorderLayout.NORTH)

        // hard coding 14 = 11 demo dirs + images + fonts + Intro
        progressBar.maximum = 13
        progressLabel.text = "Loading images"
        newDemoImages()
        progressBar.value = progressBar.value + 1
        progressLabel.text = "Loading fonts"
        newDemoFonts()
        progressBar.value = progressBar.value + 1
        progressLabel.text = "Loading Intro"
        intro = Intro()
        progressBar.value = progressBar.value + 1
        UIManager.put("Button.margin", Insets(0, 0, 0, 0))

        controls = GlobalControls()
        performancemonitor = PerformanceMonitor()

        val globalPanel = GlobalPanel(this)

        tabbedPane = JTabbedPane()
        tabbedPane.font = Font("serif", Font.PLAIN, 12)
        tabbedPane.addTab("", J2DIcon(), globalPanel)
        tabbedPane.addChangeListener {
            globalPanel.onDemoTabChanged(tabbedPane.selectedIndex)
        }

        groups = Array(demos.size) { i ->
            val groupName = demos[i][0]
            progressLabel.text = "Loading demos." + groupName
            val demoGroup = DemoGroup(groupName)
            tabbedPane.addTab(groupName, null)
            progressBar.value = progressBar.value + 1
            demoGroup
        }

        add(tabbedPane, BorderLayout.CENTER)
    }

    private fun createMenuBar(): JMenuBar {

        JPopupMenu.setDefaultLightWeightPopupEnabled(false)
        val menuBar = JMenuBar()

        if (Java2DemoApplet.applet == null) {
            val file = menuBar.add(JMenu("File"))
            fileMI = file.add(JMenuItem("Exit"))
            fileMI!!.addActionListener(this)
        }

        val options = menuBar.add(JMenu("Options"))

        controlsCB = options.add(
            JCheckBoxMenuItem("Global Controls", true)
                                ) as JCheckBoxMenuItem
        controlsCB!!.addItemListener(this)

        memoryCB = options.add(
            JCheckBoxMenuItem("Memory Monitor", true)
                              ) as JCheckBoxMenuItem
        memoryCB.addItemListener(this)

        perfCB = options.add(
            JCheckBoxMenuItem("Performance Monitor", true)
                            ) as JCheckBoxMenuItem
        perfCB.addItemListener(this)

        options.add(JSeparator())

        ccthreadCB = options.add(
            JCheckBoxMenuItem("Custom Controls Thread")
                                ) as JCheckBoxMenuItem
        ccthreadCB.addItemListener(this)

        printCB = options.add(printCB) as JCheckBoxMenuItem

        verboseCB = options.add(
            JCheckBoxMenuItem("Verbose")
                               ) as JCheckBoxMenuItem

        options.add(JSeparator())

        backgMI = options.add(JMenuItem("Background Color"))
        backgMI!!.addActionListener(this)

        runMI = options.add(JMenuItem("Run Window"))
        runMI!!.addActionListener(this)

        cloneMI = options.add(JMenuItem("Cloning Feature"))
        cloneMI!!.addActionListener(this)

        return menuBar
    }

    fun createRunWindow() {
        if (rf != null) {
            rf!!.toFront()
            return
        }
        runwindow = RunWindow()
        val l = object : WindowAdapter() {

            override fun windowClosing(e: WindowEvent?) {
                runwindow!!.stop()
                rf!!.dispose()
            }

            override fun windowClosed(e: WindowEvent?) {
                rf = null
            }
        }
        rf = JFrame("Run")
        rf!!.addWindowListener(l)
        rf!!.contentPane.add("Center", runwindow)
        rf!!.pack()
        if (Java2DemoApplet.applet == null) {
            rf!!.size = Dimension(200, 125)
        } else {
            rf!!.size = Dimension(200, 150)
        }
        rf!!.isVisible = true
    }

    fun startRunWindow() {
        SwingUtilities.invokeLater { runwindow!!.doRunAction() }
    }

    override fun actionPerformed(e: ActionEvent) {
        if (e.source == fileMI) {
            System.exit(0)
        } else if (e.source == runMI) {
            createRunWindow()
        } else if (e.source == cloneMI) {
            if (cloningfeature == null) {
                cloningfeature = CloningFeature()
                val l = object : WindowAdapter() {

                    override fun windowClosing(e: WindowEvent?) {
                        cloningfeature!!.stop()
                        cf!!.dispose()
                    }

                    override fun windowClosed(e: WindowEvent?) {
                        cloningfeature = null
                    }
                }
                cf = JFrame("Cloning Demo")
                cf!!.addWindowListener(l)
                cf!!.contentPane.add("Center", cloningfeature)
                cf!!.pack()
                cf!!.size = Dimension(320, 330)
                cf!!.isVisible = true
            } else {
                cf!!.toFront()
            }
        } else if (e.source == backgMI) {
            backgroundColor = JColorChooser.showDialog(this, "Background Color", Color.WHITE)
            for (i in 1 until tabbedPane.tabCount) {
                val p = groups[i - 1].panel
                for (j in 0 until p.componentCount) {
                    val dp = p.getComponent(j) as DemoPanel
                    if (dp.surface != null) {
                        dp.surface.background = backgroundColor
                    }
                }
            }
        }
    }

    override fun itemStateChanged(e: ItemEvent) {
        if (e.source == controlsCB) {
            val newVisibility = !controls.isVisible
            controls.isVisible = newVisibility
            for (cmp in controls.textureChooser.components) {
                cmp.isVisible = newVisibility
            }
        } else if (e.source == memoryCB) {
            if (memoryMonitor.isVisible) {
                memoryMonitor.isVisible = false
                memoryMonitor.surf.isVisible = false
                memoryMonitor.surf.stop()
            } else {
                memoryMonitor.isVisible = true
                memoryMonitor.surf.isVisible = true
                memoryMonitor.surf.start()
            }
        } else if (e.source == perfCB) {
            performancemonitor?.run {
                if (isVisible) {
                    isVisible = false
                    surf.isVisible = false
                    surf.stop()
                } else {
                    isVisible = true
                    surf.isVisible = true
                    surf.start()
                }
            }
        } else if (e.source == ccthreadCB) {
            val state = if (ccthreadCB.isSelected) START else STOP
            if (tabbedPane.selectedIndex != 0) {
                val p = groups[tabbedPane.selectedIndex - 1].panel
                for (i in 0 until p.componentCount) {
                    val dp = p.getComponent(i) as DemoPanel
                    if (dp.ccc != null) {
                        dp.ccc.handleThread(state)
                    }
                }
            }
        }
        revalidate()
    }

    fun start() {
        if (tabbedPane.selectedIndex == 0) {
            intro.start()
        } else {
            groups[tabbedPane.selectedIndex - 1].setup(false)
            if (memoryMonitor.surf.thread == null && memoryCB.state) {
                memoryMonitor.surf.start()
            }
            performancemonitor?.run {
                if (surf.thread == null && perfCB.state) {
                    surf.start()
                }
            }
        }
    }

    fun stop() {
        if (tabbedPane.selectedIndex == 0) {
            intro.stop()
        } else {
            memoryMonitor.surf.stop()
            performancemonitor?.surf?.stop()
            val i = tabbedPane.selectedIndex - 1
            groups[i].shutDown(groups[i].panel)
        }
    }

    /**
     * The Icon for the Intro tab.
     */
    internal class J2DIcon : Icon
    {
        private val frc = FontRenderContext(null, true, true)
        private val tl = TextLayout("Java2D", FONT, frc)

        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.font = FONT
            if (tabbedPane.selectedIndex == 0) {
                g2.color = myBlue
            } else {
                g2.color = myBlack
            }
            tl.draw(g2, x.toFloat(), (y + 15).toFloat())
        }

        override fun getIconWidth(): Int = 40

        override fun getIconHeight(): Int = 22

        companion object
        {
            private val myBlue = Color(94, 105, 176)
            private val myBlack = Color(20, 20, 20)
            private val FONT = Font(Font.SERIF, Font.BOLD, 12)
        }
    }

    companion object
    {
        var demo: Java2Demo? = null
        lateinit var controls: GlobalControls
        var performancemonitor: PerformanceMonitor? = null
        lateinit var tabbedPane: JTabbedPane
        lateinit var progressLabel: JLabel
        lateinit var progressBar: JProgressBar
        lateinit var groups: Array<DemoGroup>
        lateinit var verboseCB: JCheckBoxMenuItem
        lateinit var ccthreadCB: JCheckBoxMenuItem
        var printCB = JCheckBoxMenuItem("Default Printer")
        var backgroundColor: Color? = null
        lateinit var memoryCB: JCheckBoxMenuItem
        lateinit var perfCB: JCheckBoxMenuItem
        lateinit var intro: Intro

        internal var demos = arrayOf(
            arrayOf("Arcs_Curves", "Arcs", "BezierAnim", "Curves", "Ellipses"),
            arrayOf("Clipping", "Areas", "ClipAnim", "Intersection", "Text"),
            arrayOf("Colors", "BullsEye", "ColorConvert", "Rotator3D"),
            arrayOf("Composite", "ACimages", "ACrules", "FadeAnim"),
            arrayOf("Fonts", "AttributedStr", "Highlighting", "Outline", "Tree"),
            arrayOf("Images", "DukeAnim", "ImageOps", "JPEGFlip", "WarpImage"),
            arrayOf("Lines", "Caps", "Dash", "Joins", "LineAnim"),
            arrayOf("Mix", "Balls", "BezierScroller", "Stars3D"),
            arrayOf("Paint", "GradAnim", "Gradient", "Texture", "TextureAnim"),
            arrayOf("Paths", "Append", "CurveQuadTo", "FillStroke", "WindingRule"),
            arrayOf("Transforms", "Rotate", "SelectTx", "TransformAnim"))

        private fun initFrame(args: Array<String>) {
            val frame = JFrame("Java 2D(TM) Demo")
            frame.accessibleContext.accessibleDescription = "A sample application to demonstrate Java2D features"
            var FRAME_WIDTH = 400
            var FRAME_HEIGHT = 200
            frame.setSize(FRAME_WIDTH, FRAME_HEIGHT)
            val d = Toolkit.getDefaultToolkit().screenSize
            frame.setLocation(d.width / 2 - FRAME_WIDTH / 2, d.height / 2 - FRAME_HEIGHT / 2)
            frame.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
            frame.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    System.exit(0)
                }
                override fun windowDeiconified(e: WindowEvent?) {
                    if (demo != null) {
                        demo!!.start()
                    }
                }
                override fun windowIconified(e: WindowEvent?) {
                    if (demo != null) {
                        demo!!.stop()
                    }
                }
            })
            JOptionPane.setRootFrame(frame)

            val progressPanel = object : JPanel() {
                override fun getInsets(): Insets {
                    return Insets(40, 30, 20, 30)
                }
            }
            progressPanel.layout = BoxLayout(progressPanel, BoxLayout.Y_AXIS)
            frame.contentPane.add(progressPanel, BorderLayout.CENTER)

            val labelSize = Dimension(400, 20)
            progressLabel = JLabel("Loading, please wait...")
            progressLabel.alignmentX = Component.CENTER_ALIGNMENT
            progressLabel.maximumSize = labelSize
            progressLabel.preferredSize = labelSize
            progressPanel.add(progressLabel)
            progressPanel.add(Box.createRigidArea(Dimension(1, 20)))

            progressBar = JProgressBar()
            progressBar.isStringPainted = true
            progressLabel.labelFor = progressBar
            progressBar.alignmentX = Component.CENTER_ALIGNMENT
            progressBar.minimum = 0
            progressBar.value = 0
            progressBar.accessibleContext.accessibleName = "Java2D loading progress"
            progressPanel.add(progressBar)

            frame.isVisible = true

            demo = Java2Demo()

            frame.contentPane.removeAll()
            frame.contentPane.layout = BorderLayout()
            frame.contentPane.add(demo!!, BorderLayout.CENTER)
            FRAME_WIDTH = 730
            FRAME_HEIGHT = 570
            frame.setLocation(d.width / 2 - FRAME_WIDTH / 2, d.height / 2 - FRAME_HEIGHT / 2)
            frame.setSize(FRAME_WIDTH, FRAME_HEIGHT)
            frame.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

            for (arg in args) {
                val value = arg.substringAfter('=', "")
                when {
                    arg.startsWith("-runs=") -> {
                        RunWindow.numRuns = Integer.parseInt(value)
                        RunWindow.exit = true
                        demo!!.createRunWindow()
                    }
                    arg.startsWith("-screen=") ->
                        GlobalControls.screenComboBox.setSelectedIndex(Integer.parseInt(value))
                    arg.startsWith("-antialias=") ->
                        Java2Demo.controls.antialiasingCheckBox.isSelected = value.endsWith("true")
                    arg.startsWith("-rendering=") ->
                        Java2Demo.controls.renderCheckBox.isSelected = value.endsWith("true")
                    arg.startsWith("-texture=") ->
                        Java2Demo.controls.textureCheckBox.isSelected = value.endsWith("true")
                    arg.startsWith("-composite=") ->
                        Java2Demo.controls.compositeCheckBox.isSelected = value.endsWith("true")
                    arg.startsWith("-verbose") -> Java2Demo.verboseCB.isSelected = true
                    arg.startsWith("-print") -> {
                        Java2Demo.printCB.isSelected = true
                        RunWindow.printCheckBox.isSelected = true
                    }
                    arg.startsWith("-columns=") -> DemoGroup.columns = Integer.parseInt(value)
                    arg.startsWith("-buffers=") -> {
                        // usage -buffers=3,10
                        RunWindow.buffersFlag = true
                        val (v1, v2) = value.split(',')
                        RunWindow.bufBeg = Integer.parseInt(v1)
                        RunWindow.bufEnd = Integer.parseInt(v2)
                    }
                    arg.startsWith("-ccthread") -> Java2Demo.ccthreadCB.isSelected = true
                    arg.startsWith("-zoom") -> RunWindow.zoomCheckBox.isSelected = true
                    arg.startsWith("-maxscreen") -> {
                        frame.setLocation(0, 0)
                        frame.setSize(d.width, d.height)
                    }
                }
            }

            frame.validate()
            frame.repaint()
            frame.focusTraversalPolicy.getDefaultComponent(frame).requestFocus()
            demo!!.start()

            if (RunWindow.exit) {
                demo!!.startRunWindow()
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            for (i in args.indices) {
                if (args[i].startsWith("-h") || args[i].startsWith("-help")) {
                    var s = ("\njava -jar Java2Demo.jar -runs=5 -delay=5 -screen=5 "
                            + "-antialias=true -rendering=true -texture=true "
                            + "-composite=true -verbose -print -columns=3 "
                            + "-buffers=5,10 -ccthread -zoom -maxscreen \n")
                    println(s)
                    s = ("    -runs=5       Number of runs to execute\n"
                            + "    -delay=5      Sleep amount between tabs\n"
                            + "    -antialias=   true or false for antialiasing\n"
                            + "    -rendering=   true or false for quality or speed\n"
                            + "    -texture=     true or false for texturing\n"
                            + "    -composite=   true or false for compositing\n"
                            + "    -verbose      output Surface graphic states \n"
                            + "    -print        during run print the Surface, "
                            + "use the Default Printer\n"
                            + "    -columns=3    # of columns to use in clone layout \n"
                            + "    -screen=3     demos all use this screen type \n"
                            + "    -buffers=5,10 during run - clone to see screens "
                            + "five through ten\n"
                            + "    -ccthread     Invoke the Custom Controls Thread \n"
                            + "    -zoom         mouseClick on surface for zoom in  \n"
                            + "    -maxscreen    take up the entire monitor screen \n")
                    println(s)
                    s = ("Examples : \n" + "    Print all of the demos : \n"
                            + "        java -jar Java2Demo.jar -runs=1 -delay=60 -print \n"
                            + "    Run zoomed in with custom control thread \n"
                            + "        java -jar Java2Demo.jar -runs=10 -zoom -ccthread\n")
                    println(s)
                    System.exit(0)
                } else if (args[i].startsWith("-delay=")) {
                    val s = args[i].substring(args[i].indexOf('=') + 1)
                    RunWindow.delay = Integer.parseInt(s)
                }
            }

            SwingUtilities.invokeLater { initFrame(args) }
        }
    }
}
