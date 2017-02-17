/*******************************************************************************
 * Copyright (c) 2009, 2017 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package it.greenvulcano.gvesb.virtual.pop.uidcache;


/**
 * UID Cache.
 *
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 */
public interface UIDCache
{
	/**
	 * Verify if the uid is contained in cache
	 * 
	 * @param uid 
	 * 			idetified for cache
	 * @return boolean 
	 * 			return true if the cache contains the uid
	 */
    public boolean contains(String uid);

    /**
     * Add an uid in a cache
     * 
     * @param uid
     * 			identified for cache
     */
    public void add(String uid);
    
    /**
     * Remove an uid in a cache
     * 
     * @param uid
     * 			identified for cache
     * @return boolean
     * 			return true if an uid is removed from cache
     */
    public boolean remove(String uid);
}
