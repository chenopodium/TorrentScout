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
 * Created on Dec 22, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.iontorrent.utils.cache;

import com.iontorrent.utils.SystemTool;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author croth
 *
 * Use the TimedPool to cache data for a period of time. After the time has passed, the item in the pool is removed the next time it is accessed
 *  (so there is no need for an extra thread checking periodically). Use put and get to add/remove an item to the pool.
 * It is best used for caching server side objects that take a long time to retrieve/compute.
 * Example: the list of all readable documents of a user is used in several cases (features, sequences, pathways), but retrieving it takes some time.
 * So the method getReadableDocuments in UserService caches the list of readable documents for 24hours per user.
 * If a user uses sharing or creates a new document, this data should be cleared/refreshed. 
 * Use remove to remove an item and clear to empty the entire pool.
 */
public class TimedPool {

	private static final int MAX_SIZE = 1000;
	public static final long MINUTE = 60 * 1000;
	public static final long HOUR = 60 * MINUTE;
	public static final long DAY = HOUR * 24;
	public static final long WEEK = DAY * 7;
	public static final long MONTH = WEEK * 4;

	private static boolean DEBUG = false;
	private static TimedPool pool = new TimedPool();
	private Hashtable hash = new Hashtable();
	private String class_name = "TimedPool";
	private long defaultTime = DAY;

	public TimedPool() {
		this(DAY);
	}
	public TimedPool(long time) {
		this.defaultTime = time;
	}
	public static TimedPool getPool() {
		return pool;
	}
	public long getDefaultTime() {
		return defaultTime;
	}

	public void setDefaultTime(long l) {
		defaultTime = l;
	}
	public synchronized void put(String key, Object value) {
		put(key, value, defaultTime);
	}
    
	public synchronized void clear() {
		hash = new Hashtable();
	}
    
	public synchronized void remove(String key) {
		if (hash == null) return;
		hash.remove(key);
	}
    
	public synchronized void put(String key, Object value, long time) {
		p("adding object "+key+" to timed pool, refresh time is "+time/HOUR+ "hours");
		if (hash == null)
			hash = new Hashtable();
		if (hash.size() % 10 == 0)
			checkPool();
		TimedEntry e = new TimedEntry(key, value, time);
		hash.put(key, e);
	}

	public synchronized Object get(String key) {
		p("getting object "+key+" from timed pool");
		if (hash == null)
			return null;
		TimedEntry e = (TimedEntry) hash.get(key);
		if (e!= null && e.needsRefresh()) {
			p("Entry " + key + " is old, not returning it ");
			hash.remove(e);
			return null;
		}
		
		if (e!= null) return e.getObj();
		else return null;

	}
    
    /**
     * If this method is ever made public for some reason, be sure to
     * make it synchronized!!
     */
	private void checkPool() {
		if (hash == null || hash.size() < 1)
			return;
		if (hash.size() > MAX_SIZE) {
			clear();
		} else if (SystemTool.getFreeBytes() < 1000000) {
			clear();
			err("Less than 1MB free RAM, clearing pools");
		} else {
			// check all entries for time
			for (Iterator iter = hash.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				TimedEntry et = (TimedEntry) hash.get(key);
				if (et.needsRefresh())
					hash.remove(et);
			}
		}
	}
    
	class TimedEntry {
		private long created;
		private Object obj;
		private String key;
		private long refreshTime;

		public TimedEntry(String key, Object obj) {
			this(key, obj, WEEK);
		}
		public TimedEntry(String key, Object obj, long refresh) {
			this.created = System.currentTimeMillis();
			this.key = key;
			this.obj = obj;
			this.refreshTime = refresh;

		}

		public String getKey() {
			return key;
		}

		public Object getObj() {
			return obj;
		}

		public long getRefreshTime() {
			return refreshTime;
		}

		public void setRefreshTime(long l) {
			refreshTime = l;
		}

		public boolean needsRefresh() {
			long time = created + -System.currentTimeMillis();
			boolean r = refreshTime < time;
			p(
				"time since creation: "
					+ time / HOUR
					+ " hours; refresh: "
					+ refreshTime / HOUR
					+ ", needsRefresh: "
					+ r);
			return r;
		}
	}

	//	*****************************************************************
	//	TEST/DEBUG
	//	*****************************************************************
	 /** ================== LOGGING ===================== */
    private void err(String msg, Exception ex) {
        Logger.getLogger(TimedPool.class.getName()).log(Level.SEVERE, msg, ex);
    }

    private static void err(String msg) {
        Logger.getLogger(TimedPool.class.getName()).log(Level.SEVERE, msg);
    }

    private void warn(String msg) {
        Logger.getLogger(TimedPool.class.getName()).log(Level.WARNING, msg);
    }

    private static void p(String msg) {
        System.out.println("TimedPool: " + msg);
        //Logger.getLogger( TimedPool.class.getName()).log(Level.INFO, msg, ex);
    }

}