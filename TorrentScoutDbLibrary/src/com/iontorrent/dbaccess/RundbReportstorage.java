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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author Chantal Roth
 */
@Entity
@Table(name = "rundb_reportstorage")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbReportstorage.findAll", query = "SELECT r FROM RundbReportstorage r")})
public class RundbReportstorage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "\"webServerPath\"")
    private String webServerPath;
    @Basic(optional = false)
    @Column(name = "\"dirPath\"")
    private String dirPath;

    public RundbReportstorage() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbReportstorage.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbReportstorage.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbReportstorage.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RundbReportstorage: " + msg);
        //Logger.getLogger( RundbReportstorage.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbReportstorage(Integer id) {
        this.id = id;
    }

    public RundbReportstorage(Integer id, String name, String webServerPath, String dirPath) {
        this.id = id;
        this.name = name;
        this.webServerPath = webServerPath;
        this.dirPath = dirPath;
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

    public String getWebServerPath() {
        return webServerPath;
    }

    public void setWebServerPath(String webServerPath) {
        this.webServerPath = webServerPath;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
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
        if (!(object instanceof RundbReportstorage)) {
            return false;
        }
        RundbReportstorage other = (RundbReportstorage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbReportstorage[ id=" + id + " ]";
    }
}
