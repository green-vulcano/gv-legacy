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
package it.greenvulcano.gvesb.gvdp.impl;

import javax.jms.Session;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author nunzio
 *
 */
public class JMSBytesMessageDataProvider extends AbstractJMSMessageDataProvider
{
    /**
     * This implementation of data provider needs to have JMS {#link Session}
     * object set as property. When setting session, a new {#link BytesMessage}
     * is created.
     *
     * @see it.greenvulcano.gvesb.gvdp.impl.AbstractJMSMessageDataProvider#setContext(java.lang.Object)
     */
    @Override
    public void setContext(Object object) throws Exception
    {
        super.setContext(object);
        currentMessage = ((Session) object).createBytesMessage();
        logger.debug("Created a new JMS message: " + currentMessage);
    }

}
