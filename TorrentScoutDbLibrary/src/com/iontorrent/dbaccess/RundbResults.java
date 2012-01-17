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
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
/**
 *
 * @author Chantal Roth
 */
@Entity
@Table(name = "rundb_results")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbResults.findAll", query = "SELECT r FROM RundbResults r")})
public class RundbResults implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "\"resultsName\"")
    private String resultsName;
    @Basic(optional = false)
    @Column(name = "\"timeStamp\"")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;
    @Basic(optional = false)
    @Column(name = "\"sffLink\"")
    private String sffLink;
    @Basic(optional = false)
    @Column(name = "\"fastqLink\"")
    private String fastqLink;
    @Basic(optional = false)
    @Column(name = "\"reportLink\"")
    private String reportLink;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @Column(name = "\"tfSffLink\"")
    private String tfSffLink;
    @Basic(optional = false)
    @Column(name = "\"tfFastq\"")
    private String tfFastq;
    @Basic(optional = false)
    @Column(name = "log")
    private String log;
    @Basic(optional = false)
    @Column(name = "\"processedCycles\"")
    private int processedCycles;
    @Basic(optional = false)
    @Column(name = "\"framesProcessed\"")
    private int framesProcessed;
    @Basic(optional = false)
    @Column(name = "\"timeToComplete\"")
    private String timeToComplete;
    @JoinColumn(name = "experiment_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RundbExperiment rundbExperiment;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reportId")
    private Collection<RundbTfmetrics> rundbTfmetricsCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reportId")
    private Collection<RundbLibmetrics> rundbLibmetricsCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reportId")
    private Collection<RundbQualitymetrics> rundbQualitymetricsCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reportId")
    private Collection<RundbAnalysismetrics> rundbAnalysismetricsCollection;
      
    public RundbResults() {
    }

    public RundbResults(Integer id) {
        this.id = id;
    }

    public RundbResults(Integer id, String resultsName, Date timeStamp, String sffLink, String fastqLink, String reportLink, String status, String tfSffLink, String tfFastq, String log,  int processedCycles, int framesProcessed, String timeToComplete) {
        this.id = id;
        this.resultsName = resultsName;
        this.timeStamp = timeStamp;
        this.sffLink = sffLink;
        this.fastqLink = fastqLink;
        this.reportLink = reportLink;
        this.status = status;
        this.tfSffLink = tfSffLink;
        this.tfFastq = tfFastq;
        this.log = log;
        this.processedCycles = processedCycles;
        this.framesProcessed = framesProcessed;
        this.timeToComplete = timeToComplete;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getResultsName() {
        return resultsName;
    }

    public void setResultsName(String resultsName) {
        this.resultsName = resultsName;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSffLink() {
        return sffLink;
    }

    public void setSffLink(String sffLink) {
        this.sffLink = sffLink;
    }

    public String getFastqLink() {
        return fastqLink;
    }

    public void setFastqLink(String fastqLink) {
        this.fastqLink = fastqLink;
    }

    public String getReportLink() {
        return reportLink;
    }

    public void setReportLink(String reportLink) {
        this.reportLink = reportLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTfSffLink() {
        return tfSffLink;
    }

    public void setTfSffLink(String tfSffLink) {
        this.tfSffLink = tfSffLink;
    }

    public String getTfFastq() {
        return tfFastq;
    }

    public void setTfFastq(String tfFastq) {
        this.tfFastq = tfFastq;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    

    public int getProcessedCycles() {
        return processedCycles;
    }

    public void setProcessedCycles(int processedCycles) {
        this.processedCycles = processedCycles;
    }

    public int getFramesProcessed() {
        return framesProcessed;
    }

    public void setFramesProcessed(int framesProcessed) {
        this.framesProcessed = framesProcessed;
    }

    public String getTimeToComplete() {
        return timeToComplete;
    }

    public void setTimeToComplete(String timeToComplete) {
        this.timeToComplete = timeToComplete;
    }


    public RundbExperiment getRundbExperiment() {
        return rundbExperiment;
    }

    public void setRundbExperiment(RundbExperiment rundbExperiment) {
        this.rundbExperiment = rundbExperiment;
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
        if (!(object instanceof RundbResults)) {
            return false;
        }
        RundbResults other = (RundbResults) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.scout.dbaccess.RundbResults[id=" + id + "]";
    }

    /**
     * @return the rundbTfmetricsCollection
     */
    public Collection<RundbTfmetrics> getRundbTfmetricsCollection() {
        return rundbTfmetricsCollection;
    }

    /**
     * @param rundbTfmetricsCollection the rundbTfmetricsCollection to set
     */
    public void setRundbTfmetricsCollection(Collection<RundbTfmetrics> rundbTfmetricsCollection) {
        this.rundbTfmetricsCollection = rundbTfmetricsCollection;
    }

    /**
     * @return the rundbLibmetricsCollection
     */
    public Collection<RundbLibmetrics> getRundbLibmetricsCollection() {
        return rundbLibmetricsCollection;
    }

    /**
     * @param rundbLibmetricsCollection the rundbLibmetricsCollection to set
     */
    public void setRundbLibmetricsCollection(Collection<RundbLibmetrics> rundbLibmetricsCollection) {
        this.rundbLibmetricsCollection = rundbLibmetricsCollection;
    }

    /**
     * @return the rundbQualitymetricsCollection
     */
    public Collection<RundbQualitymetrics> getRundbQualitymetricsCollection() {
        return rundbQualitymetricsCollection;
    }

    /**
     * @param rundbQualitymetricsCollection the rundbQualitymetricsCollection to set
     */
    public void setRundbQualitymetricsCollection(Collection<RundbQualitymetrics> rundbQualitymetricsCollection) {
        this.rundbQualitymetricsCollection = rundbQualitymetricsCollection;
    }

    /**
     * @return the rundbAnalysismetricsCollection
     */
    public Collection<RundbAnalysismetrics> getRundbAnalysismetricsCollection() {
        return rundbAnalysismetricsCollection;
    }

    /**
     * @param rundbAnalysismetricsCollection the rundbAnalysismetricsCollection to set
     */
    public void setRundbAnalysismetricsCollection(Collection<RundbAnalysismetrics> rundbAnalysismetricsCollection) {
        this.rundbAnalysismetricsCollection = rundbAnalysismetricsCollection;
    }

}
