package com.koekiebox;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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

		if(this.systemPropertyNameForGitDescribe == null ||
						this.systemPropertyNameForGitDescribe.trim().isEmpty())
		{
			properties.setProperty(DEFAULT_GIT_DESCRIBE,this.getGitDescribeValue());
		}
		else
		{
			properties.setProperty(this.systemPropertyNameForGitDescribe,this.getGitDescribeValue());
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
																							 +"]. 'git describe' failed. Tag your repository. "+specificError);
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
