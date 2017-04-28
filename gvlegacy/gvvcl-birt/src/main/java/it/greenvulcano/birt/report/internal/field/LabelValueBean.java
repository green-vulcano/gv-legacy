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
package  it.greenvulcano.birt.report.internal.field;

/**
 * Uses when need to set or get the label and his value
 *
 * @version 4.0.0 Mar 2017
 * @author GreenVulcano Developer Team
 */
public class LabelValueBean
{
    private String label = null;
    private String value = null;

    /**
     * Constructor. Set label and value
     * 
     * @param label
     * @param value
     */
    public LabelValueBean(String label, String value)
    {
        this.label = label;
        this.value = value;
    }

    /**
     * Get the label
     * 
     * @return String
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Get the label
     * 
     * @return String
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Set the label
     * 
     * @param label
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * Set the value
     * 
     * @param value
     */
    public void setValue(String value)
    {
        this.value = value;
    }
}
