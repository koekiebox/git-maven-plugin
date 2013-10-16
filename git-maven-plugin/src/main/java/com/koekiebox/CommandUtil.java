package com.koekiebox;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jbruwer
 * Date: 10/1/13
 * Time: 1:07 PM
 */
public class CommandUtil {

	public static final class CommandResult
	{
		private int exitCode;
		private String[] resultLines;

		private CommandResult(int exitCodeParam,String[] resultLinesParam)
		{
			this.exitCode = exitCodeParam;
			this.resultLines = resultLinesParam;
		}

		/**
		 *
		 * @return
		 */
		public int getExitCode() {
			return this.exitCode;
		}

		/**
		 *
		 * @return
		 */
		public String[] getResultLines() {
			return this.resultLines;
		}

		/**
		 *
		 * @return
		 */
		@Override
		public String toString()
		{

			if(this.resultLines == null)
			{
				return null;
			}

			StringBuilder stringBuilder = new StringBuilder();
			for(String line : this.resultLines)
			{
				stringBuilder.append(line);
				stringBuilder.append("\n");
			}

			return stringBuilder.toString();
		}
	}

	/**
	 *
	 * @param commandParams
	 * @return
	 */
	public static CommandResult executeCommand(Log logParam,String... commandParams) throws MojoExecutionException
	{
		if(commandParams == null || commandParams.length == 0)
		{
			throw new MojoExecutionException("Unable to execute command. No commands provided.");
		}

		logParam.info("Cool, in the bag...");

		List<String> returnedLines = new ArrayList<>();

		try
		{
			logParam.info("About to execute...");
			Process process = Runtime.getRuntime().exec(commandParams);
			logParam.info("Got Process");

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			logParam.info("Got Reader");
			String readLine = null;
			while((readLine = reader.readLine()) != null)
			{
				logParam.info("LINE["+readLine+"]");
				returnedLines.add(readLine);
			}

			//Now for the error lines...
			if(returnedLines.isEmpty())
			{
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

				while((readLine = errorReader.readLine()) != null)
				{
					logParam.info("ERROR_LINE["+readLine+"]");
					returnedLines.add(readLine);
				}
			}

			logParam.info("Time to wait");
			int exitValue = -1000;
			try {
				exitValue = process.waitFor();
			}
			//
			catch (InterruptedException e) {
				logParam.error(e.getMessage());

				String commandString = (commandParams == null || commandParams.length == 0) ? "<unknown>":commandParams[0];

				throw new MojoExecutionException("Unable to wait for command ["+commandString+"] to exit. "+e.getMessage(),e);
			}

			logParam.info("Returning Result: "+exitValue);

			String[] rtnArr = {};
			return new CommandResult(exitValue,returnedLines.toArray(rtnArr));
		}
		catch (IOException ioExeption)
		{
			logParam.error(ioExeption.getMessage());
			String commandString = (commandParams == null || commandParams.length == 0) ? "<unknown>":commandParams[0];

			throw new MojoExecutionException("Unable to execute command/s ["+commandString+"]. "+ioExeption.getMessage(),ioExeption);
		}
	}
}
