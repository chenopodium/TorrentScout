/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DataTransPanel.java
 *
 * Created on 13.09.2011, 10:57:35
 */
package com.iontorrent.torrentscout.explorer.fit;

import com.iontorrent.torrentscout.explorer.ExplorerContext;
import com.iontorrent.torrentscout.explorer.fit.AbstractHistoFunction.EvalType;
import com.iontorrent.torrentscout.explorer.process.PlotFunction;
import com.iontorrent.utils.system.Parameter;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 *
 * @author Chantal Roth
 */
public class FitFunctionsPanel extends javax.swing.JPanel {

    AbstractHistoFunction curfunc;
    ArrayList<AbstractHistoFunction> alltrans;
    int FACTOR = 10000;
    boolean DOACTIONS = false;
    private FunctionListener listener;

    /** Creates new form DataTransPanel */
    public FitFunctionsPanel(ExplorerContext maincont, FunctionListener listener) {
        DOACTIONS = false;
        initComponents();
        this.listener = listener;
        alltrans = FitFunctionsFactory.getFunctions(maincont);
        AbstractHistoFunction[] data = new AbstractHistoFunction[alltrans.size()];
        int i = 0;
        for (AbstractHistoFunction t : alltrans) {

            data[i++] = t;
        }
        curfunc = data[0];
        update(curfunc);
        this.listTrans.setListData(data);
        DOACTIONS = true;
        this.setPreferredSize(new Dimension(800, 600));
    }

    private void update(AbstractHistoFunction trans) {
        if (trans == null) {
            return;
        }
        this.curfunc = trans;
        DOACTIONS = false;
//        String s = trans.getDescription();
//        s = s.replace("\n", "<br>");
//        s = s.replace(" ", "&nbsp;");
        //  this.lblDesc.setText("<html>"+s+"</html>");

        Parameter par[] = trans.getParams();
        this.txtParam1.setText("");
        this.txtParam2.setText("");
        this.txtParam3.setText("");
        this.txtParam4.setText("");

        update(lbldesc1, slbl0, val0, txtParam1, par, 0, slider0);
        update(lbldesc2, slbl1, val1, txtParam2, par, 1, slider1);
        update(lbldesc3, slbl2, val2, txtParam3, par, 2, slider2);
        update(lbldesc4, slbl3, val3, txtParam4, par, 3, slider3);
        String s = trans.getHtmlDesc();
        // s = s.replace("\n", "<br>");
        //  s = s.replace(" ", "&nbsp;");
        //   this.jTextArea1.setText("<html>"+s+"</html>");
        this.desc.setText(s);

        this.panelradio.removeAll();
        panelradio.setLayout(new FlowLayout());
        if (curfunc.getPossibleTypes() != null) {
            
            for (int i = 0; i < curfunc.getPossibleTypes().length; i++) {
                EvalType type = curfunc.getPossibleTypes()[i];
                JRadioButton rad = new JRadioButton(type.getDesc());
                if (curfunc.getEvalType() == null) curfunc.setEvalType(type);
                group.add(rad);
                if (curfunc.getEvalType() == type) {
                    rad.setSelected(true);
                }
                panelradio.add(rad);
                rad.addActionListener(new MyListener(type));
            }
        }
        DOACTIONS = true;

    }

    private class MyListener implements ActionListener {
        EvalType type;
        public MyListener(EvalType type) {
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (curfunc != null) {
                curfunc.setEvalType(type);
                p("Setting eval type of "+curfunc.getName()+" to type "+type);
        }
    }
    }
    private void p(String s) {
        System.out.println("FitFunctionspanel: "+s);
    }

    private void update(JLabel lbl, JLabel slbl, JLabel val, JTextField txt, Parameter[] pars, int which, JSlider slider) {
        boolean en = false;
        if (pars != null) {
            en = pars.length > which;
        }
        lbl.setEnabled(en);
        slbl.setEnabled(en);;
        txt.setEnabled(en);
        lbl.setVisible(en);
        slbl.setVisible(en);
        val.setVisible(en);
        txt.setVisible(en);
        slider.setVisible(en);
        slider.setEnabled(en);
        if (en) {
            Parameter par = pars[which];
            if (par != null) {
                if (par.getValue() != null) {
                    txt.setText(par.getValue());
                }
                lbl.setText(par.getName());
                slbl.setText(par.getName());
                txt.setToolTipText(par.getDescription());
                slider.setMinimum((int) (par.getMin() * FACTOR));
                slider.setMaximum((int) (par.getMax() * FACTOR));
                slider.setExtent((int) (par.getInc() * FACTOR));
                slider.setValue((int) (par.getDoubleValue() * FACTOR));
                val.setText(par.getValue());

            }
        }
    }

