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
@Table(name = "rundb_template")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RundbTemplate.findAll", query = "SELECT r FROM RundbTemplate r")})
public class RundbTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "sequence")
    private String sequence;
    @Basic(optional = false)
    @Column(name = "key")
    private String key;
    @Basic(optional = false)
    @Column(name = "comments")
    private String comments;
    @Basic(optional = false)
    @Column(name = "isofficial")
    private boolean isofficial;

    public RundbTemplate() {

    }

/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( RundbTemplate.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( RundbTemplate.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( RundbTemplate.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("RundbTemplate: " + msg);
        //Logger.getLogger( RundbTemplate.class.getName()).log(Level.INFO, msg, ex);
    }

    public RundbTemplate(Integer id) {
        this.id = id;
    }

    public RundbTemplate(Integer id, String name, String sequence, String key, String comments, boolean isofficial) {
        this.id = id;
        this.name = name;
        this.sequence = sequence;
        this.key = key;
        this.comments = comments;
        this.isofficial = isofficial;
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

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean getIsofficial() {
        return isofficial;
    }

    public void setIsofficial(boolean isofficial) {
        this.isofficial = isofficial;
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
        if (!(object instanceof RundbTemplate)) {
            return false;
        }
        RundbTemplate other = (RundbTemplate) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.iontorrent.dbaccess.RundbTemplate[ id=" + id + " ]";
    }
}
