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

package com.iontorrent.dbaccess;


import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author Chantal Roth
 */
@Entity
@Table(name = "rundb_analysismetrics")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbAnalysismetrics.findAll", query = "SELECT r FROM RundbAnalysismetrics r")})
public class RundbAnalysismetrics implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "\"id\"")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "\"libLive\"")
    private int libLive;
    @Basic(optional = false)
    @Column(name = "\"libKp\"")
    private int libKp;
    @Basic(optional = false)
    @Column(name = "\"libMix\"")
    private int libMix;
    @Basic(optional = false)
    @Column(name = "\"libFinal\"")
    private int libFinal;
    @Basic(optional = false)
    @Column(name = "\"tfLive\"")
    private int tfLive;
    @Basic(optional = false)
    @Column(name = "\"tfKp\"")
    private int tfKp;
    @Basic(optional = false)
    @Column(name = "\"tfMix\"")
    private int tfMix;
    @Basic(optional = false)
    @Column(name = "\"tfFinal\"")
    private int tfFinal;
    @Basic(optional = false)
    @Column(name = "\"empty\"")
    private int empty;
    @Basic(optional = false)
    @Column(name = "\"bead\"")
    private int bead;
    @Basic(optional = false)
    @Column(name = "\"live\"")
    private int live;
    @Basic(optional = false)
    @Column(name = "\"dud\"")
    private int dud;
    @Basic(optional = false)
    @Column(name = "\"amb\"")
    private int amb;
    @Basic(optional = false)
    @Column(name = "\"tf\"")
    private int tf;
    @Basic(optional = false)
    @Column(name = "\"lib\"")
    private int lib;
    @Basic(optional = false)
    @Column(name = "\"pinned\"")
    private int pinned;
    @Basic(optional = false)
    @Column(name = "\"ignored\"")
    private int ignored;
    @Basic(optional = false)
    @Column(name = "\"excluded\"")
    private int excluded;
    @Basic(optional = false)
    @Column(name = "\"washout\"")
    private int washout;
    @Basic(optional = false)
    @Column(name = "\"washout_dud\"")
    private int washoutDud;
    @Basic(optional = false)
    @Column(name = "\"washout_ambiguous\"")
    private int washoutAmbiguous;
    @Basic(optional = false)
    @Column(name = "\"washout_live\"")
    private int washoutLive;
    @Basic(optional = false)
    @Column(name = "\"washout_test_fragment\"")
    private int washoutTestFragment;
    @Basic(optional = false)
    @Column(name = "\"washout_library\"")
    private int washoutLibrary;
    @Basic(optional = false)
    @Column(name = "\"lib_pass_basecaller\"")
    private int libPassBasecaller;
    @Basic(optional = false)
    @Column(name = "\"lib_pass_cafie\"")
    private int libPassCafie;
    @Basic(optional = false)
    @Column(name = "\"keypass_all_beads\"")
    private int keypassAllBeads;
    @Basic(optional = false)
    @Column(name = "\"sysCF\"")
    private double sysCF;
    @Basic(optional = false)
    @Column(name = "\"sysIE\"")
    private double sysIE;
    @Basic(optional = false)
    @Column(name = "\"sysDR\"")
    private double sysDR;
    @JoinColumn(name = "report_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RundbResults reportId;

    public RundbAnalysismetrics() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbAnalysismetrics.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbAnalysismetrics.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbAnalysismetrics.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RundbAnalysismetrics: " + msg);
        //Logger.getLogger( RundbAnalysismetrics.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbAnalysismetrics(Integer id) {
        this.id = id;
    }

    public RundbAnalysismetrics(Integer id, int libLive, int libKp, int libMix, int libFinal, int tfLive, int tfKp, int tfMix, int tfFinal, int empty, int bead, int live, int dud, int amb, int tf, int lib, int pinned, int ignored, int excluded, int washout, int washoutDud, int washoutAmbiguous, int washoutLive, int washoutTestFragment, int washoutLibrary, int libPassBasecaller, int libPassCafie, int keypassAllBeads, double sysCF, double sysIE, double sysDR) {
        this.id = id;
        this.libLive = libLive;
        this.libKp = libKp;
        this.libMix = libMix;
        this.libFinal = libFinal;
        this.tfLive = tfLive;
        this.tfKp = tfKp;
        this.tfMix = tfMix;
        this.tfFinal = tfFinal;
        this.empty = empty;
        this.bead = bead;
        this.live = live;
        this.dud = dud;
        this.amb = amb;
        this.tf = tf;
        this.lib = lib;
        this.pinned = pinned;
        this.ignored = ignored;
        this.excluded = excluded;
        this.washout = washout;
        this.washoutDud = washoutDud;
        this.washoutAmbiguous = washoutAmbiguous;
        this.washoutLive = washoutLive;
        this.washoutTestFragment = washoutTestFragment;
        this.washoutLibrary = washoutLibrary;
        this.libPassBasecaller = libPassBasecaller;
        this.libPassCafie = libPassCafie;
        this.keypassAllBeads = keypassAllBeads;
        this.sysCF = sysCF;
        this.sysIE = sysIE;
        this.sysDR = sysDR;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getLibLive() {
        return libLive;
    }

    public void setLibLive(int libLive) {
        this.libLive = libLive;
    }

    public int getLibKp() {
        return libKp;
    }

    public void setLibKp(int libKp) {
        this.libKp = libKp;
    }

    public int getLibMix() {
        return libMix;
    }

    public void setLibMix(int libMix) {
        this.libMix = libMix;
    }

    public int getLibFinal() {
        return libFinal;
    }

    public void setLibFinal(int libFinal) {
        this.libFinal = libFinal;
    }

    public int getTfLive() {
        return tfLive;
    }

    public void setTfLive(int tfLive) {
        this.tfLive = tfLive;
    }

    public int getTfKp() {
        return tfKp;
    }

    public void setTfKp(int tfKp) {
        this.tfKp = tfKp;
    }

    public int getTfMix() {
        return tfMix;
    }

    public void setTfMix(int tfMix) {
        this.tfMix = tfMix;
    }

    public int getTfFinal() {
        return tfFinal;
    }

    public void setTfFinal(int tfFinal) {
        this.tfFinal = tfFinal;
    }

    public int getEmpty() {
        return empty;
    }

    public void setEmpty(int empty) {
        this.empty = empty;
    }

    public int getBead() {
        return bead;
    }

    public void setBead(int bead) {
        this.bead = bead;
    }

    public int getLive() {
        return live;
    }

    public void setLive(int live) {
        this.live = live;
    }

    public int getDud() {
        return dud;
    }

    public void setDud(int dud) {
        this.dud = dud;
    }

    public int getAmb() {
        return amb;
    }

    public void setAmb(int amb) {
        this.amb = amb;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public int getLib() {
        return lib;
    }

    public void setLib(int lib) {
        this.lib = lib;
    }

    public int getPinned() {
        return pinned;
    }

    public void setPinned(int pinned) {
        this.pinned = pinned;
    }

    public int getIgnored() {
        return ignored;
    }

    public void setIgnored(int ignored) {
        this.ignored = ignored;
    }

    public int getExcluded() {
        return excluded;
    }

    public void setExcluded(int excluded) {
        this.excluded = excluded;
    }

    public int getWashout() {
        return washout;
    }

    public void setWashout(int washout) {
        this.washout = washout;
    }

    public int getWashoutDud() {
        return washoutDud;
    }

    public void setWashoutDud(int washoutDud) {
        this.washoutDud = washoutDud;
    }

    public int getWashoutAmbiguous() {
        return washoutAmbiguous;
    }

    public void setWashoutAmbiguous(int washoutAmbiguous) {
        this.washoutAmbiguous = washoutAmbiguous;
    }

    public int getWashoutLive() {
        return washoutLive;
    }

    public void setWashoutLive(int washoutLive) {
        this.washoutLive = washoutLive;
    }

    public int getWashoutTestFragment() {
        return washoutTestFragment;
    }

    public void setWashoutTestFragment(int washoutTestFragment) {
        this.washoutTestFragment = washoutTestFragment;
    }

    public int getWashoutLibrary() {
        return washoutLibrary;
    }

    public void setWashoutLibrary(int washoutLibrary) {
        this.washoutLibrary = washoutLibrary;
    }

    public int getLibPassBasecaller() {
        return libPassBasecaller;
    }

    public void setLibPassBasecaller(int libPassBasecaller) {
        this.libPassBasecaller = libPassBasecaller;
    }

    public int getLibPassCafie() {
        return libPassCafie;
    }

    public void setLibPassCafie(int libPassCafie) {
        this.libPassCafie = libPassCafie;
    }

    public int getKeypassAllBeads() {
        return keypassAllBeads;
    }

    public void setKeypassAllBeads(int keypassAllBeads) {
        this.keypassAllBeads = keypassAllBeads;
    }

    public double getSysCF() {
        return sysCF;
    }

    public void setSysCF(double sysCF) {
        this.sysCF = sysCF;
    }

    public double getSysIE() {
        return sysIE;
    }

    public void setSysIE(double sysIE) {
        this.sysIE = sysIE;
    }

    public double getSysDR() {
        return sysDR;
    }

    public void setSysDR(double sysDR) {
        this.sysDR = sysDR;
    }

    public RundbResults getReportId() {
        return reportId;
    }

    public void setReportId(RundbResults reportId) {
        this.reportId = reportId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RundbAnalysismetrics)) {
            return false;
        }
        RundbAnalysismetrics other = (RundbAnalysismetrics) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbAnalysismetrics[ id=" + id + " ]";
    }
}