    public void parseParameters() {
        if (curfunc == null) {
            return;
        }
        Parameter par[] = curfunc.getParams();
        if (par != null) {
            if (par.length > 0) {
                par[0].setValue(this.txtParam1.getText());
            }
            if (par.length > 1) {
                par[1].setValue(this.txtParam2.getText());
            }
            if (par.length > 2) {
                par[2].setValue(this.txtParam3.getText());
            }
            if (par.length > 3) {
                par[3].setValue(this.txtParam4.getText());
            }
            curfunc.setParams(par);
            if (listener != null && curfunc instanceof PlotFunction) {
                listener.functionChanged((PlotFunction) curfunc);
            }
        }
        String s = curfunc.getHtmlDesc();
        // s = s.replace("\n", "<br>");
        //  s = s.replace(" ", "&nbsp;");
        //   this.jTextArea1.setText("<html>"+s+"</html>");
        this.desc.setText(s);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        group = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        listTrans = new javax.swing.JList();
        pCenter = new javax.swing.JPanel();
        pangui = new javax.swing.JPanel();
        txtParam4 = new javax.swing.JTextField();
        lbldesc1 = new javax.swing.JLabel();
        txtParam3 = new javax.swing.JTextField();
        txtParam1 = new javax.swing.JTextField();
        lbldesc3 = new javax.swing.JLabel();
        lbldesc4 = new javax.swing.JLabel();
        lbldesc2 = new javax.swing.JLabel();
        txtParam2 = new javax.swing.JTextField();
        desc = new javax.swing.JLabel();
        panelradio = new javax.swing.JPanel();
        panslider = new javax.swing.JPanel();
        slbl0 = new javax.swing.JLabel();
        slider0 = new javax.swing.JSlider();
        slbl1 = new javax.swing.JLabel();
        slider1 = new javax.swing.JSlider();
        slbl2 = new javax.swing.JLabel();
        slider2 = new javax.swing.JSlider();
        slbl3 = new javax.swing.JLabel();
        slider3 = new javax.swing.JSlider();
        val0 = new javax.swing.JLabel();
        val1 = new javax.swing.JLabel();
        val2 = new javax.swing.JLabel();
        val3 = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        listTrans.setMaximumSize(new java.awt.Dimension(140, 100));
        listTrans.setMinimumSize(new java.awt.Dimension(50, 40));
        listTrans.setPreferredSize(new java.awt.Dimension(100, 100));
        listTrans.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listTransValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listTrans);

        add(jScrollPane1, java.awt.BorderLayout.WEST);

        pCenter.setOpaque(false);
        pCenter.setLayout(new java.awt.BorderLayout());

        pangui.setAlignmentX(0.0F);
        pangui.setOpaque(false);
        pangui.setLayout(new java.awt.GridBagLayout());

