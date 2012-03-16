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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author Chantal Roth
 */
@Entity
@Table(name = "rundb_referencegenome")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbReferencegenome.findAll", query = "SELECT r FROM RundbReferencegenome r")})
public class RundbReferencegenome implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "pretty_name")
    private String prettyName;
    @Basic(optional = false)
    @Column(name = "enabled")
    private boolean enabled;
    @Basic(optional = false)
    @Column(name = "reference_path")
    private String referencePath;
    @Basic(optional = false)
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Basic(optional = false)
    @Column(name = "version")
    private String version;
    @Basic(optional = false)
    @Column(name = "species")
    private String species;
    @Basic(optional = false)
    @Column(name = "source")
    private String source;
    @Basic(optional = false)
    @Column(name = "notes")
    private String notes;
    @Column(name = "status")
    private String status;

    public RundbReferencegenome() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbReferencegenome.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbReferencegenome.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbReferencegenome.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("RundbReferencegenome: " + msg);
        //Logger.getLogger( RundbReferencegenome.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbReferencegenome(Integer id) {
        this.id = id;
    }

    public RundbReferencegenome(Integer id, String name, String prettyName, boolean enabled, String referencePath, Date date, String version, String species, String source, String notes) {
        this.id = id;
        this.name = name;
        this.prettyName = prettyName;
        this.enabled = enabled;
        this.referencePath = referencePath;
        this.date = date;
        this.version = version;
        this.species = species;
        this.source = source;
        this.notes = notes;
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

    public String getPrettyName() {
        return prettyName;
    }

    public void setPrettyName(String prettyName) {
        this.prettyName = prettyName;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReferencePath() {
        return referencePath;
    }

    public void setReferencePath(String referencePath) {
        this.referencePath = referencePath;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        if (!(object instanceof RundbReferencegenome)) {
            return false;
        }
        RundbReferencegenome other = (RundbReferencegenome) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbReferencegenome[ id=" + id + " ]";
    }
}
