/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.debug;

import it.greenvulcano.gvesb.core.debug.GVDebugger;
import it.greenvulcano.gvesb.core.debug.GVDebugger.DebugCommand;
import it.greenvulcano.gvesb.core.debug.GVDebugger.DebugKey;
import it.greenvulcano.gvesb.core.debug.model.DebuggerObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebuggerServlet extends HttpServlet {
    /**
     * 
     */
    private static final long   serialVersionUID  = 1L;
    
    private final static Logger LOG = LoggerFactory.getLogger(DebuggerServlet.class);
    private final static Set<String> KEYS = Stream.of(DebugKey.values())
	                                                      .map(DebugKey::name)
	                                                      .collect(Collectors.toSet());
    
    
    private GVDebugger gvDebugger;
    
    public void setGvDebugger(GVDebugger gvDebugger) {
		this.gvDebugger = gvDebugger;
	}

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
        	
        	StringBuffer sb = new StringBuffer();
        	dump(request, sb);
        	LOG.debug(sb.toString());
        	
            DebugCommand debugOperation = Optional.ofNullable(request.getParameter("debugOperation"))
                                                  .map(DebugCommand::valueOf)
                                                  .orElseThrow(IllegalArgumentException::new);
            
            PrintWriter writer = response.getWriter();
                        
            Map<DebugKey, String> params =  request.getParameterMap()
                                                   .keySet()
                                                   .stream()
                                                   .filter(KEYS::contains)
                                                   .map(DebugKey::valueOf)
                                                   .collect(Collectors.toMap( Function.identity(), k -> request.getParameter(k.name())));
            
            DebuggerObject dObj = gvDebugger.processCommand(debugOperation, params);
                        
            if (dObj == null) {
                dObj = DebuggerObject.FAIL_DEBUGGER_OBJECT;
            }
            String debugOperationResponse = dObj.toXML();
            
            LOG.debug("Debug operation response: "+debugOperationResponse);
            writer.println(debugOperationResponse);
        } catch (IllegalArgumentException e) {
        	LOG.error("Fail to process debug operation: missing or invalid value for parameter debugOperation");
        	response.getWriter().println("Missing or invalid value for parameter debugOperation");
        } catch (Exception e) {
        	LOG.error("Fail to process debug operation", e);
            throw new ServletException(e);
        }
    }
   
    private void dump(HttpServletRequest request, StringBuffer log) throws IOException {
        String hN;

        log.append("-- DUMP HttpServletRequest START").append("\n");
        log.append("Method             : ").append(request.getMethod()).append("\n");
        log.append("RequestedSessionId : ").append(request.getRequestedSessionId()).append("\n");
        log.append("Scheme             : ").append(request.getScheme()).append("\n");
        log.append("IsSecure           : ").append(request.isSecure()).append("\n");
        log.append("Protocol           : ").append(request.getProtocol()).append("\n");
        log.append("ContextPath        : ").append(request.getContextPath()).append("\n");
        log.append("PathInfo           : ").append(request.getPathInfo()).append("\n");
        log.append("QueryString        : ").append(request.getQueryString()).append("\n");
        log.append("RequestURI         : ").append(request.getRequestURI()).append("\n");
        log.append("RequestURL         : ").append(request.getRequestURL()).append("\n");
        log.append("ContentType        : ").append(request.getContentType()).append("\n");
        log.append("ContentLength      : ").append(request.getContentLength()).append("\n");
        log.append("CharacterEncoding  : ").append(request.getCharacterEncoding()).append("\n");
        
        log.append("---- Headers START\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            hN = headerNames.nextElement();
            log.append("[" + hN + "]=");
            Enumeration<String> headers = request.getHeaders(hN);
            while (headers.hasMoreElements()) {
                log.append("[" + headers.nextElement() + "]");
            }
            log.append("\n");
        }
        log.append("---- Headers END\n");
        
        log.append("---- Body START\n");
        log.append(IOUtils.toString(request.getInputStream())).append("\n");
        log.append("---- Body END\n");
       
        log.append("-- DUMP HttpServletRequest END \n");
    }    
  
}
