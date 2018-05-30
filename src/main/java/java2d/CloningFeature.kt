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
import java.awt.Dimension
import java.awt.Font
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
class CloningFeature : JPanel(), Runnable
{
    private var thread: Thread? = null
    private val ta: JTextArea

    init {
        layout = BorderLayout()
        val eb = EmptyBorder(5, 5, 5, 5)
        val sbb = SoftBevelBorder(BevelBorder.RAISED)
        border = CompoundBorder(eb, sbb)

        ta = JTextArea("Cloning Demonstrated\n\nClicking once on a demo\n")
        ta.minimumSize = Dimension(300, 500)
        val scroller = JScrollPane()
        scroller.viewport.add(ta)
        ta.font = Font("Dialog", Font.PLAIN, 14)
        ta.foreground = Color.black
        ta.background = Color.lightGray
        ta.isEditable = false

        add("Center", scroller)

        start()
    }

    fun start() {
        thread = Thread(this)
        thread!!.priority = Thread.MAX_PRIORITY
        thread!!.name = "CloningFeature"
        thread!!.start()
    }

    fun stop() {
        if (thread != null) {
            thread!!.interrupt()
        }
        thread = null
    }

    override fun run() {

        var index = Java2Demo.tabbedPane.selectedIndex
        if (index == 0) {
            Java2Demo.tabbedPane.selectedIndex = 1
            try {
                Thread.sleep(3333)
            } catch (e: Exception) {
                return
            }
        }

        if (!Java2Demo.controls.toolBarCB.isSelected) {
            Java2Demo.controls.toolBarCB.isSelected = true
            try {
                Thread.sleep(2222)
            } catch (e: Exception) {
                return
            }
        }

        index = Java2Demo.tabbedPane.selectedIndex - 1
        val dg = Java2Demo.group[index]
        var dp = dg.panel.getComponent(0) as DemoPanel
        if (dp.surface == null) {
            ta.append("Sorry your zeroth component is not a Surface.")
            return
        }

        dg.mouseClicked(dp.surface)

        try {
            Thread.sleep(3333)
        } catch (e: Exception) {
            return
        }

        ta.append("Clicking the ToolBar double document button\n")
        try {
            Thread.sleep(3333)
        } catch (e: Exception) {
            return
        }

        dp = dg.clonePanels[0].getComponent(0) as DemoPanel

        if (dp.tools != null) {
            var i = 0
            while (i < 3 && thread != null) {
                ta.append("   Cloning\n")
                dp.tools.cloneB.doClick()
                try {
                    Thread.sleep(3333)
                } catch (e: Exception) {
                    return
                }

                i++
            }
        }

        ta.append("Changing attributes \n")

        try {
            Thread.sleep(3333)
        } catch (e: Exception) {
            return
        }

        val cmps = dg.clonePanels[0].components
        var i = 0
        while (i < cmps.size && thread != null) {
            dp = cmps[i] as DemoPanel
            if (dp.tools == null) {
                i++
                continue
            }
            when (i) {
                0 -> {
                    ta.append("   Changing AntiAliasing\n")
                    dp.tools.aliasB.doClick()
                }
                1 -> {
                    ta.append("   Changing Composite & Texture\n")
                    dp.tools.compositeB.doClick()
                    dp.tools.textureB.doClick()
                }
                2 -> {
                    ta.append("   Changing Screen\n")
                    dp.tools.screenCombo.setSelectedIndex(4)
                }
                3 -> {
                    ta.append("   Removing a clone\n")
                    dp.tools.cloneB.doClick()
                }
            }
            try {
                Thread.sleep(3333)
            } catch (e: Exception) {
                return
            }
            i++
        }

        ta.append("\nAll Done!")
    }
}