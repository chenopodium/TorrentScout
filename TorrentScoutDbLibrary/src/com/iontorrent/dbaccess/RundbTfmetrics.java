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
@Table(name = "rundb_tfmetrics")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbTfmetrics.findAll", query = "SELECT r FROM RundbTfmetrics r")})
public class RundbTfmetrics implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "\"id\"")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "\"name\"")
    private String name;
    @Basic(optional = false)
    @Column(name = "\"matchMismatchHisto\"")
    private String matchMismatchHisto;
    @Basic(optional = false)
    @Column(name = "\"matchMismatchMean\"")
    private double matchMismatchMean;
    @Basic(optional = false)
    @Column(name = "\"matchMismatchMode\"")
    private double matchMismatchMode;
    @Basic(optional = false)
    @Column(name = "\"Q10Histo\"")
    private String q10Histo;
    @Basic(optional = false)
    @Column(name = "\"Q10Mean\"")
    private double q10Mean;
    @Basic(optional = false)
    @Column(name = "\"Q10Mode\"")
    private double q10Mode;
    @Basic(optional = false)
    @Column(name = "\"Q17Histo\"")
    private String q17Histo;
    @Basic(optional = false)
    @Column(name = "\"Q17Mean\"")
    private double q17Mean;
    @Basic(optional = false)
    @Column(name = "\"Q17Mode\"")
    private double q17Mode;
    @Basic(optional = false)
    @Column(name = "\"SysSNR\"")
    private double sysSNR;
    @Basic(optional = false)
    @Column(name = "\"HPSNR\"")
    private String hpsnr;
    @Basic(optional = false)
    @Column(name = "\"corrHPSNR\"")
    private String corrHPSNR;
    @Basic(optional = false)
    @Column(name = "\"HPAccuracy\"")
    private String hPAccuracy;
    @Basic(optional = false)
    @Column(name = "\"rawOverlap\"")
    private String rawOverlap;
    @Basic(optional = false)
    @Column(name = "\"corOverlap\"")
    private String corOverlap;
    @Basic(optional = false)
    @Column(name = "\"hqReadCount\"")
    private double hqReadCount;
    @Basic(optional = false)
    @Column(name = "\"aveHqReadCount\"")
    private double aveHqReadCount;
    @Basic(optional = false)
    @Column(name = "\"Q10ReadCount\"")
    private double q10ReadCount;
    @Basic(optional = false)
    @Column(name = "\"aveQ10ReadCount\"")
    private double aveQ10ReadCount;
    @Basic(optional = false)
    @Column(name = "\"Q17ReadCount\"")
    private double q17ReadCount;
    @Basic(optional = false)
    @Column(name = "\"aveQ17ReadCount\"")
    private double aveQ17ReadCount;
    @Basic(optional = false)
    @Column(name = "\"sequence\"")
    private String sequence;
    @Basic(optional = false)
    @Column(name = "\"keypass\"")
    private double keypass;
    @Basic(optional = false)
    @Column(name = "\"preCorrSNR\"")
    private double preCorrSNR;
    @Basic(optional = false)
    @Column(name = "\"postCorrSNR\"")
    private double postCorrSNR;
    @Basic(optional = false)
    @Column(name = "\"rawIonogram\"")
    private String rawIonogram;
    @Basic(optional = false)
    @Column(name = "\"corrIonogram\"")
    private String corrIonogram;
    @Basic(optional = false)
    @Column(name = "\"CF\"")
    private double cf;
    @Basic(optional = false)
    @Column(name = "\"IE\"")
    private double ie;
    @Basic(optional = false)
    @Column(name = "\"DR\"")
    private double dr;
    @Basic(optional = false)
    @Column(name = "\"error\"")
    private double error;
    @Basic(optional = false)
    @Column(name = "\"number\"")
    private double number;
    @Basic(optional = false)
    @Column(name = "\"aveKeyCount\"")
    private double aveKeyCount;
    @JoinColumn(name = "report_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RundbResults reportId;

    public RundbTfmetrics() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbTfmetrics.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbTfmetrics.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbTfmetrics.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RundbTfmetrics: " + msg);
        //Logger.getLogger( RundbTfmetrics.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbTfmetrics(Integer id) {
        this.id = id;
    }

    public RundbTfmetrics(Integer id, String name, String matchMismatchHisto, double matchMismatchMean, double matchMismatchMode, String q10Histo, double q10Mean, double q10Mode, String q17Histo, double q17Mean, double q17Mode, double sysSNR, String hpsnr, String corrHPSNR, String hPAccuracy, String rawOverlap, String corOverlap, double hqReadCount, double aveHqReadCount, double q10ReadCount, double aveQ10ReadCount, double q17ReadCount, double aveQ17ReadCount, String sequence, double keypass, double preCorrSNR, double postCorrSNR, String rawIonogram, String corrIonogram, double cf, double ie, double dr, double error, double number, double aveKeyCount) {
        this.id = id;
        this.name = name;
        this.matchMismatchHisto = matchMismatchHisto;
        this.matchMismatchMean = matchMismatchMean;
        this.matchMismatchMode = matchMismatchMode;
        this.q10Histo = q10Histo;
        this.q10Mean = q10Mean;
        this.q10Mode = q10Mode;
        this.q17Histo = q17Histo;
        this.q17Mean = q17Mean;
        this.q17Mode = q17Mode;
        this.sysSNR = sysSNR;
        this.hpsnr = hpsnr;
        this.corrHPSNR = corrHPSNR;
        this.hPAccuracy = hPAccuracy;
        this.rawOverlap = rawOverlap;
        this.corOverlap = corOverlap;
        this.hqReadCount = hqReadCount;
        this.aveHqReadCount = aveHqReadCount;
        this.q10ReadCount = q10ReadCount;
        this.aveQ10ReadCount = aveQ10ReadCount;
        this.q17ReadCount = q17ReadCount;
        this.aveQ17ReadCount = aveQ17ReadCount;
        this.sequence = sequence;
        this.keypass = keypass;
        this.preCorrSNR = preCorrSNR;
        this.postCorrSNR = postCorrSNR;
        this.rawIonogram = rawIonogram;
        this.corrIonogram = corrIonogram;
        this.cf = cf;
        this.ie = ie;
        this.dr = dr;
        this.error = error;
        this.number = number;
        this.aveKeyCount = aveKeyCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMatchMismatchHisto() {
        return matchMismatchHisto;
    }

    public void setMatchMismatchHisto(String matchMismatchHisto) {
        this.matchMismatchHisto = matchMismatchHisto;
    }

    public double getMatchMismatchMean() {
        return matchMismatchMean;
    }

    public void setMatchMismatchMean(double matchMismatchMean) {
        this.matchMismatchMean = matchMismatchMean;
    }

    public double getMatchMismatchMode() {
        return matchMismatchMode;
    }

    public void setMatchMismatchMode(double matchMismatchMode) {
        this.matchMismatchMode = matchMismatchMode;
    }

    public String getQ10Histo() {
        return q10Histo;
    }

    public void setQ10Histo(String q10Histo) {
        this.q10Histo = q10Histo;
    }

    public double getQ10Mean() {
        return q10Mean;
    }

    public void setQ10Mean(double q10Mean) {
        this.q10Mean = q10Mean;
    }

    public double getQ10Mode() {
        return q10Mode;
    }

    public void setQ10Mode(double q10Mode) {
        this.q10Mode = q10Mode;
    }

    public String getQ17Histo() {
        return q17Histo;
    }

    public void setQ17Histo(String q17Histo) {
        this.q17Histo = q17Histo;
    }

    public double getQ17Mean() {
        return q17Mean;
    }

    public void setQ17Mean(double q17Mean) {
        this.q17Mean = q17Mean;
    }

    public double getQ17Mode() {
        return q17Mode;
    }

    public void setQ17Mode(double q17Mode) {
        this.q17Mode = q17Mode;
    }

    public double getSysSNR() {
        return sysSNR;
    }

    public void setSysSNR(double sysSNR) {
        this.sysSNR = sysSNR;
    }

    public String getHpsnr() {
        return hpsnr;
    }

    public void setHpsnr(String hpsnr) {
        this.hpsnr = hpsnr;
    }

    public String getCorrHPSNR() {
        return corrHPSNR;
    }

    public void setCorrHPSNR(String corrHPSNR) {
        this.corrHPSNR = corrHPSNR;
    }

    public String getHPAccuracy() {
        return hPAccuracy;
    }

    public void setHPAccuracy(String hPAccuracy) {
        this.hPAccuracy = hPAccuracy;
    }

    public String getRawOverlap() {
        return rawOverlap;
    }

    public void setRawOverlap(String rawOverlap) {
        this.rawOverlap = rawOverlap;
    }

    public String getCorOverlap() {
        return corOverlap;
    }

    public void setCorOverlap(String corOverlap) {
        this.corOverlap = corOverlap;
    }

    public double getHqReadCount() {
        return hqReadCount;
    }

    public void setHqReadCount(double hqReadCount) {
        this.hqReadCount = hqReadCount;
    }

    public double getAveHqReadCount() {
        return aveHqReadCount;
    }

    public void setAveHqReadCount(double aveHqReadCount) {
        this.aveHqReadCount = aveHqReadCount;
    }

    public double getQ10ReadCount() {
        return q10ReadCount;
    }

    public void setQ10ReadCount(double q10ReadCount) {
        this.q10ReadCount = q10ReadCount;
    }

    public double getAveQ10ReadCount() {
        return aveQ10ReadCount;
    }

    public void setAveQ10ReadCount(double aveQ10ReadCount) {
        this.aveQ10ReadCount = aveQ10ReadCount;
    }

    public double getQ17ReadCount() {
        return q17ReadCount;
    }

    public void setQ17ReadCount(double q17ReadCount) {
        this.q17ReadCount = q17ReadCount;
    }

    public double getAveQ17ReadCount() {
        return aveQ17ReadCount;
    }

    public void setAveQ17ReadCount(double aveQ17ReadCount) {
        this.aveQ17ReadCount = aveQ17ReadCount;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public double getKeypass() {
        return keypass;
    }

    public void setKeypass(double keypass) {
        this.keypass = keypass;
    }

    public double getPreCorrSNR() {
        return preCorrSNR;
    }

    public void setPreCorrSNR(double preCorrSNR) {
        this.preCorrSNR = preCorrSNR;
    }

    public double getPostCorrSNR() {
        return postCorrSNR;
    }

    public void setPostCorrSNR(double postCorrSNR) {
        this.postCorrSNR = postCorrSNR;
    }

    public String getRawIonogram() {
        return rawIonogram;
    }

    public void setRawIonogram(String rawIonogram) {
        this.rawIonogram = rawIonogram;
    }

    public String getCorrIonogram() {
        return corrIonogram;
    }

    public void setCorrIonogram(String corrIonogram) {
        this.corrIonogram = corrIonogram;
    }

    public double getCf() {
        return cf;
    }

    public void setCf(double cf) {
        this.cf = cf;
    }

    public double getIe() {
        return ie;
    }

    public void setIe(double ie) {
        this.ie = ie;
    }

    public double getDr() {
        return dr;
    }

    public void setDr(double dr) {
        this.dr = dr;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
    }

    public double getAveKeyCount() {
        return aveKeyCount;
    }

    public void setAveKeyCount(double aveKeyCount) {
        this.aveKeyCount = aveKeyCount;
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
        if (!(object instanceof RundbTfmetrics)) {
            return false;
        }
        RundbTfmetrics other = (RundbTfmetrics) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbTfmetrics[ id=" + id + " ]";
    }
}
