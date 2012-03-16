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
package com.iontorrent.scout.experimentviewer.options;

import com.iontorrent.dbaccess.RundbExperiment;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author Chantal Roth
 */
public class PersistenceHelper {

    private EntityManagerFactory fact;
    private EntityManager entityManager;
    private String URL;

    public PersistenceHelper() {
    }

    public boolean createEntityManager() {
        entityManager = null;
        if (getURL().indexOf(":") < 1) {
            URL = URL+ ":5432";
        }
        if (getURL().indexOf("/") < 5) {
            URL = URL + "/iondb";
        }

        try {
            HashMap newmap = new HashMap<String, String>();
            if (getURL().startsWith("jdbc:postgresql://")) newmap.put("javax.persistence.jdbc.url", getURL());
            else newmap.put("javax.persistence.jdbc.url", "jdbc:postgresql://" + getURL());
            newmap.put("javax.persistence.jdbc.user", "ion");
            newmap.put("javax.persistence.jdbc.password", "ion");
            newmap.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");

            /** <property name="javax.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432/iondb"/>
            <property name="javax.persistence.jdbc.password" value="ion"/>
            <property name="javax.persistence.jdbc.driver" value="org.postgresql.Driver"/>
            <property name="javax.persistence.jdbc.user" value="ion"/>
             * */
            try {
                fact = Persistence.createEntityManagerFactory("TorrentScoutDbLibraryPU", newmap);
            }
            catch (Exception e) {
                warn(e.getMessage());
                return false;
            }
            
            entityManager = fact.createEntityManager(newmap);
            

        } catch (Exception e) {
            warn(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean testURL() {
        p("setAndTestURL: " + getURL());
       
        if (entityManager == null) createEntityManager();
        
        List<RundbExperiment> experiments = null;
        if (getEntityManager() != null) {
            try {
                Query query = getEntityManager().createQuery("SELECT c FROM RundbExperiment c");
                experiments = query.getResultList();

            } catch (Exception e) {
                err(e.getMessage(), e);
                return false;
            }
        }
        else err("Got no entity manager");
        if (experiments == null) {
            p("Got no experiments from "+URL);
            return false;
        } else {
            p("Test ok, got "+experiments.size()+" from " +URL);                    
            return true;
        }
    }

    /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
 //  System.out.println("PersistenceHelper: ERR " + msg);
        Logger.getLogger(PersistenceHelper.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger(PersistenceHelper.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(PersistenceHelper.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
//  System.out.println("PersistenceHelper: " + msg);
        Logger.getLogger(PersistenceHelper.class.getName()).log(Level.INFO, msg);
    }

    /**
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @return the URL
     */
    public String getURL() {
        return URL;
    }

    /**
     * @param URL the URL to set
     */
    public boolean setURL(String URL) {
        this.URL = URL;
        return createEntityManager();
    }
}
