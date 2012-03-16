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
@Table(name = "rundb_rig")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbRig.findAll", query = "SELECT r FROM RundbRig r")})
public class RundbRig implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "comments")
    private String comments;
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RundbLocation locationId;

    public RundbRig() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbRig.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbRig.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbRig.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("RundbRig: " + msg);
        //Logger.getLogger( RundbRig.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbRig(String name) {
        this.name = name;
    }

    public RundbRig(String name, String comments) {
        this.name = name;
        this.comments = comments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public RundbLocation getLocationId() {
        return locationId;
    }

    public void setLocationId(RundbLocation locationId) {
        this.locationId = locationId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RundbRig)) {
            return false;
        }
        RundbRig other = (RundbRig) object;
        if ((this.name == null && other.name != null) || (this.name != null && !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbRig[ name=" + name + " ]";
    }
}
