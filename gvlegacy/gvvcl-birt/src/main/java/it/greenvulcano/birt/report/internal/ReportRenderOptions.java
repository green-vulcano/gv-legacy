/*
 * Copyright (c) 2009-2017 GreenVulcano ESB Open Source Project. All rights
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
package  it.greenvulcano.birt.report.internal;

import it.greenvulcano.birt.report.exception.BIRTException;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.w3c.dom.Node;

/**
 *
 * @version 4.0.0 Apr,2017
 * @author GreenVulcano Developer Team
 */
public interface ReportRenderOptions
{
	/**
	 * 
	 * @param node
	 * @throws BIRTException
	 */
    public void init(Node node) throws BIRTException;

    /**
     * 
     * @return String
     */
    public String getType();
    
    /**
     * 
     * @return RenderOption
     */
    public RenderOption getOptions();
}
