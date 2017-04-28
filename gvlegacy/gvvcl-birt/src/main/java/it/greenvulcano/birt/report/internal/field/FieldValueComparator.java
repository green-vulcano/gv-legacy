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
package it.greenvulcano.birt.report.internal.field;

import java.util.Comparator;

/**
 * 
 * @version 4.0.0 Mar,2017
 * @author GreenVulcano Developer Team
 *
 */
public class FieldValueComparator implements Comparator<LabelValueBean> {

    /**
     * Return the results of method java.lang.String.compareTo(String anotherString)
     * used with the two parameters passed to this method
     * 
     * @param LabelValueBean o1
     * @param LabelValueBean o2
	 * @return int
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(LabelValueBean o1, LabelValueBean o2)
    {
        return o1.getLabel().compareTo(o2.getLabel());
    }

}
