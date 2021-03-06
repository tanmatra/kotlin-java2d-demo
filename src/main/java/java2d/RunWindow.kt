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

import java.awt.Color
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.util.Date
import java.util.logging.Level
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

/**
 * A separate window for running the Java2Demo.  Go from tab to tab or demo to demo.
 */
internal class RunWindow(
    internal val options: Options,
    private val java2Demo: Java2Demo,
    internal val frame: JFrame
) : JPanel(GridBagLayout()), Runnable
{
    private val delayTextField: JTextField

    private val runsTextField: JTextField

    @Volatile
    private var thread: Thread? = null

    private val progressBar: JProgressBar

    private val zoomCheckBox = JCheckBox("Zoom").apply {
        isSelected = options.zoom
        horizontalAlignment = SwingConstants.CENTER
    }

    private val printCheckBox = JCheckBox("Print").apply {
        isSelected = options.print
        java2Demo.isDefaultPrinter = options.print
        addActionListener {
            java2Demo.isDefaultPrinter = isSelected
        }
    }

    internal val runButton = JButton("Run").apply {
        background = Color.GREEN
        addActionListener {
            if (text == "Run") {
                doRunAction()
            } else {
                stop()
            }
        }
        minimumSize = Dimension(70, 30)
    }

    init {
        border = CompoundBorder(EmptyBorder(5, 5, 5, 5), BorderFactory.createLoweredBevelBorder())

        add(runButton, GBC(0, 0).fill())

        progressBar = JProgressBar().apply {
            preferredSize = Dimension(100, 30)
            minimum = 0
        }
        add(progressBar, GBC(1, 0).span(2, 1).fill().growHorizontal())

        val p1 = JPanel(GridLayout(2, 2))
        var p2 = JPanel()
        p2.add(JLabel("Runs:"))

        runsTextField = JTextField(options.runs.toString()).apply {
            preferredSize = Dimension(30, 20)
            addActionListener {
                options.runs = text.trim().toInt()
            }
        }.also {
            p2.add(it)
        }

        p1.add(p2)
        p2 = JPanel()
        p2.add(JLabel("Delay:"))
        delayTextField = JTextField(options.delay.toString()).apply {
            preferredSize = Dimension(30, 20)
            addActionListener {
                options.delay = text.trim().toInt()
            }
        }.also {
            p2.add(it)
        }
        p1.add(p2)

        p1.add(zoomCheckBox)
        p1.add(printCheckBox)
        add(p1, GBC(0, 1).span(3, 1).fill().grow())
    }

    fun doRunAction() {
        runButton.text = "Stop"
        runButton.background = Color.RED
        start()
    }

    fun start() {
        thread = Thread(this, "RunWindow").apply {
            priority = Thread.NORM_PRIORITY + 1
            start()
        }
    }

    @Synchronized
    fun stop() {
        thread?.interrupt()
        thread = null
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        (this as java.lang.Object).notifyAll()
    }

    fun sleepPerTab() {
        repeat(options.delay + 1) { _ ->
            repeat(10) { _ ->
                if (thread == null) return
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                }
            }
            EventQueue.invokeLater { progressBar.value = progressBar.value + 1 }
        }
    }

    private fun printDemo(demoGroup: DemoGroup) {
        invokeAndWait {
            if (!java2Demo.globalControls.toolBarCheckBox.isSelected) {
                java2Demo.globalControls.toolBarCheckBox.isSelected = true
                demoGroup.invalidate()
            }
            demoGroup.activePanel.forEachComponent<DemoPanel> { demoPanel ->
                demoPanel.tools?.let { tools ->
                    demoPanel.surface?.animating?.let { animating ->
                        if (animating.isRunning) {
                            tools.startStopButton?.doClick()
                        }
                    }
                    tools.printButton.doClick()
                }
            }
        }
    }

    override fun run() {
        val javaVersion = System.getProperty("java.version")
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        println("\nJava2D Demo RunWindow : ${options.runs} Runs, ${options.delay} second delay between tabs\n" +
                "java version: $javaVersion\n" +
                "$osName $osVersion\n")
        val runtime = Runtime.getRuntime()

        var runNum = 0
        while (runNum < options.runs && thread != null) {
            val date = Date()
            print("#$runNum $date, ")
            runtime.gc()
            val freeMemory = runtime.freeMemory().toFloat()
            val totalMemory = runtime.totalMemory().toFloat()
            println("${((totalMemory - freeMemory) / 1024)}K used")

            for (mainTabIndex in 0 until java2Demo.tabbedPaneCount) {
                if (thread == null) break
                val demoGroup: DemoGroup? = if (mainTabIndex != 0) java2Demo.groups[mainTabIndex - 1] else null
                invokeAndWait {
                    progressBar.value = 0
                    progressBar.maximum = options.delay
                    demoGroup?.invalidate()
                    java2Demo.tabbedPaneIndex = mainTabIndex
                }

                if (demoGroup != null && (zoomCheckBox.isSelected || options.buffersFlag)) {
                    var demoPanel: DemoPanel = demoGroup.activePanel.getComponent(0) as DemoPanel
                    if (demoGroup.tabbedPane == null && demoPanel.surface != null) {
                        invokeAndWait {
                            demoGroup.scatterDemos(demoPanel.surface!!)
                        }
                    }
                    for (subTabIndex in 1 until demoGroup.tabbedPane!!.tabCount) {
                        if (thread == null) break
                        invokeAndWait {
                            progressBar.value = 0
                            progressBar.maximum = options.delay
                            demoGroup.tabbedPane!!.selectedIndex = subTabIndex
                        }
                        val p = demoGroup.activePanel
                        if (options.buffersFlag && p.componentCount == 1) {
                            demoPanel = p.getComponent(0) as DemoPanel
                            demoPanel.surface!!.animating?.stop()
                            for (cloneIndex in options.beginBuffer .. options.endBuffer) {
                                if (thread == null) break
                                invokeAndWait {
                                    demoPanel.tools!!.cloneButton!!.doClick()
                                    (p.getComponent(p.componentCount - 1) as DemoPanel).let { clone ->
                                        clone.surface!!.animating?.stop()
                                        clone.tools?.let { tools ->
                                            tools.issueRepaint = true
                                            tools.selectedScreenIndex = cloneIndex
                                            tools.issueRepaint = false
                                        }
                                    }
                                }
                            }
                        }
                        if (printCheckBox.isSelected) {
                            printDemo(demoGroup)
                        }
                        sleepPerTab()
                    }
                } else if (demoGroup != null && printCheckBox.isSelected) {
                    printDemo(demoGroup)
                    sleepPerTab()
                } else {
                    sleepPerTab()
                }
            }
            if (runNum + 1 == options.runs) {
                println("Finished.")
                if (options.exit && thread != null) {
                    println("System.exit(0).")
                    System.exit(0)
                }
            }
            runNum++
        }
        invokeAndWait {
            runButton.text = "Run"
            runButton.background = Color.GREEN
            progressBar.value = 0
        }

        thread = null
    }

    internal class Options(
        var runs: Int = 20,
        var delay: Int = 10,
        var exit: Boolean = false,
        var zoom: Boolean = false,
        var print: Boolean = false,
        var buffersFlag: Boolean = false,
        var beginBuffer: Int = 0,
        var endBuffer: Int = 0
    ) {
        fun setBuffers(beginBuffer: Int, endBuffer: Int) {
            buffersFlag = true
            this.beginBuffer = beginBuffer
            this.endBuffer = endBuffer
        }
    }

    companion object
    {
        private fun invokeAndWait(run: () -> Unit) {
            try {
                EventQueue.invokeAndWait(run)
            } catch (e: Exception) {
                getLogger<RunWindow>().log(Level.SEVERE, "ERROR in invokeAndWait", e)
            }
        }
    }
}
