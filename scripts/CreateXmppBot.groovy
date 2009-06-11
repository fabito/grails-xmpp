Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

if(grails.util.GrailsUtil.grailsVersion.startsWith("1.1")) {
	includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )
	//includeTargets << new File( "${grailsHome}/scripts/CreateIntegrationTest.groovy")

	target('default': "Creates a new xmpp bot") {
	    depends(checkVersion)

		typeName = "XmppBot"
		artifactName = "XmppBot"
		artifactPath = "grails-app/xmpp-bots"

		createArtifact()
		//createTestSuite()
	}
}
else {
	includeTargets << grailsScript("_GrailsInit")
	includeTargets << grailsScript("_GrailsCreateArtifacts")

	target('default': "Creates a new xmpp bot") {
	    depends(checkVersion, parseArguments)

	    def type = "XmppBot"
	    promptForName(type: type)

	    def name = argsMap["params"][0]
	    createArtifact(name: name, suffix: type, type: type, path: "grails-app/xmpp-bots")
		//createUnitTest(name: name, suffix: type)
	}
}