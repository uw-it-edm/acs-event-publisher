# Configure components :

This is the list of allowed properties and their default values : 

    # Enable subscription to ACS events ( Enable the component ) 
    edm.eventpublisher.enabled=true
    # allow the component to subscribe to ACS events without emitting sns events
    edm.eventpublisher.noop=false  
    edm.eventpublisher.aws_region=
    edm.eventpublisher.aws_accessKeyId=
    edm.eventpublisher.aws_secretAccessKey=
    edm.eventpublisher.snsTopicARN=


#  Configure access to Alfresco Private Repository
  * Create a maven master password, then encrypt the artifacts.alfresco.com password : https://maven.apache.org/guides/mini/guide-encryption.html
  * Add the repository in your .m2/settings.xml following this : https://docs.alfresco.com/4.2/tasks/dev-extensions-maven-sdk-tutorials-configure-maven-enterprise.html



# Create release 

Travis will create a new GitHub release when a new tag is detected.
To create new tag, run these commands on your local machine : 

    git flow release start x.x.x
    mvn versions:set -DnewVersion=x.x.x -DgenerateBackupPoms=false
    git add pom.xml
    git commit -m "Bump version"
    git flow release finish 
    git push origin master develop
    
Once this is done, prepare develop for the next devolpment cycle by updating the pom to use a `SNAPSHOT` version

    mvn versions:set -DnewVersion=x.x.x-SNAPSHOT -DgenerateBackupPoms=false
    git add pom.xml
    git commit -m  "Prepare for next development cycle"
    git push origin develop 



#Alfresco SDK does not support Alfresco 6.X, the directions below are out of date

---

# SetUp

* copy the license file from one of our server `scp $(ec2GetForEnvAndType dev acs-repo | jq -r '.[0].privateIp'):/usr/share/tomcat/shared/classes/alfresco/extension/license/*.lic* ./src/test/license/licence.lic`
* symlink the content-model repo to `./src/test/resources/alfresco/extensions`
`ln -s /path/to/acs-content-models/* ./src/test/resources/alfresco/extension/`

# Run in IntelliJ
* add a new maven project, command should be `clean install alfresco:run`
* Go to the `Runner` tab, override the alfresco/module/acs-event-publisher/alfresco-global.properties with the values you want to use  



# Alfresco Platform JAR Module - SDK 3

To run use `mvn clean install -DskipTests=true alfresco:run` or `./run.sh` and verify that it 

 * Runs the embedded Tomcat + H2 DB 
 * Runs Alfresco Platform (Repository)
 * Runs Alfresco Solr4
 * Packages both as JAR and AMP assembly
 
 Try cloning it, change the port and play with `enableShare`, `enablePlatform` and `enableSolr`. 
 
 Protip: This module will work just fine as a Share module if the files are changed and 
 if the enablePlatform and enableSolr is disabled.
 
# Few things to notice

 * No parent pom
 * WAR assembly is handled by the Alfresco Maven Plugin configuration
 * Standard JAR packaging and layout
 * Works seamlessly with Eclipse and IntelliJ IDEA
 * JRebel for hot reloading, JRebel maven plugin for generating rebel.xml, agent usage: `MAVEN_OPTS=-Xms256m -Xmx1G -agentpath:/home/martin/apps/jrebel/lib/libjrebel64.so`
 * AMP as an assembly
 * [Configurable Run mojo](https://github.com/Alfresco/alfresco-sdk/blob/sdk-3.0/plugins/alfresco-maven-plugin/src/main/java/org/alfresco/maven/plugin/RunMojo.java) in the `alfresco-maven-plugin`
 * No unit testing/functional tests just yet
 * Resources loaded from META-INF
 * Web Fragment (this includes a sample servlet configured via web fragment)
 
# TODO
 
  * Abstract assembly into a dependency so we don't have to ship the assembly in the archetype
  * Purge, 
  * Functional/remote unit tests
   
