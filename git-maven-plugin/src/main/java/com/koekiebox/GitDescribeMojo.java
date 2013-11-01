package com.koekiebox;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * User: jasonbruwer
 * Date: 10/16/13
 * Time: 12:11 AM
 */
@Mojo(name="git_describe")
public class GitDescribeMojo extends AbstractMojo{

	private static final String DEFAULT_GIT_DESCRIBE = "git.describe";

	@Parameter(property = "git-describe-prop-name",defaultValue = DEFAULT_GIT_DESCRIBE)
	private String systemPropertyNameForGitDescribe;

	@Parameter(property = "makeUseOfJavaFile",defaultValue = "false")
	private boolean makeUseOfJavaFile;

	@Parameter(property = "pathOfTheJavaFile",defaultValue = "false")
	private String pathOfTheJavaFile;

	@Parameter(property = "constantToChange",defaultValue = "false")
	private String constantToChange;


	/** @parameter default-value="${project}" */
	@Component
	private MavenProject mavenProject;

	/**
	 *
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Properties properties = this.mavenProject.getProperties();

		this.getLog().info("Executing '"+this.mavenProject.getName()+
															 "' Version: "+this.mavenProject.getVersion()+ " with 'git describe' support.");


		//Make use of the Java File...
		if(this.makeUseOfJavaFile)
		{
			//pathOfTheJavaFile
			if(this.pathOfTheJavaFile == null || this.pathOfTheJavaFile.isEmpty())
			{
				this.getLog().error("When 'makeUseOfJavaFile' the property 'pathOfTheJavaFile' needs to be supplied also.");
				return;
			}

			//constantToChange
			if(this.constantToChange == null || this.constantToChange.isEmpty())
			{
				this.getLog().error("When 'makeUseOfJavaFile' the property 'constantToChange' needs to be supplied also.");
				return;
			}

			this.getLog().info("Making use of Java File. Creating Java File at '"+this.pathOfTheJavaFile+"' for constant '"+
																 this.constantToChange+"'.");

			File pomFileExecuted = this.mavenProject.getFile();

			File directoryOfPomFile = pomFileExecuted.getParentFile();

			File javaFileToEdit =
							new File(directoryOfPomFile.getAbsolutePath()+File.separator+this.pathOfTheJavaFile);

			if(javaFileToEdit.exists())
			{
				try
				{
					String propertyVal = this.getGitDescribeValue();
					this.getLog().info("Starting to edit Java File...");
					this.editJavaFile(javaFileToEdit,propertyVal);
				}
				catch (IOException ioExcept)
				{
					this.getLog().error("Unable to add Git Describe: "+ioExcept.getMessage()+".",ioExcept);
				}
			}
			else
			{
				this.getLog().error("Java File at location '"+javaFileToEdit.getAbsolutePath()+
																		"' does not exist.");
			}
		}
		else
		{
			this.getLog().info("Making use of setting Maven property.");
			String propertyVal = this.getGitDescribeValue();

			if(this.systemPropertyNameForGitDescribe == null ||
							this.systemPropertyNameForGitDescribe.trim().isEmpty())
			{
				this.systemPropertyNameForGitDescribe = DEFAULT_GIT_DESCRIBE;
			}

			properties.setProperty(this.systemPropertyNameForGitDescribe,propertyVal);

			this.getLog().info("["+this.systemPropertyNameForGitDescribe+"]: "+propertyVal);
		}
	}

	/**
	 *
	 * @param toReadParam
	 * @throws IOException
	 */
	private void editJavaFile(File toReadParam,String gitDescribeValueParam) throws IOException
	{
		String finalSource = null;
		String fullPath = null;
		try
		{
			this.getLog().info("Reading existing content...");

			finalSource = FileUtils.readFileToString(toReadParam);

			this.getLog().info("Done...Replacing String...");

			fullPath = toReadParam.getAbsolutePath();

			this.getLog().info("0. fullPath: "+fullPath+" | "+constantToChange);

			//public static final String VERSION = "";
			int indexOfConstantVarDecl = finalSource.lastIndexOf(this.constantToChange);

			this.getLog().info("1. indexOfConstantVarDecl: "+indexOfConstantVarDecl);

			String prefix = finalSource.substring(0,indexOfConstantVarDecl);

			this.getLog().info("2. prefix:\n "+prefix);

			String poster = finalSource.substring(indexOfConstantVarDecl + this.constantToChange.length());
			this.getLog().info("2.2. poster:\n "+poster);

			int indexOfDblQuote = poster.indexOf('\"');
			String postfixPartOne = poster.substring(0,indexOfDblQuote+1);

			int indexOfSecondDblQuote = poster.indexOf('\"',indexOfDblQuote+1);

			String postfixPartTwo = poster.substring(indexOfSecondDblQuote);

			this.getLog().info("6. postfixPartOne:\n "+postfixPartOne);
			this.getLog().info("7. postfixPartTwo:\n "+postfixPartTwo);

			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append(prefix);
			stringBuilder.append(this.constantToChange);
			stringBuilder.append(postfixPartOne);
			stringBuilder.append(gitDescribeValueParam);
			stringBuilder.append(postfixPartTwo);

			this.getLog().info("\n\n\n\n----------\n"+stringBuilder.toString()+"\n---------\n\n\n\n\n\n");

			toReadParam.delete();

			finalSource = stringBuilder.toString();
		}
		finally {
			if(!new File(fullPath).exists() && (finalSource != null && !finalSource.isEmpty()))
			{
				FileUtils.writeStringToFile(new File(fullPath),finalSource);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	private String getGitDescribeValue() throws MojoExecutionException
	{
		CommandUtil.CommandResult commandResult = CommandUtil.executeCommand(
						this.getLog(),"git","describe");

		String[] resultLines = commandResult.getResultLines();
		if(resultLines == null || resultLines.length == 0)
		{
			return "Unable to get tag version.";
		}

		if(commandResult.getExitCode() != 0)
		{
			String specificError = "";

			for(String line: resultLines)
			{
				specificError += line;
			}

			return ("Not expected response code ["+commandResult.getExitCode()
																							 +"]. 'git describe' failed. Tag your repository!!! "+specificError);
		}

		this.getLog().info("Execute Result: " +commandResult.getExitCode() + commandResult.getResultLines()[0]);

		String returnVal = "";
		for(String line: resultLines)
		{
			returnVal += line;
		}

		return returnVal;
	}
}
