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
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Panel that holds the Demo groups, Controls and Monitors for each tab.
 * It's a special "always visible" panel for the Controls, MemoryMonitor &
 * PerformanceMonitor.
 */
class GlobalPanel : JPanel(), ChangeListener
{
    private val panel: JPanel
    private var index: Int = 0

    init {
        layout = BorderLayout()
        panel = JPanel(GridBagLayout())
        panel.border = CompoundBorder(EmptyBorder(5, 0, 5, 5), BevelBorder(BevelBorder.LOWERED))
        Java2Demo.addToGridBag(panel, Java2Demo.controls, 0, 0, 1, 1, 0.0, 0.0)
        Java2Demo.addToGridBag(panel, Java2Demo.memorymonitor, 0, 1, 1, 1, 0.0, 0.0)
        Java2Demo.addToGridBag(panel, Java2Demo.performancemonitor!!, 0, 2, 1, 1, 0.0, 0.0)
        add(Java2Demo.intro)
    }

    override fun stateChanged(e: ChangeEvent) {
        Java2Demo.group[index].shutDown(Java2Demo.group[index].panel)
        if (Java2Demo.tabbedPane.selectedIndex == 0) {
            Java2Demo.memorymonitor.surf.stop()
            Java2Demo.performancemonitor!!.surf.stop()
            removeAll()
            add(Java2Demo.intro)
            Java2Demo.intro.start()
        } else {
            if (componentCount == 1) {
                Java2Demo.intro.stop()
                remove(Java2Demo.intro)
                add(panel, BorderLayout.EAST)
                if (Java2Demo.memoryCB.state) {
                    Java2Demo.memorymonitor.surf.start()
                }
                if (Java2Demo.perfCB.state) {
                    Java2Demo.performancemonitor!!.surf.start()
                }
            } else {
                remove(Java2Demo.group[index])
            }
            index = Java2Demo.tabbedPane.selectedIndex - 1
            add(Java2Demo.group[index])
            Java2Demo.group[index].setup(false)
        }
        revalidate()
    }
}
