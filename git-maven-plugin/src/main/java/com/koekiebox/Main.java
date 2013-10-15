package com.koekiebox;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * User: jasonbruwer
 * Date: 10/16/13
 * Time: 12:11 AM
 */
@Mojo(name="git_describe")
public class Main  extends AbstractMojo{

	/**
	 *
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		//Print Hello world so we know its being called...
		this.getLog().info( "Hello, world." );
	}
}
