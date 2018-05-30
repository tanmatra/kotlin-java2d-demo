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
package java2d;


import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Panel that holds the Demo groups, Controls and Monitors for each tab.
 * It's a special "always visible" panel for the Controls, MemoryMonitor &
 * PerformanceMonitor.
 */
@SuppressWarnings("serial")
public class GlobalPanel extends JPanel implements ChangeListener {

    private JPanel p;
    private int index;

    public GlobalPanel() {
        setLayout(new BorderLayout());
        p = new JPanel(new GridBagLayout());
        EmptyBorder eb = new EmptyBorder(5, 0, 5, 5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        p.setBorder(new CompoundBorder(eb, bb));
        Java2Demo.Companion.addToGridBag(p, Java2Demo.Companion.getControls(), 0, 0, 1, 1, 0, 0);
        Java2Demo.Companion.addToGridBag(p, Java2Demo.Companion.getMemorymonitor(), 0, 1, 1, 1, 0, 0);
        Java2Demo.Companion.addToGridBag(p, Java2Demo.Companion.getPerformancemonitor(), 0, 2, 1, 1, 0, 0);
        add(Java2Demo.Companion.getIntro());
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        Java2Demo.Companion.getGroup()[index].shutDown(Java2Demo.Companion.getGroup()[index].getPanel());
        if (Java2Demo.Companion.getTabbedPane().getSelectedIndex() == 0) {
            Java2Demo.Companion.getMemorymonitor().surf.stop();
            Java2Demo.Companion.getPerformancemonitor().surf.stop();
            removeAll();
            add(Java2Demo.Companion.getIntro());
            Java2Demo.Companion.getIntro().start();
        } else {
            if (getComponentCount() == 1) {
                Java2Demo.Companion.getIntro().stop();
                remove(Java2Demo.Companion.getIntro());
                add(p, BorderLayout.EAST);
                if (Java2Demo.Companion.getMemoryCB().getState()) {
                    Java2Demo.Companion.getMemorymonitor().surf.start();
                }
                if (Java2Demo.Companion.getPerfCB().getState()) {
                    Java2Demo.Companion.getPerformancemonitor().surf.start();
                }
            } else {
                remove(Java2Demo.Companion.getGroup()[index]);
            }
            index = Java2Demo.Companion.getTabbedPane().getSelectedIndex() - 1;
            add(Java2Demo.Companion.getGroup()[index]);
            Java2Demo.Companion.getGroup()[index].setup(false);
        }
        revalidate();
    }
}
