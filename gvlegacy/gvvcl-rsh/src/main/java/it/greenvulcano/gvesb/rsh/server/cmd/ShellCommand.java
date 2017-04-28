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

package it.greenvulcano.gvesb.rsh.server.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.rsh.RSHException;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandDef;
import it.greenvulcano.gvesb.rsh.server.cmd.helper.ShellCommandResult;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.shell.StreamPumper;

/**
 * Executes the shell command
 * 
 * @version 4.0.0 - Mar 2017
 * @author GreenVulcano Developer Team
 */
public class ShellCommand implements Serializable {

	private static final long serialVersionUID = -1941483456172771275L;

	private static final Logger logger = LoggerFactory.getLogger(ShellCommand.class);

	/**
	 * The id
	 */
	private String id;
	/**
	 * Define the shell command element
	 */
	private ShellCommandDef commandDef;

	/**
	 * Constructor
	 * 
	 * @param commandDef
	 */
	public ShellCommand(ShellCommandDef commandDef) {
		NMDC.push();
		try {
			this.commandDef = commandDef;
			this.id = commandDef.getId();
			NMDC.put("CMD_ID", id);
			logger.debug("Create ShellCommand [" + id + "]");
		} finally {
			NMDC.pop();
		}
	}

	/**
	 * Execute the shell command
	 * 
	 * @return ShellCommandResult
	 * @throws RSHException
	 */
	public ShellCommandResult execute() throws RSHException {
		ShellCommandResult result = null;
		int exitCode = ShellCommandResult.NO_EXIT_CODE;
		PrintWriter stdIn = null;
		StreamPumper outputPumper = null;
		StreamPumper errorPumper = null;
		Process proc = null;

		NMDC.push();
		try {
			NMDC.put("CMD_ID", id);
			logger.info("Executing the ShellCommand[" + id + "]: \n" + commandDef);
			File wDir = null;
			if (commandDef.getWorkDir() != null) {
				wDir = new File(commandDef.getWorkDir());
			}

			List<String> commands = commandDef.getCommands();
			String command = commandDef.getCommand();
			if (commands != null) {
				command = commands.get(0);
			}

			proc = Runtime.getRuntime().exec(command, commandDef.getEnvArray(), wDir);

			outputPumper = new StreamPumper(proc.getInputStream());
			errorPumper = new StreamPumper(proc.getErrorStream());
			outputPumper.start();
			errorPumper.start();

			if ((commands != null) && (commands.size() > 1)) {
				stdIn = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
				for (int i = 1; i < commands.size(); i++) {
					stdIn.println(commands.get(i));
				}
			}

			if (commandDef.isDaemon()) {
				try {
					Thread.sleep(5000);
				} catch (Exception exc) {
					// TODO: handle exception
				}
				try {
					exitCode = proc.exitValue();
				} catch (IllegalThreadStateException exc) {
					exitCode = 0;
				}
				proc.destroy();
			} else {
				try {
					outputPumper.join();
					errorPumper.join();
					exitCode = proc.waitFor();
				} catch (InterruptedException exc) {
					logger.warn("Interrupted exception", exc);
				}
			}

			result = new ShellCommandResult(commandDef, exitCode, outputPumper.getOutput(), errorPumper.getOutput());
			logger.info("Execution terminated with status: " + exitCode);
			/*
			 * String stderr = errorPumper.getOutput(); if (stderr.length() > 0)
			 * { logger.warn("An error occurs executing the shell command:\n" +
			 * stderr); } logger.debug("Shell output:\n" +
			 * outputPumper.getOutput());
			 */
		} catch (Exception exc) {
			logger.error("An error occurs executing the shell task", exc);
			result = new ShellCommandResult(commandDef, exitCode, "", "" + exc);
		} finally {
			if (stdIn != null) {
				try {
					stdIn.close();
				} catch (Exception exc) {
					// do nothing
				}
			}
			if (proc != null) {
				try {
					proc.destroy();
				} catch (Exception exc) {
					// do nothing
				}
			}
			outputPumper = null;
			errorPumper = null;
			proc = null;

			NMDC.pop();
		}

		return result;
	}
}
