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

package it.greenvulcano.gvesb.datahandling.dbo.utils;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResultSetTransformer {

	private final static Logger LOG = LoggerFactory.getLogger(ResultSetTransformer.class);
	
	
	public static JSONArray toJSONArray(ResultSet resultSet) throws SQLException {
		
		JSONArray queryResult = new JSONArray();
        
		int columns = resultSet.getMetaData().getColumnCount();		        
        List<String> names = new ArrayList<>(columns);		      		         
        for (int i=1; i<=columns; i++) {
        	names.add(resultSet.getMetaData().getColumnLabel(i));
        }
		
        while (resultSet.next()) {
         	           
            JSONObject obj = new JSONObject();
            for (String key : names){
            	           	
                 Object jsonValue = parseValue(resultSet.getObject(key));
            	
            	 if (key.contains(".")){
         	    		String[] hieararchy = key.split("\\.");
         	    		
         	    		JSONObject child = Optional.ofNullable(obj.optJSONObject(hieararchy[0]))
         	    								   .orElse(new JSONObject());
         	    		child.put(hieararchy[1], Optional.ofNullable(jsonValue).orElse(JSONObject.NULL));
         	    		
         	    		obj.put(hieararchy[0], child);
     	    	} else {
     	    		obj.put(key,  Optional.ofNullable(jsonValue).orElse(JSONObject.NULL));
     	    	}
            }		            
            
            queryResult.put(obj);	            
        }
		
		return queryResult;
		
	}
	
	public static Object parseValue(Object object) {
		try {
			if (object instanceof Blob) {
				
				byte[] blob = IOUtils.toByteArray(Blob.class.cast(object).getBinaryStream());
				object = Base64.getEncoder().encodeToString(blob);
			}
		} catch (Exception e) {
			LOG.error("Something goes wrong parsing BLOB field",e);
			object = "Unparsable BLOB";
		}
		
		try {
			
			if (object instanceof Clob) {
				
				byte[] clob = IOUtils.toByteArray(Clob.class.cast(object).getAsciiStream());
				object = new String(clob, "UTF-8");
			}
			
		} catch (Exception e) {
			LOG.error("Something goes wrong parsing CLOB field",e);
			object = "Unparsable CLOB";
		}
		
		if (object instanceof String) {
			
			String value = String.class.cast(object).trim();
			
			try {
				if (value.startsWith("{")  && value.endsWith("}")) {
					return new JSONObject(value);
					
				} else if (value.startsWith("[") && value.endsWith("]"))  {
					return new JSONArray(value);
				}
			} catch (JSONException e) {
				LOG.warn("Something goes wrong parsing "+value+" as JSON");
			}
			
		}	
		
		return object;
		
	}
	
	
	
}
