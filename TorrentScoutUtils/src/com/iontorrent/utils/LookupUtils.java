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

package com.iontorrent.utils;


import java.util.logging.Level;
import java.util.logging.Logger;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.InstanceContent.Convertor;

/**
 *
 * @author Chantal Roth
 *
 * @author Carlos Hoces
 */
public final class LookupUtils {

    private static LookupUtils instance;
    private static final Map<Class<?>, JPAbstractLookup> LOOKUP_PS = new HashMap<Class<?>, JPAbstractLookup> ();

    private LookupUtils() {
    }

    /**
     * It will return this singleton instance
     * @return
     */
    public synchronized static LookupUtils getInstance() {
        if (instance == null) {
            instance = new LookupUtils();
        }
        return instance;
    }

    /**
     * Returns the InstanceContent defined for param clazz.
     * Classes may use this method to gain access to the InstanceContent associated to clazz.
     * @param <T>
     * @param clazz
     * @return
     */
    public synchronized static <T> InstanceContent getPublisher(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }
        setPublisher(clazz);
        return LOOKUP_PS.get(clazz).getContent();
    }

    /**
     * It will return a Lookup.Result for data stored in param clazz, and use the defined lookupListener to retrieve it.
     * It will throw an IllegalArgumentException in case either param is null.
     * @param <T>
     * @param clazz
     * @param lookupListener
     * @return
     */
    public synchronized static <T> Lookup.Result<T> getSubscriber(Class<T> clazz, LookupListener lookupListener) {
        if (lookupListener == null || clazz == null) {
            throw new IllegalArgumentException("params cannot be null");
        }
        setPublisher(clazz);
        final Lookup.Result<T> result = LOOKUP_PS.get(clazz).lookupResult(clazz);
        result.addLookupListener(lookupListener);
        return result;
    }

    /**
     * Allows to publish data to an InstanceContent.
     * It will throw an IllegalArgumentException in case either param is null, or no InstanceContent found
     * @param <T>
     * @param content
     * @param dataInstance
     */
    public synchronized static <T> void publish(InstanceContent content, T dataInstance) {
        publish(content, dataInstance, null);
    }

    /**
     * Allows to publish data to an InstanceContent, using a Convertor
     * It will throw an IllegalArgumentException in case either param is null, or no InstanceContent found
     * @param <T>
     * @param <R>
     * @param content
     * @param dataInstance
     * @param convertor
     */
    public synchronized static <T, R> void publish(InstanceContent content, T dataInstance, Convertor<T, R> convertor) {
        checkContent(content, dataInstance);
        
        content.set(Collections.singleton(dataInstance), convertor);
    }

    /**
     * It will create a lookup and an InstanceContent associated to the param clazz.
     * Classes may get either the Lookup or the InstanceContent by using getClassLookup and getPublisher methods.
     * @param <T>
     * @param clazz
     */
    public synchronized static <T> void setPublisher(Class<T> clazz) {
        if (!LOOKUP_PS.containsKey(clazz)) {
            LOOKUP_PS.put(clazz, new JPAbstractLookup(new InstanceContent()));
        }
    }

    /**
     * Returns the Lookup associated to param clazz.
     * If no lookup was defined for that class, it will throw an IllegalArgumentException.
     * @param <T>
     * @param clazz
     * @return
     */
    public synchronized static <T> Lookup getClassLookup(Class<T> clazz) {
        if (!LOOKUP_PS.containsKey(clazz)) {
            throw new IllegalArgumentException("no Lookup defined for param clazz");
        }
        return LOOKUP_PS.get(clazz);
    }

    /**
     * This method returns a Lookup.Result for class type T, defined via param resultClass.
     * A lookup must be supplied at param lookup. If lookup or resultClass params are set to null, it will throw an IllegalArgumentException.
     * Param resultClass is the Lookup.Result class that will hold data.
     * Param lookupListener is a listener defined to collect Lookup.Result data
     * 
     * @param <T> 
     * @param lookup
     * @param resultClass
     * @param lookupListener 
     * @return
     * @return: a Lookup.Result for class T
     *
     */
    public synchronized static <T> Lookup.Result<T> getLookupResult(Lookup lookup, Class<T> resultClass, LookupListener lookupListener) {
        if (lookup == null || resultClass == null) {
            throw new IllegalArgumentException("lookup or resultClass params must not be null");
        }
        final Lookup.Result<T> result = lookup.lookupResult(resultClass);
        result.addLookupListener(lookupListener);
        return result;
    }

    /**
     * This will return either the first object found which implements the class passed as a parameter, or null if not anyone found.
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> T getInstance(Class<T> clazz) {
        return Lookup.getDefault().lookup(clazz);
    }

    /**
     * It will return a collection of all instances of clazz
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> Collection<? extends T> getAlIinstances(Class<T> clazz) {
        return Lookup.getDefault().lookupAll(clazz);
    }

    private static <T> void checkContent(InstanceContent content, T dataInstance) {
        if (content == null) {
            throw new IllegalArgumentException("InstanceContent null");
        }
        if (dataInstance == null) {
            throw new IllegalArgumentException("dataInstance cannot be null");
        }
        boolean found = false;
      //  final String className = dataInstance.getClass().getName();
        final Iterator<Entry<Class<?>, JPAbstractLookup>> checkLookupMap = LOOKUP_PS.entrySet().iterator();
        checkLoop:
        while (checkLookupMap.hasNext()) {
            final Entry<Class<?>, JPAbstractLookup> entry = checkLookupMap.next();
            if (entry.getValue().getContent().equals(content)) {
//                if (!entry.getKey().getName().equals(className)) {
//                    throw new IllegalStateException("dataInstance and data class do not match");
//                }
                found = true;
                break checkLoop;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("InstanceContent not found");
        }
    }

    private static class JPAbstractLookup extends AbstractLookup {

        private static final long serialVersionUID = 5429372940135351125L;
        private transient final InstanceContent content;

        public JPAbstractLookup(InstanceContent content) {
            super(content);
            this.content = content;
        }

        /**
         * @return the content
         */
        public InstanceContent getContent() {
            return content;
        }
    }


/** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger( LookupUtils.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private void err(String msg) {
        Logger.getLogger( LookupUtils.class.getName()).log(Level.SEVERE, msg);
    }

     private void warn(String msg) {
        Logger.getLogger( LookupUtils.class.getName()).log(Level.WARNING, msg);
    }

    private void p(String msg) {
        System.out.println("LookupUtils: " + msg);
        //Logger.getLogger( LookupUtils.class.getName()).log(Level.INFO, msg, ex);
    }
}
