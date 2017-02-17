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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.util.metadata.PropertiesHandler;

/**
 * Keep track of read message's UID in a file.
 * 
 * @version 4.0.0 - Feb 2017
 * @author GreenVulcano Developer Team
 */
public class FileUIDCacheManager implements UIDCacheManager
{
    private static final Logger          logger                   = LoggerFactory.getLogger(FileUIDCacheManager.class);

    private static final String          POP_CACHE_FOLDER_NAME    = "gv.pop.uidcache.folder";
    private static final String          POP_CACHE_READ_ALWAYS    = "gv.pop.uidcache.folder.read.always";

    private HashMap<String, Set<String>> popCacheMap              = new HashMap<String, Set<String>>();
    private String                       popCacheFolder           = System.getProperty(POP_CACHE_FOLDER_NAME,
                                                                          System.getProperty("java.io.tmpdir")
                                                                                  + File.separator + "GV_POP_CACHE");
    private boolean                      popCacheFolderReadAlways = Boolean.getBoolean(POP_CACHE_READ_ALWAYS);


    /**
     * 
     * @param key
     * 			the key of the UIDCache
     * @return UIDCache
     */
    @Override
    public synchronized UIDCache getUIDCache(String key) throws Exception
    {
        return new FileUIDCache(key, getUIDCacheInt(key), this);
    }

    /**
     * creates the POP cache folder
     */
    public FileUIDCacheManager()
    {
        try {
            popCacheFolder = PropertiesHandler.expand(popCacheFolder);
            File popF = new File(popCacheFolder);
            if (!popF.isDirectory()) {
                if (!popF.mkdirs()) {
                    logger.error("Unable to create POP cache folder [" + popCacheFolder + "]");
                }
            }
        }
        catch (Exception exc) {
            logger.error("Error initializing FileUIDCacheManager", exc);
        }
    }

    /**
     * Retrieve UIDCache from a file
     * 
     * @param key
     * 			the key of the UIDCache
     * @return Set<String>
     * 			returns a Set<String> of UIDCache 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Set<String> getUIDCacheInt(String key) throws Exception
    {
        Set<String> uidCache = popCacheMap.get(key);

        if ((uidCache == null) || popCacheFolderReadAlways) {
            uidCache = null;
            String fileName = popCacheFolder + File.separator + key + ".uidcache";
            File uidCacheFile = new File(fileName);
            if (uidCacheFile.canRead()) {
                ObjectInputStream in = null;
                try {
                    logger.debug("Reading POP Cache [" + key + "] from file [" + fileName + "]");
                    in = new ObjectInputStream(new FileInputStream(uidCacheFile));
                    uidCache = (HashSet<String>) in.readObject();
                    popCacheMap.put(key, uidCache);
                }
                catch (Exception exc) {
                    logger.error("Error reading POP cache file [" + fileName + "]", exc);
                    // must rethrow Exception???
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Exception exc2) {
                            // do nothing
                        }
                    }
                }
            }
            else {
                uidCache = new HashSet<String>();
                popCacheMap.put(key, uidCache);
                updateUIDCacheInt(key);
            }
        }

        return uidCache;
    }

    /**
     * Update UIDCache data in a file
     * 
     * @param key
     * 			the key of the UIDCache
     * @throws Exception
     */
    void updateUIDCacheInt(String key) throws Exception
    {
        Set<String> uidCache = popCacheMap.get(key);

        if (uidCache != null) {
            String fileName = popCacheFolder + File.separator + key + ".uidcache";
            File uidCacheFile = new File(fileName);
            ObjectOutputStream out = null;
            try {
            	//TODO:
                logger.debug("Writing POP Cache [" + key + "] on file [" + fileName + "]");
                out = new ObjectOutputStream(new FileOutputStream(uidCacheFile));
                out.writeObject(uidCache);
                out.flush();
            }
            catch (Exception exc) {
                logger.error("Error writing POP cache file [" + fileName + "]", exc);
                // must rethrow Exception???
            }
            finally {
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (Exception exc2) {
                        // do nothing
                    }
                }
            }
        }
    }
}
