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
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.util.Date
import java.util.logging.Level
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

/**
 * A separate window for running the Java2Demo.  Go from tab to tab or demo to
 * demo.
 */
class RunWindow : JPanel(GridBagLayout()), Runnable
{
    private val delayTextField: JTextField
    private val runsTextField: JTextField
    private var thread: Thread? = null
    private val progressBar: JProgressBar
    private var demoGroup: DemoGroup? = null
    private var demoPanel: DemoPanel? = null

    init {
        border = CompoundBorder(EmptyBorder(5, 5, 5, 5), BevelBorder(BevelBorder.LOWERED))

        runButton = JButton("Run").apply {
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
        Java2Demo.addToGridBag(this, runButton, 0, 0, 1, 1, 0.0, 0.0)

        progressBar = JProgressBar().apply {
            preferredSize = Dimension(100, 30)
            minimum = 0
        }
        Java2Demo.addToGridBag(this, progressBar, 1, 0, 2, 1, 1.0, 0.0)

        val p1 = JPanel(GridLayout(2, 2))
        var p2 = JPanel()
        p2.add(JLabel("Runs:").apply {
            font = FONT
            foreground = Color.BLACK
        })

        runsTextField = JTextField(numRuns.toString()).apply {
            preferredSize = Dimension(30, 20)
            addActionListener {
                numRuns = Integer.parseInt(text.trim())
            }
        }.also {
            p2.add(it)
        }

        p1.add(p2)
        p2 = JPanel()
        p2.add(JLabel("Delay:").apply {
            font = FONT
            foreground = Color.BLACK
        })
        delayTextField = JTextField(delay.toString()).apply {
            preferredSize = Dimension(30, 20)
            addActionListener {
                delay = Integer.parseInt(text.trim())
            }
        }.also {
            p2.add(it)
        }
        p1.add(p2)

        zoomCheckBox.horizontalAlignment = SwingConstants.CENTER
        zoomCheckBox.font = FONT
        printCheckBox.font = FONT
        p1.add(zoomCheckBox)
        p1.add(printCheckBox)
        printCheckBox.addActionListener {
            Java2Demo.printCB.isSelected = printCheckBox.isSelected
        }
        Java2Demo.addToGridBag(this, p1, 0, 1, 3, 1, 1.0, 1.0)
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
        var j = 0
        while (j < delay + 1 && thread != null) {
            var k = 0
            while (k < 10 && thread != null) {
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                }

                k++
            }
            EventQueue.invokeLater { progressBar.value = progressBar.value + 1 }
            j++
        }
    }

    private fun printDemo(dg: DemoGroup?) {
        invokeAndWait {
            if (!Java2Demo.controls.toolBarCB.isSelected) {
                Java2Demo.controls.toolBarCB.isSelected = true
                dg!!.invalidate()
            }
            for (comp in dg!!.panel.components) {
                val dp = comp as DemoPanel
                if (dp.tools != null) {
                    if (dp.surface.animating != null) {
                        if (dp.surface.animating.running()) {
                            dp.tools.startStopButton!!.doClick()
                        }
                    }
                    dp.tools.printButton.doClick()
                }
            }
        }
    }

    override fun run() {
        val javaVersion = System.getProperty("java.version")
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        println("\nJava2D Demo RunWindow : $numRuns Runs, $delay second delay between tabs\n" +
                "java version: $javaVersion\n" +
                "$osName $osVersion\n")
        val runtime = Runtime.getRuntime()

        var runNum = 0
        while (runNum < numRuns && thread != null) {
            val date = Date()
            print("#$runNum $date, ")
            runtime.gc()
            val freeMemory = runtime.freeMemory().toFloat()
            val totalMemory = runtime.totalMemory().toFloat()
            println("${((totalMemory - freeMemory) / 1024)}K used")

            var i = 0
            while (i < Java2Demo.tabbedPane.tabCount && thread != null) {

                val mainTabIndex = i
                invokeAndWait {
                    progressBar.value = 0
                    progressBar.maximum = delay
                    if (mainTabIndex != 0) {
                        demoGroup = Java2Demo.group[mainTabIndex - 1]
                        demoGroup!!.invalidate()
                    }
                    Java2Demo.tabbedPane.selectedIndex = mainTabIndex
                }

                if (i != 0 && (zoomCheckBox.isSelected || buffersFlag)) {
                    demoPanel = demoGroup!!.panel.getComponent(0) as DemoPanel
                    if (demoGroup!!.tabbedPane == null && demoPanel!!.surface != null) {
                        invokeAndWait {
                            demoGroup!!.mouseClicked(demoPanel!!.surface)
                        }
                    }
                    var j = 1
                    while (j < demoGroup!!.tabbedPane!!.tabCount && thread != null) {
                        val subTabIndex = j
                        invokeAndWait {
                            progressBar.value = 0
                            progressBar.maximum = delay
                            demoGroup!!.tabbedPane!!.selectedIndex = subTabIndex
                        }

                        val p = demoGroup!!.panel
                        if (buffersFlag && p.componentCount == 1) {
                            demoPanel = p.getComponent(0) as DemoPanel
                            if (demoPanel!!.surface.animating != null) {
                                demoPanel!!.surface.animating.stop()
                            }
                            var k = bufBeg
                            while (k <= bufEnd && thread != null) {
                                val cloneIndex = k
                                invokeAndWait {
                                    demoPanel!!.tools.cloneButton!!.doClick()
                                    val n = p.componentCount
                                    val clone = p.getComponent(n - 1) as DemoPanel
                                    clone.surface.animating?.stop()
                                    clone.tools.issueRepaint = true
                                    clone.tools.screenCombo.selectedIndex = cloneIndex
                                    clone.tools.issueRepaint = false
                                }
                                k++
                            }
                        }
                        if (printCheckBox.isSelected) {
                            printDemo(demoGroup)
                        }
                        sleepPerTab()
                        j++
                    }
                } else if (i != 0 && printCheckBox.isSelected) {
                    printDemo(demoGroup)
                    sleepPerTab()
                } else {
                    sleepPerTab()
                }
                i++
            }
            if (runNum + 1 == numRuns) {
                println("Finished.")
                if (exit && thread != null) {
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
        demoGroup = null
        demoPanel = null
    }

    companion object
    {
        lateinit var runButton: JButton
        var delay = 10
        var numRuns = 20
        var exit: Boolean = false
        val zoomCheckBox = JCheckBox("Zoom")
        val printCheckBox = JCheckBox("Print")
        var buffersFlag: Boolean = false
        var bufBeg: Int = 0
        var bufEnd: Int = 0
        private val FONT = Font(Font.SERIF, Font.PLAIN, 10)

        private fun invokeAndWait(run: () -> Unit) {
            try {
                EventQueue.invokeAndWait(run)
            } catch (e: Exception) {
                getLogger<RunWindow>().log(Level.SEVERE, "ERROR in invokeAndWait", e)
            }
        }
    }
}
