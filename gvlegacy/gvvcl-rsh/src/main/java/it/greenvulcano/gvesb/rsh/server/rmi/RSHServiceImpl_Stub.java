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
package it.greenvulcano.gvesb.rsh.server.rmi;


/*
public final class RSHServiceImpl_Stub extends java.rmi.server.RemoteStub */

import com.healthmarketscience.rmiio.RemoteInputStream;

import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;

import java.lang.reflect.Method;

import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.server.rmi.RSHService;

/**
 * @version 4.0.0 - Mar 2017
 * @author GreenVulcano Developer Team
 */

public final class RSHServiceImpl_Stub extends RemoteStub implements RSHService,Remote
{
	private static final long serialVersionUID = 2;

    private static Method $method_getFile_0;
    private static Method $method_sendFile_1;
    private static Method $method_shellExec_2;
    
    static {
        try {
            $method_getFile_0 = RSHService.class.getMethod("getFile",new Class[]{String.class});
            $method_sendFile_1 = RSHService.class.getMethod("sendFile",new Class[]{String.class, RemoteInputStream.class});
            $method_shellExec_2 = RSHService.class.getMethod("shellExec",new Class[]{ShellCommandDef.class});
        }
        catch (NoSuchMethodException e) {
            throw new NoSuchMethodError("stub class initialization failed");
        }
    }
    
    /**
     * Constructor. Set ref of java.rmi.server.RemoteObject.ref
     * 
     * @throws RemoteException
     * @see java.rmi.server.UnicastRemoteObject#UnicastRemoteObject()
     */
    public RSHServiceImpl_Stub(RemoteRef ref) throws RemoteException
    {
    	super(ref);
    }
	
    /**
     * 
     * @param $param_String_1
     * @return RemoteInputStream
     * @throws RSHException
     * @throws RemoteException
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#getFile(String)
     */
    @Override
    public RemoteInputStream getFile(String $param_String_1)throws RSHException, RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_getFile_0, new Object[]{$param_String_1},
                    -3494612659481588766L);
            return ((RemoteInputStream) $result);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (RemoteException e) {
            throw e;
        }
        catch (RSHException e) {
            throw e;
        }
        catch (Exception e) {
            throw new UnexpectedException("undeclared checked exception", e);
        }
    }
    
    /**
     * 
     * @param $param_String_1
     * @param $param_RemoteInputStream_2
     * @throws RSHException
     * @throws RemoteException
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#sendFile(String, RemoteInputStream)
     */
    @Override
    public void sendFile(String $param_String_1,RemoteInputStream $param_RemoteInputStream_2)throws RSHException, RemoteException
    {
        try {
            ref.invoke(this, $method_sendFile_1, new Object[]{$param_String_1, $param_RemoteInputStream_2},
                    -9108544457034698505L);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (RemoteException e) {
            throw e;
        }
        catch (RSHException e) {
            throw e;
        }
        catch (Exception e) {
            throw new UnexpectedException("undeclared checked exception", e);
        }
    }
    
    /**
     * 
     * @param $param_ShellCommandDef_1
     * @return ShellCommandResult
     * @throws RSHException
     * @throws RemoteException
     * @see it.greenvulcano.gvesb.rsh.server.rmi.RSHService#shallExec(ShellCommandDef commandDef)
     */
    @Override
    public ShellCommandResult shellExec(ShellCommandDef $param_ShellCommandDef_1) throws RSHException, RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_shellExec_2, new Object[]{$param_ShellCommandDef_1},
                    8771592846415270156L);
            return ((ShellCommandResult) $result);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (RemoteException e) {
            throw e;
        }
        catch (RSHException e) {
            throw e;
        }
        catch (Exception e) {
            throw new UnexpectedException("undeclared checked exception", e);
        }
    }
}
