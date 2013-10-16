Git Maven Plugin
================

Maven Git plugin to perform basic Git functions from within Maven.

*** Usage ***

1. Add Plug-in repository.

>       <dependency>
>             <groupId>com.koekiebox.git-maven-plugin</groupId>
>             <artifactId>git-maven-plugin</artifactId>
>             <scope>compile</scope>
>           </dependency>

2. Declare plugin in pom.xml and configure. Make sure its the first plugin.

> <!--Git Describe-->
>       <plugin>
>         <groupId>com.koekiebox.git-maven-plugin</groupId>
>         <artifactId>git-maven-plugin</artifactId>
>         <executions>
>           <execution>
>             <phase>initialize</phase>
>             <goals>
>               <goal>git_describe</goal>
>             </goals>
>           </execution>
>         </executions>
>         <configuration>
>           <systemPropertyNameForGitDescribe>git.describe</systemPropertyNameForGitDescribe>
>         </configuration>
>      </plugin>

3. Make use of the 'maven-jar-plugin' to extract the property and place in MANIFEST.MF.

> <!--Include in the MANIFEST.MF with the Git-Describe-->
>       <plugin>
>         <groupId>org.apache.maven.plugins</groupId>
>         <artifactId>maven-jar-plugin</artifactId>
>         <version>2.4</version>
>         <configuration>
>           <archive>
>             <index>true</index>
>             <manifest>
>               <addClasspath>true</addClasspath>
>             </manifest>
>             <manifestEntries>
>               <git-describe>${git.describe}</git-describe><!--This is where we retrieve the property-->
>               <mode>development</mode>
>               <url>${project.url}</url>
>               <key>value</key>
>             </manifestEntries>
>           </archive>
>         </configuration>
>       </plugin>

4. Make use of the following code to extract the "git.describe" value from the MANIFEST.MF.

> Class clazz = MyClass.class;
> String className = clazz.getSimpleName() + ".class";
> String classPath = clazz.getResource(className).toString();
> if (!classPath.startsWith("jar")) {
>   // Class not from JAR
>   return;
> }
> String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
>     "/META-INF/MANIFEST.MF";
> Manifest manifest = new Manifest(new URL(manifestPath).openStream());
> Attributes attr = manifest.getMainAttributes();
> String value = attr.getValue("git.describe");


