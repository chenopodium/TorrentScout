/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.guiutils.flow;

import com.iontorrent.expmodel.FlowListener;
import com.iontorrent.guiutils.FlowPanel;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Chantal Roth
 */
public class FlowSelectionPanel extends JPanel {

    private int flow;
    private JSlider slider;
    private JLabel lbl;
    private boolean DOACTIONS;
    private FlowListener list;
    private JSpinner spinner;

    public FlowSelectionPanel(FlowListener list) {
        this.list = list;
        createGui();
    }
    public void setMax(int max) {
        slider.setMaximum(max);
       // spinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, max, 1));
        spinner.repaint();
    }
    private void createGui() {
        setLayout(new GridLayout(1, 3));
        DOACTIONS = false;
        
        lbl = new javax.swing.JLabel("Flow #:");
        slider = new javax.swing.JSlider();
        slider.setMajorTickSpacing(50);
        slider.setMaximum(220);
        slider.setMinorTickSpacing(10);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setToolTipText("Select a flow");
        slider.addChangeListener(new javax.swing.event.ChangeListener() {

            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderFlowStateChanged(evt);
            }
        });
        spinner = new JSpinner();
        spinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 200, 1));
        spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerFlowStateChanged(evt);
            }
        });
        this.slider.setValue(0);
        this.spinner.setValue(0);
       
        add(lbl);
        add(slider);
        add(new FlowPanel(spinner));
        DOACTIONS = true;
    }

    private void spinnerFlowStateChanged(javax.swing.event.ChangeEvent evt) {
        //     if (DOACTIONS == false) return:
        if (DOACTIONS == false) {
            p("spinnerFlow, got change but actions is false");
            DOACTIONS = true;
            return;
        }
        DOACTIONS = false;
        this.flow = Integer.parseInt(this.spinner.getValue().toString());
        p("Spinner: Changing flow slider to "+flow+",  max is "+((SpinnerNumberModel)spinner.getModel()).getMaximum());
        this.slider.setValue(flow);
        ArrayList<Integer> flows = new  ArrayList<Integer>();
        flows.add(flow);
        list.flowChanged(flows);
        DOACTIONS = true;
    }

    private void sliderFlowStateChanged(javax.swing.event.ChangeEvent evt) {
        //     if (DOACTIONS == false) return:
        if (DOACTIONS == false) {
            p("SliderFlow, got change but actions is false");
            DOACTIONS = true;
            return;
        }
        DOACTIONS = false;
        p("Slider: Changing flow of spinner to "+flow);
        spinner.setValue(this.slider.getValue());
        this.flow = slider.getValue();
       ArrayList<Integer> flows = new  ArrayList<Integer>();
        flows.add(flow);
        list.flowChanged(flows);
        DOACTIONS = true;
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(FlowSelectionPanel.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(FlowSelectionPanel.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(FlowSelectionPanel.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("FlowSelectionPanel: " + msg);
        //Logger.getLogger( FlowSelectionPanel.class.getName()).log(Level.INFO, msg, ex);
    }

    /**
     * @return the flow
     */
    public int getFlow() {
        return flow;
    }

    /**
     * @param flow the flow to set
     */
    public void setFlow(int flow) {
        this.flow = flow;
        DOACTIONS = false;
        slider.setValue(flow);
        spinner.setValue(flow);
         DOACTIONS = true;
        
    }
}