        txtParam4.setColumns(10);
        txtParam4.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.txtParam4.text")); // NOI18N
        txtParam4.setMinimumSize(new java.awt.Dimension(60, 20));
        txtParam4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtParam4ActionPerformed(evt);
            }
        });
        txtParam4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtParam4FocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pangui.add(txtParam4, gridBagConstraints);

        lbldesc1.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.lbldesc1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pangui.add(lbldesc1, gridBagConstraints);

        txtParam3.setColumns(10);
        txtParam3.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.txtParam3.text")); // NOI18N
        txtParam3.setMinimumSize(new java.awt.Dimension(60, 20));
        txtParam3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtParam3ActionPerformed(evt);
            }
        });
        txtParam3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtParam3FocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pangui.add(txtParam3, gridBagConstraints);

        txtParam1.setColumns(10);
        txtParam1.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.txtParam1.text")); // NOI18N
        txtParam1.setMinimumSize(new java.awt.Dimension(60, 20));
        txtParam1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtParam1ActionPerformed(evt);
            }
        });
        txtParam1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtParam1FocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pangui.add(txtParam1, gridBagConstraints);

        lbldesc3.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.lbldesc3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pangui.add(lbldesc3, gridBagConstraints);

        lbldesc4.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.lbldesc4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        pangui.add(lbldesc4, gridBagConstraints);

        lbldesc2.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.lbldesc2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        pangui.add(lbldesc2, gridBagConstraints);

        txtParam2.setColumns(10);
        txtParam2.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.txtParam2.text")); // NOI18N
        txtParam2.setMinimumSize(new java.awt.Dimension(60, 20));
        txtParam2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtParam2ActionPerformed(evt);
            }
        });
        txtParam2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtParam2FocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pangui.add(txtParam2, gridBagConstraints);

        desc.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.desc.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 10;
        pangui.add(desc, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 10;
        pangui.add(panelradio, gridBagConstraints);

        pCenter.add(pangui, java.awt.BorderLayout.NORTH);

        panslider.setLayout(new java.awt.GridBagLayout());

        slbl0.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.slbl0.text")); // NOI18N
        panslider.add(slbl0, new java.awt.GridBagConstraints());

        slider0.setMinimumSize(new java.awt.Dimension(100, 23));
        slider0.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider0StateChanged(evt);
            }
        });
        panslider.add(slider0, new java.awt.GridBagConstraints());

        slbl1.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.slbl1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        panslider.add(slbl1, gridBagConstraints);

        slider1.setMinimumSize(new java.awt.Dimension(100, 23));
        slider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider1StateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        panslider.add(slider1, gridBagConstraints);

        slbl2.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.slbl2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        panslider.add(slbl2, gridBagConstraints);

        slider2.setMinimumSize(new java.awt.Dimension(100, 23));
        slider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider2StateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        panslider.add(slider2, gridBagConstraints);

        slbl3.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.slbl3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        panslider.add(slbl3, gridBagConstraints);

        slider3.setMinimumSize(new java.awt.Dimension(100, 23));
        slider3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider3StateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        panslider.add(slider3, gridBagConstraints);

        val0.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.val0.text")); // NOI18N
        panslider.add(val0, new java.awt.GridBagConstraints());

        val1.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.val1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        panslider.add(val1, gridBagConstraints);

        val2.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.val2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        panslider.add(val2, gridBagConstraints);

        val3.setText(org.openide.util.NbBundle.getMessage(FitFunctionsPanel.class, "FitFunctionsPanel.val3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        panslider.add(val3, gridBagConstraints);

        pCenter.add(panslider, java.awt.BorderLayout.CENTER);

        add(pCenter, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtParam1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtParam1ActionPerformed
        parseParameters();
}//GEN-LAST:event_txtParam1ActionPerformed

    private void txtParam2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtParam2ActionPerformed
        parseParameters();
}//GEN-LAST:event_txtParam2ActionPerformed

    public void select(AbstractHistoFunction func) {
        DOACTIONS = false;
        if (curfunc != null) {
            this.parseParameters();
        }
        if (func != null) {
            this.update(func);
            if (listTrans.getSelectedValue() != func) {
                listTrans.setSelectedValue(func, true);
            }
        }
        DOACTIONS = true;
    }
    private void listTransValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listTransValueChanged
        if (!DOACTIONS) {
            return;
        }
        select((AbstractHistoFunction) listTrans.getSelectedValue());
    }//GEN-LAST:event_listTransValueChanged

    private void txtParam1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtParam1FocusLost
        parseParameters();
    }//GEN-LAST:event_txtParam1FocusLost

    private void txtParam2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtParam2FocusLost
        parseParameters();
    }//GEN-LAST:event_txtParam2FocusLost

    private void txtParam4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtParam4ActionPerformed
        parseParameters();
    }//GEN-LAST:event_txtParam4ActionPerformed

    private void txtParam3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtParam3ActionPerformed
        parseParameters();
    }//GEN-LAST:event_txtParam3ActionPerformed

    private void txtParam3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtParam3FocusLost
        parseParameters();
    }//GEN-LAST:event_txtParam3FocusLost

    private void txtParam4FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtParam4FocusLost
        parseParameters();
    }//GEN-LAST:event_txtParam4FocusLost

    private void slider0StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider0StateChanged
        if (!DOACTIONS) {
            return;
        }
        JSlider slider = (JSlider) evt.getSource();
        val0.setText("" + (double) slider.getValue() / FACTOR);
        txtParam1.setText(val0.getText());
        parseParameters();
    }//GEN-LAST:event_slider0StateChanged

    private void slider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider1StateChanged
        if (!DOACTIONS) {
            return;
        }
        JSlider slider = (JSlider) evt.getSource();
        val1.setText("" + (double) slider.getValue() / FACTOR);
        txtParam2.setText(val1.getText());
        parseParameters();
    }//GEN-LAST:event_slider1StateChanged

    private void slider2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider2StateChanged

        if (!DOACTIONS) {
            return;
        }
        JSlider slider = (JSlider) evt.getSource();
        val2.setText("" + (double) slider.getValue() / FACTOR);
        txtParam3.setText(val2.getText());
        parseParameters();
    }//GEN-LAST:event_slider2StateChanged

    private void slider3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider3StateChanged
        if (!DOACTIONS) {
            return;
        }
        JSlider slider = (JSlider) evt.getSource();
        val3.setText("" + (double) slider.getValue() / FACTOR);
        txtParam4.setText(val3.getText());
        parseParameters();
    }//GEN-LAST:event_slider3StateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel desc;
    private javax.swing.ButtonGroup group;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbldesc1;
    private javax.swing.JLabel lbldesc2;
    private javax.swing.JLabel lbldesc3;
    private javax.swing.JLabel lbldesc4;
    private javax.swing.JList listTrans;
    private javax.swing.JPanel pCenter;
    private javax.swing.JPanel panelradio;
    private javax.swing.JPanel pangui;
    private javax.swing.JPanel panslider;
    private javax.swing.JLabel slbl0;
    private javax.swing.JLabel slbl1;
    private javax.swing.JLabel slbl2;
    private javax.swing.JLabel slbl3;
    private javax.swing.JSlider slider0;
    private javax.swing.JSlider slider1;
    private javax.swing.JSlider slider2;
    private javax.swing.JSlider slider3;
    private javax.swing.JTextField txtParam1;
    private javax.swing.JTextField txtParam2;
    private javax.swing.JTextField txtParam3;
    private javax.swing.JTextField txtParam4;
    private javax.swing.JLabel val0;
    private javax.swing.JLabel val1;
    private javax.swing.JLabel val2;
    private javax.swing.JLabel val3;
    // End of variables declaration//GEN-END:variables
}
