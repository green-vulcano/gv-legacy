/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for UID Cache.
 * 
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 */
public class FileUIDCache implements UIDCache
{
    private static final Logger logger  = LoggerFactory.getLogger(FileUIDCache.class);

    private String              key     = null;
    private Set<String>         cache   = null;
    private FileUIDCacheManager manager = null;


    /**
     * 
     * @param key
     * 			the key of the UIDCache
     * @param cache
     * 			A Set<String> for a cache
     * @param manager
     * 			Instance of FileUIDCacheManager
     */	
    public FileUIDCache(String key, Set<String> cache, FileUIDCacheManager manager)
    {
        this.key = key;
        this.cache = cache;
        this.manager = manager;
    }

    /**
     *
     */
    @Override
    public boolean contains(String uid)
    {
        boolean found = cache.contains(uid);
        if (found) {
            logger.debug("Found UID [" + uid + "] in Cache [" + key + "]");
        }
        return found;
    }

    /**
     *
     */
    @Override
    public void add(String uid)
    {
        synchronized (cache) {
            logger.debug("Writing UID [" + uid + "] in Cache [" + key + "]");
            cache.add(uid);
            try {
                manager.updateUIDCacheInt(key);
            }
            catch (Exception exc) {
                // TODO: handle exception
                logger.error("Error saving Cache [" + key + "] data.", exc);
            }
        }
    }
    
    /**
     *
     */
    public boolean remove(String uid)
    {
        synchronized (cache) {
            logger.debug("Deleting UID [" + uid + "] in Cache [" + key + "]");
            boolean res = cache.remove(uid);
            try {
                manager.updateUIDCacheInt(key);
            }
            catch (Exception exc) {
                // TODO: handle exception
                logger.error("Error saving Cache [" + key + "] data.", exc);
            }
            return res;
        }
    }
}
