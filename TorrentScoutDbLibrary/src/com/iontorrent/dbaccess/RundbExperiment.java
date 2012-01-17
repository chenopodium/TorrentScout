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
@Table(name = "rundb_experiment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbExperiment.findAll", query = "SELECT r FROM RundbExperiment r")})
public class RundbExperiment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "\"id\"")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "\"expDir\"")
    private String expDir;
    @Basic(optional = false)
    @Column(name = "\"expName\"")
    private String expName;
    @Basic(optional = false)
    @Column(name = "\"pgmName\"")
    private String pgmName;
    @Basic(optional = false)
    @Column(name = "log")
    private String log;
    @Basic(optional = false)
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Basic(optional = false)
    @Column(name = "storage_options")
    private String storageOptions;
    @Column(name = "project")
    private String project;
    @Column(name = "sample")
    private String sample;
    @Column(name = "library")
    private String library;
    @Column(name = "notes")
    private String notes;
    @Basic(optional = false)
    @Column(name = "\"chipBarcode\"")
    private String chipBarcode;
    @Basic(optional = false)
    @Column(name = "\"seqKitBarcode\"")
    private String seqKitBarcode;
    @Basic(optional = false)
    @Column(name = "\"reagentBarcode\"")
    private String reagentBarcode;
    @Basic(optional = false)
    @Column(name = "\"autoAnalyze\"")
    private boolean autoAnalyze;
    @Basic(optional = false)
    @Column(name = "\"usePreBeadfind\"")
    private boolean usePreBeadfind;
    @Basic(optional = false)
    @Column(name = "\"chipType\"")
    private String chipType;
    @Basic(optional = false)
    @Column(name = "cycles")
    private int cycles;
    @Basic(optional = false)
    @Column(name = "\"expCompInfo\"")
    private String expCompInfo;
    @Basic(optional = false)
    @Column(name = "\"baselineRun\"")
    private boolean baselineRun;
    @Basic(optional = false)
    @Column(name = "\"flowsInOrder\"")
    private String flowsInOrder;
    @Basic(optional = false)
    @Column(name = "star")
    private boolean star;
    @Basic(optional = false)
    @Column(name = "\"ftpStatus\"")
    private String ftpStatus;
    @Basic(optional = false)
    @Column(name = "\"libraryKey\"")
    private String libraryKey;
    @Column(name = "\"storageHost\"")
    private String storageHost;
    @Basic(optional = false)
    @Column(name = "flows")
    private int flows;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "rundbExperiment")
    private Collection<RundbResults> rundbResultsCollection;

    public RundbExperiment() {
    }

    public RundbExperiment(Integer id) {
        this.id = id;
    }

    public RundbExperiment(Integer id, String expDir, String expName, String pgmName, String log, Date date, String storageOptions, String chipBarcode, String seqKitBarcode, String reagentBarcode, boolean autoAnalyze, boolean usePreBeadfind, String chipType, int cycles, String expCompInfo, boolean baselineRun, String flowsInOrder, boolean star, String ftpStatus, String libraryKey, int flows) {
        this.id = id;
        this.expDir = expDir;
        this.expName = expName;
        this.pgmName = pgmName;
        this.log = log;
        
        this.date = date;
        this.storageOptions = storageOptions;
        this.chipBarcode = chipBarcode;
        this.seqKitBarcode = seqKitBarcode;
        this.reagentBarcode = reagentBarcode;
        this.autoAnalyze = autoAnalyze;
        this.usePreBeadfind = usePreBeadfind;
        this.chipType = chipType;
        this.cycles = cycles;
        this.expCompInfo = expCompInfo;
        this.baselineRun = baselineRun;
        this.flowsInOrder = flowsInOrder;
        this.star = star;
        this.ftpStatus = ftpStatus;
        this.libraryKey = libraryKey;
        this.flows = flows;
    }

  
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExpDir() {
        return expDir;
    }

    public void setExpDir(String expDir) {
        this.expDir = expDir;
    }

    public String getExpName() {
        return expName;
    }

    public void setExpName(String expName) {
        this.expName = expName;
    }

    public String getPgmName() {
        return pgmName;
    }

    public void setPgmName(String pgmName) {
        this.pgmName = pgmName;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStorageOptions() {
        return storageOptions;
    }

    public void setStorageOptions(String storageOptions) {
        this.storageOptions = storageOptions;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getChipBarcode() {
        return chipBarcode;
    }

    public void setChipBarcode(String chipBarcode) {
        this.chipBarcode = chipBarcode;
    }

    public String getSeqKitBarcode() {
        return seqKitBarcode;
    }

    public void setSeqKitBarcode(String seqKitBarcode) {
        this.seqKitBarcode = seqKitBarcode;
    }

    public String getReagentBarcode() {
        return reagentBarcode;
    }

    public void setReagentBarcode(String reagentBarcode) {
        this.reagentBarcode = reagentBarcode;
    }

    public boolean getAutoAnalyze() {
        return autoAnalyze;
    }

    public void setAutoAnalyze(boolean autoAnalyze) {
        this.autoAnalyze = autoAnalyze;
    }

    public boolean getUsePreBeadfind() {
        return usePreBeadfind;
    }

    public void setUsePreBeadfind(boolean usePreBeadfind) {
        this.usePreBeadfind = usePreBeadfind;
    }

    public String getChipType() {
        return chipType;
    }

    public void setChipType(String chipType) {
        this.chipType = chipType;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public String getExpCompInfo() {
        return expCompInfo;
    }

    public void setExpCompInfo(String expCompInfo) {
        this.expCompInfo = expCompInfo;
    }

    public boolean getBaselineRun() {
        return baselineRun;
    }

    public void setBaselineRun(boolean baselineRun) {
        this.baselineRun = baselineRun;
    }

    public String getFlowsInOrder() {
        return flowsInOrder;
    }

    public void setFlowsInOrder(String flowsInOrder) {
        this.flowsInOrder = flowsInOrder;
    }

    public boolean getStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public String getFtpStatus() {
        return ftpStatus;
    }

    public void setFtpStatus(String ftpStatus) {
        this.ftpStatus = ftpStatus;
    }

    public String getLibraryKey() {
        return libraryKey;
    }

    public void setLibraryKey(String libraryKey) {
        this.libraryKey = libraryKey;
    }

    public String getStorageHost() {
        return storageHost;
    }

    public void setStorageHost(String storageHost) {
        this.storageHost = storageHost;
    }

    public int getFlows() {
        return flows;
    }

    public void setFlows(int flows) {
        this.flows = flows;
    }

    public Collection<RundbResults> getRundbResultsCollection() {
        return rundbResultsCollection;
    }

    public void setRundbResultsCollection(Collection<RundbResults> rundbResultsCollection) {
        this.rundbResultsCollection = rundbResultsCollection;
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
        if (!(object instanceof RundbExperiment)) {
            return false;
        }
        RundbExperiment other = (RundbExperiment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.scout.dbaccess.RundbExperiment[id=" + id + "]";
    }

}
