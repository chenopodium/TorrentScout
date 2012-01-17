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
@Table(name = "rundb_qualitymetrics")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbQualitymetrics.findAll", query = "SELECT r FROM RundbQualitymetrics r")})
public class RundbQualitymetrics implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "q0_bases")
    private int q0Bases;
    @Basic(optional = false)
    @Column(name = "q0_reads")
    private int q0Reads;
    @Basic(optional = false)
    @Column(name = "q0_max_read_length")
    private int q0MaxReadLength;
    @Basic(optional = false)
    @Column(name = "q0_mean_read_length")
    private double q0MeanReadLength;
    @Basic(optional = false)
    @Column(name = "q0_50bp_reads")
    private int q050bpReads;
    @Basic(optional = false)
    @Column(name = "q0_100bp_reads")
    private int q0100bpReads;
    @Basic(optional = false)
    @Column(name = "q0_15bp_reads")
    private int q015bpReads;
    @Basic(optional = false)
    @Column(name = "q17_bases")
    private int q17Bases;
    @Basic(optional = false)
    @Column(name = "q17_reads")
    private int q17Reads;
    @Basic(optional = false)
    @Column(name = "q17_max_read_length")
    private int q17MaxReadLength;
    @Basic(optional = false)
    @Column(name = "q17_mean_read_length")
    private double q17MeanReadLength;
    @Basic(optional = false)
    @Column(name = "q17_50bp_reads")
    private int q1750bpReads;
    @Basic(optional = false)
    @Column(name = "q17_100bp_reads")
    private int q17100bpReads;
    @Basic(optional = false)
    @Column(name = "q17_150bp_reads")
    private int q17150bpReads;
    @Basic(optional = false)
    @Column(name = "q20_bases")
    private int q20Bases;
    @Basic(optional = false)
    @Column(name = "q20_reads")
    private int q20Reads;
    @Basic(optional = false)
    @Column(name = "q20_max_read_length")
    private double q20MaxReadLength;
    @Basic(optional = false)
    @Column(name = "q20_mean_read_length")
    private int q20MeanReadLength;
    @Basic(optional = false)
    @Column(name = "q20_50bp_reads")
    private int q2050bpReads;
    @Basic(optional = false)
    @Column(name = "q20_100bp_reads")
    private int q20100bpReads;
    @Basic(optional = false)
    @Column(name = "q20_150bp_reads")
    private int q20150bpReads;
    @JoinColumn(name = "report_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RundbResults reportId;

    public RundbQualitymetrics() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbQualitymetrics.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbQualitymetrics.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbQualitymetrics.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RundbQualitymetrics: " + msg);
        //Logger.getLogger( RundbQualitymetrics.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbQualitymetrics(Integer id) {
        this.id = id;
    }

    public RundbQualitymetrics(Integer id, int q0Bases, int q0Reads, int q0MaxReadLength, double q0MeanReadLength, int q050bpReads, int q0100bpReads, int q015bpReads, int q17Bases, int q17Reads, int q17MaxReadLength, double q17MeanReadLength, int q1750bpReads, int q17100bpReads, int q17150bpReads, int q20Bases, int q20Reads, double q20MaxReadLength, int q20MeanReadLength, int q2050bpReads, int q20100bpReads, int q20150bpReads) {
        this.id = id;
        this.q0Bases = q0Bases;
        this.q0Reads = q0Reads;
        this.q0MaxReadLength = q0MaxReadLength;
        this.q0MeanReadLength = q0MeanReadLength;
        this.q050bpReads = q050bpReads;
        this.q0100bpReads = q0100bpReads;
        this.q015bpReads = q015bpReads;
        this.q17Bases = q17Bases;
        this.q17Reads = q17Reads;
        this.q17MaxReadLength = q17MaxReadLength;
        this.q17MeanReadLength = q17MeanReadLength;
        this.q1750bpReads = q1750bpReads;
        this.q17100bpReads = q17100bpReads;
        this.q17150bpReads = q17150bpReads;
        this.q20Bases = q20Bases;
        this.q20Reads = q20Reads;
        this.q20MaxReadLength = q20MaxReadLength;
        this.q20MeanReadLength = q20MeanReadLength;
        this.q2050bpReads = q2050bpReads;
        this.q20100bpReads = q20100bpReads;
        this.q20150bpReads = q20150bpReads;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getQ0Bases() {
        return q0Bases;
    }

    public void setQ0Bases(int q0Bases) {
        this.q0Bases = q0Bases;
    }

    public int getQ0Reads() {
        return q0Reads;
    }

    public void setQ0Reads(int q0Reads) {
        this.q0Reads = q0Reads;
    }

    public int getQ0MaxReadLength() {
        return q0MaxReadLength;
    }

    public void setQ0MaxReadLength(int q0MaxReadLength) {
        this.q0MaxReadLength = q0MaxReadLength;
    }

    public double getQ0MeanReadLength() {
        return q0MeanReadLength;
    }

    public void setQ0MeanReadLength(double q0MeanReadLength) {
        this.q0MeanReadLength = q0MeanReadLength;
    }

    public int getQ050bpReads() {
        return q050bpReads;
    }

    public void setQ050bpReads(int q050bpReads) {
        this.q050bpReads = q050bpReads;
    }

    public int getQ0100bpReads() {
        return q0100bpReads;
    }

    public void setQ0100bpReads(int q0100bpReads) {
        this.q0100bpReads = q0100bpReads;
    }

    public int getQ015bpReads() {
        return q015bpReads;
    }

    public void setQ015bpReads(int q015bpReads) {
        this.q015bpReads = q015bpReads;
    }

    public int getQ17Bases() {
        return q17Bases;
    }

    public void setQ17Bases(int q17Bases) {
        this.q17Bases = q17Bases;
    }

    public int getQ17Reads() {
        return q17Reads;
    }

    public void setQ17Reads(int q17Reads) {
        this.q17Reads = q17Reads;
    }

    public int getQ17MaxReadLength() {
        return q17MaxReadLength;
    }

    public void setQ17MaxReadLength(int q17MaxReadLength) {
        this.q17MaxReadLength = q17MaxReadLength;
    }

    public double getQ17MeanReadLength() {
        return q17MeanReadLength;
    }

    public void setQ17MeanReadLength(double q17MeanReadLength) {
        this.q17MeanReadLength = q17MeanReadLength;
    }

    public int getQ1750bpReads() {
        return q1750bpReads;
    }

    public void setQ1750bpReads(int q1750bpReads) {
        this.q1750bpReads = q1750bpReads;
    }

    public int getQ17100bpReads() {
        return q17100bpReads;
    }

    public void setQ17100bpReads(int q17100bpReads) {
        this.q17100bpReads = q17100bpReads;
    }

    public int getQ17150bpReads() {
        return q17150bpReads;
    }

    public void setQ17150bpReads(int q17150bpReads) {
        this.q17150bpReads = q17150bpReads;
    }

    public int getQ20Bases() {
        return q20Bases;
    }

    public void setQ20Bases(int q20Bases) {
        this.q20Bases = q20Bases;
    }

    public int getQ20Reads() {
        return q20Reads;
    }

    public void setQ20Reads(int q20Reads) {
        this.q20Reads = q20Reads;
    }

    public double getQ20MaxReadLength() {
        return q20MaxReadLength;
    }

    public void setQ20MaxReadLength(double q20MaxReadLength) {
        this.q20MaxReadLength = q20MaxReadLength;
    }

    public int getQ20MeanReadLength() {
        return q20MeanReadLength;
    }

    public void setQ20MeanReadLength(int q20MeanReadLength) {
        this.q20MeanReadLength = q20MeanReadLength;
    }

    public int getQ2050bpReads() {
        return q2050bpReads;
    }

    public void setQ2050bpReads(int q2050bpReads) {
        this.q2050bpReads = q2050bpReads;
    }

    public int getQ20100bpReads() {
        return q20100bpReads;
    }

    public void setQ20100bpReads(int q20100bpReads) {
        this.q20100bpReads = q20100bpReads;
    }

    public int getQ20150bpReads() {
        return q20150bpReads;
    }

    public void setQ20150bpReads(int q20150bpReads) {
        this.q20150bpReads = q20150bpReads;
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
        if (!(object instanceof RundbQualitymetrics)) {
            return false;
        }
        RundbQualitymetrics other = (RundbQualitymetrics) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbQualitymetrics[ id=" + id + " ]";
    }
}
