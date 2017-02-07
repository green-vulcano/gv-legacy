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
package it.greenvulcano.gvesb.adapter.http.utils;

import it.greenvulcano.gvesb.internal.GVInternalException;

/**
 * <code>AdapterHttpException</code> is the root exception of <i>AdapterHttp</i>
 * module.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class AdapterHttpException extends GVInternalException
{
    private static final long serialVersionUID = 1520444504289108551L;

    /**
     * Creates a new <code>AdapterHttpException</code> with error code
     * identified by <code>errorId</code> and no cause.
     *
     * @param id
     *        ErrorId associated to the exception
     */
    public AdapterHttpException(String id)
    {
        super(id);
    }

    /**
     * Creates a new <code>AdapterHttpException</code> with error code
     * identified by <code>id</code> and a cause.
     *
     * @param id
     *        ErrorId associated to the exception
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public AdapterHttpException(String id, Throwable cause)
    {
        super(id, cause);
    }

    /**
     * Creates a new <code>AdapterHttpException</code> with error code
     * identified by <code>id</code> and no cause. <code>params</code> is used
     * to complete the error message.
     *
     * @param id
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     */
    public AdapterHttpException(String id, String[][] params)
    {
        super(id, params);
    }

    /**
     * Creates a new <code>AdapterHttpException</code> with a cause.
     *
     * @param id
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameters to be substituted in the error
     *        message.
     * @param cause
     *        Throwable that caused this exception to get thrown
     */
    public AdapterHttpException(String id, String[][] params, Throwable cause)
    {
        super(id, params, cause);
    }
}
