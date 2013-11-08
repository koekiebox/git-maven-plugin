package com.koekiebox;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * User: jasonbruwer Date: 10/16/13 Time: 12:11 AM
 */
@Mojo(name = "git_describe",defaultPhase = LifecyclePhase.INITIALIZE)
public class GitDescribeMojo extends AbstractMojo {

    private static final String DEFAULT_GIT_DESCRIBE = "git.describe";

    @Parameter(property = "git-describe-prop-name", defaultValue = DEFAULT_GIT_DESCRIBE)
    private String systemPropertyNameForGitDescribe;

    @Parameter(property = "makeUseOfJavaFile", defaultValue = "false")
    private boolean makeUseOfJavaFile;

    @Parameter(property = "classPathToJavaFile", defaultValue = "false")
    private String classPathToJavaFile;

    @Parameter(property = "constantToChange", defaultValue = "false")
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

	this.getLog().info("Executing '" + this.mavenProject.getName() + "' Version: " + this.mavenProject.getVersion() + " with 'git describe' support.");

			File pomFileExecuted = this.mavenProject.getFile();
			File directoryOfPomFile = pomFileExecuted.getParentFile();

	// Make use of the Java File...
	if (this.makeUseOfJavaFile) {
	    // pathOfTheJavaFile
	    if (this.classPathToJavaFile == null || this.classPathToJavaFile.isEmpty()) {
		this.getLog().error("When 'makeUseOfJavaFile' the property 'classPathToJavaFile' also needs to be supplied.");
		return;
	    }

	    // constantToChange
	    if (this.constantToChange == null || this.constantToChange.isEmpty()) {
		this.getLog().error("When 'makeUseOfJavaFile' the property 'constantToChange' needs to be supplied also.");
		return;
	    }

	    String actualPath = this.getActualPathToJavaFile();
		File actualPathFile = new File(actualPath);

	    if (!actualPathFile.exists()) {
		this.getLog().info("No GitDescribe file at '" + new File(actualPath).getAbsolutePath() + "'. Creating one.");

		try {
		    this.createDefaultGitDescribe(new File(actualPath));
		} catch (IOException ioExcept) {
		    this.getLog().error("Unable to create new Git Describe java file from scratch: " + ioExcept.getMessage() + ".", ioExcept);
		}
	    }
		else
			{
				this.getLog().info("File '"+actualPathFile.getAbsolutePath()+"' exists. No need to create.");
			}

	    this.getLog().info("Making use of Java File at '" + actualPath + "' for constant '" + this.constantToChange + "'.");

		//
	    if (actualPathFile.exists()) {
		try {
		    String propertyVal = this.getGitDescribeValue();
		    this.getLog().info("Starting to edit Java File...");
		    this.editJavaFile(actualPathFile, propertyVal);
		}
		//
		catch (IOException ioExcept) {
		    this.getLog().error("Unable to add Git Describe: " + ioExcept.getMessage() + ".", ioExcept);
		}
	    }
			//
			else {
		this.getLog().info("No Java File at location '" + actualPathFile.getAbsolutePath() + "' exists to edit.");
	    }
	} else {
	    this.getLog().info("Making use of setting Maven property.");
	    String propertyVal = this.getGitDescribeValue();

	    if (this.systemPropertyNameForGitDescribe == null || this.systemPropertyNameForGitDescribe.trim().isEmpty()) {
		this.systemPropertyNameForGitDescribe = DEFAULT_GIT_DESCRIBE;
	    }

	    properties.setProperty(this.systemPropertyNameForGitDescribe, propertyVal);

	    this.getLog().info("[" + this.systemPropertyNameForGitDescribe + "]: " + propertyVal);
	}
    }

    /**
     * 
     * @param toReadParam
     * @throws IOException
     */
    private void editJavaFile(File toReadParam, String gitDescribeValueParam) throws IOException {
	String finalSource = null;
	String fullPath = null;
	try {
		this.getLog().info("Existing Value: \n\n"+
															 gitDescribeValueParam);

	    finalSource = FileUtils.readFileToString(toReadParam);

	    fullPath = toReadParam.getAbsolutePath();

	    // public static final String VERSION = "";
	    int indexOfConstantVarDecl = finalSource.lastIndexOf(this.constantToChange);

	    String prefix = finalSource.substring(0, indexOfConstantVarDecl);

	    String poster = finalSource.substring(indexOfConstantVarDecl + this.constantToChange.length());
	    int indexOfDblQuote = poster.indexOf('\"');
	    String postfixPartOne = poster.substring(0, indexOfDblQuote + 1);

	    int indexOfSecondDblQuote = poster.indexOf('\"', indexOfDblQuote + 1);

	    String postfixPartTwo = poster.substring(indexOfSecondDblQuote);

	    StringBuilder stringBuilder = new StringBuilder();

	    stringBuilder.append(prefix);
	    stringBuilder.append(this.constantToChange);
	    stringBuilder.append(postfixPartOne);
	    stringBuilder.append(gitDescribeValueParam);
	    stringBuilder.append(postfixPartTwo);

	    this.getLog().info("\n\n\n\n----------\n" + stringBuilder.toString() + "\n---------\n\n\n\n\n\n");

	    toReadParam.delete();

	    finalSource = stringBuilder.toString();
	} finally {
	    if (!new File(fullPath).exists() && (finalSource != null && !finalSource.isEmpty())) {
		FileUtils.writeStringToFile(new File(fullPath), finalSource);
	    }
	}
    }

    private static final String PREFIX = "src/main/java/";

    /**
     * 
     * @return
     */
    private String getActualPathToJavaFile() {
	if (this.classPathToJavaFile == null) {
	    return null;
	}

	String replaceDotWithForward = this.classPathToJavaFile.replace('.', '/');

			File pomFileExecuted = this.mavenProject.getFile();
			File directoryOfPomFile = pomFileExecuted.getParentFile();


			StringBuilder returnVal = new StringBuilder();
			returnVal.append(directoryOfPomFile.getAbsolutePath());
			returnVal.append("/");
			returnVal.append(PREFIX);
			returnVal.append(replaceDotWithForward);
			returnVal.append(".java");

			return returnVal.toString();

    }

    private static final String SOURCE = "" + "package {{PACKAGE}};\n\n" + "" + "public class {{CLASS_NAME}} {\n\n" + ""
	    + "public static final String {{GIT_DESCRIBE_CONSTANT_NAME}} = \"\";\n\n" + "" + "}";

    private static final class Token {
	private static final String CLASS_NAME = "{{CLASS_NAME}}";
	private static final String PACKAGE = "{{PACKAGE}}";
	private static final String GIT_DESCRIBE_CONSTANT_NAME = "{{GIT_DESCRIBE_CONSTANT_NAME}}";
    }

    /**
	 *
	 */
    private void createDefaultGitDescribe(File toCreateParam) throws IOException {

	int lastIndexOfDot = this.classPathToJavaFile.lastIndexOf('.');

	String className = this.classPathToJavaFile.substring(lastIndexOfDot + 1);
	String packageOnly = this.classPathToJavaFile.substring(0, lastIndexOfDot);

	String modifiedSource = SOURCE.replace(Token.CLASS_NAME, className);
	modifiedSource = modifiedSource.replace(Token.PACKAGE, packageOnly);
	modifiedSource = modifiedSource.replace(Token.GIT_DESCRIBE_CONSTANT_NAME, this.constantToChange);

			this.getLog().info(modifiedSource);


	FileUtils.writeStringToFile(toCreateParam, modifiedSource);
    }

    /**
     * 
     * @return
     */
    private String getGitDescribeValue() throws MojoExecutionException {
	CommandUtil.CommandResult commandResult = CommandUtil.executeCommand(this.getLog(), "git", "describe");

	String[] resultLines = commandResult.getResultLines();
	if (resultLines == null || resultLines.length == 0) {
	    return "Unable to get tag version.";
	}

	if (commandResult.getExitCode() != 0) {
	    String specificError = "";

	    for (String line : resultLines) {
		specificError += line;
	    }

	    return ("Not expected response code [" + commandResult.getExitCode() + "]. 'git describe' failed. Tag your repository!!! " + specificError);
	}

	this.getLog().info("Execute Result: " + commandResult.getExitCode() + commandResult.getResultLines()[0]);

	String returnVal = "";
	for (String line : resultLines) {
	    returnVal += line;
	}

	return returnVal;
    }
}
