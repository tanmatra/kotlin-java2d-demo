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
import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Font
import java.io.InterruptedIOException
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.SoftBevelBorder

/**
 * Illustration of how to use the clone feature of the demo.
 */
class CloningFeature(private val java2Demo: Java2Demo) : JPanel(), Runnable
{
    @Volatile
    private var thread: Thread? = null

    private val textArea = JTextArea("Cloning Demonstrated\n\nClicking once on a demo\n").apply {
        minimumSize = Dimension(300, 500)
        font = Font(Font.DIALOG, Font.PLAIN, 14)
        foreground = Color.BLACK
        background = Color.LIGHT_GRAY
        isEditable = false
    }

    init {
        layout = BorderLayout()
        border = CompoundBorder(EmptyBorder(5, 5, 5, 5), SoftBevelBorder(BevelBorder.RAISED))
        add(JScrollPane(textArea), BorderLayout.CENTER)
        start()
    }

    fun start() {
        thread = Thread(this, "CloningFeature").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        thread?.interrupt()
        thread = null
    }

    override fun run() {
        try {
            runExceptionally()
        } catch (e: Exception) {
            // ignore
        }
    }

    @Throws(InterruptedIOException::class)
    private fun runExceptionally() {
        var index = java2Demo.tabbedPaneIndex
        if (index == 0) {
            java2Demo.tabbedPaneIndex = 1
            Thread.sleep(3333)
        }

        if (!java2Demo.globalControls.toolBarCheckBox.isSelected) {
            java2Demo.globalControls.toolBarCheckBox.isSelected = true
            Thread.sleep(2222)
        }

        index = java2Demo.tabbedPaneIndex - 1
        val demoGroup: DemoGroup = java2Demo.groups[index]
        var demoPanel: DemoPanel = demoGroup.panel.getComponent(0) as DemoPanel
        if (demoPanel.surface == null) {
            EventQueue.invokeLater {
                textArea.append("Sorry your zeroth component is not a Surface.")
            }
            return
        }

        demoGroup.scatterDemos(demoPanel.surface!!)

        Thread.sleep(3333)

        textArea.append("Clicking the ToolBar double document button\n")
        Thread.sleep(3333)

        demoPanel = demoGroup.clonePanels[0].getComponent(0) as DemoPanel

        demoPanel.tools?.let { tools ->
            repeat(3) {
                if (thread == null) {
                    return@let
                }
                EventQueue.invokeLater {
                    textArea.append("   Cloning\n")
                    tools.cloneButton!!.doClick()
                }
                Thread.sleep(3333)
            }
        }

        EventQueue.invokeLater {
            textArea.append("Changing attributes \n")
        }

        Thread.sleep(3333)

        val components: Array<out Component> = demoGroup.clonePanels[0].components
        for ((i, component) in components.withIndex()) {
            if (thread == null) {
                break
            }
            demoPanel = component as DemoPanel
            demoPanel.tools?.let { tools ->
                val options = tools.options
                EventQueue.invokeLater {
                    when (i) {
                        0 -> {
                            textArea.append("   Changing AntiAliasing\n")
                            options.antialiasing = !options.antialiasing
                        }
                        1 -> {
                            textArea.append("   Changing Composite & Texture\n")
                            options.composite = !options.composite
                            options.texture = !options.texture
                        }
                        2 -> {
                            textArea.append("   Changing Screen\n")
                            tools.selectedScreenIndex = 4
                        }
                        3 -> {
                            textArea.append("   Removing a clone\n")
                            tools.cloneButton?.doClick()
                        }
                    }
                }
            }
            Thread.sleep(3333)
        }
        EventQueue.invokeLater {
            textArea.append("\nAll Done!")
        }
    }
}
