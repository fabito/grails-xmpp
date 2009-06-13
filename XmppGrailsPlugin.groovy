import org.jivesoftware.smack.RosterListenerimport org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CFG
import grails.util.GrailsUtil

import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.PacketListenerimport org.jivesoftware.smack.RosterListener
import org.jivesoftware.smack.filter.PacketFilter

class XmppGrailsPlugin {
	// the plugin version
	def version = "0.1"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "1.1 > *"
	// the other plugins this plugin depends on
	//def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
	"grails-app/views/error.gsp",
	"grails-app/services/org/grails/xmpp/MyService.groovy"
	]
	
	def author = "Fábio Franco Uechi"
 	def authorEmail = "fabio.uechi@gmail.com"
	def title = "Plugin summary/headline"
	def description = '''\\
Eases the development of real-time web applications. Built upon the Smack API
and provide IM based services to grails application.
'''
	
	// URL to the plugin's documentation
	def documentation = "http://grails.org/GrailsXmpp+Plugin"
	def loadAfter = ['services', 'controllers']
	def observe = ['services', 'controllers']
	def dependsOn = [services: GrailsUtil.getGrailsVersion()]
	
	def doWithSpring = {
		
		connectionConfiguration (org.jivesoftware.smack.ConnectionConfiguration, CFG.config.xmpp.connection.host, CFG.config.xmpp.connection.port, CFG.config.xmpp.connection.service ) {
			SASLAuthenticationEnabled = CFG.config.xmpp.connection.SASLAuthenticationEnabled
			reconnectionAllowed = true
		}
		
		xmppConnection (org.jivesoftware.smack.XMPPConnection, ref(connectionConfiguration)) {
			
		}
		
		xmppAgent (org.grails.xmpp.XmppAgent){ bean ->
			bean.destroyMethod = "destroy"
			connection = xmppConnection
			username = CFG.config.xmpp.username
			password = CFG.config.xmpp.password
		}
		
		/*
		 * For each service containing the expose xmpp attribute
		 * creates and registers the respectice MessageListenerAdapter bean
		 * in the spring context
		 */
		application.serviceClasses?.each { service ->
			def serviceClass = service.getClazz()
			def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')
			if (exposeList!=null && exposeList.contains('xmpp')) {
				
				def listenerMethod = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'listenerMethod')
				if (!listenerMethod)
				listenerMethod = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_LISTENER_METHOD
				
				def xmppCommandPrefix = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'xmppCommandPrefix')
				// comparing to null to allow "prefixless" commands
				if (xmppCommandPrefix == null)
				xmppCommandPrefix = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_COMMAND_PREFIX
				
				def xmppCommandMethodSuffix = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'xmppCommandMethodSuffix')
				// comparing to null to allow "suffixless" commands
				if (xmppCommandMethodSuffix == null)
				xmppCommandMethodSuffix = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_COMMAND_METHOD_SUFFIX
				
				"${service.shortName}XmppMessageListener"(org.grails.xmpp.MessageListenerAdapter, ref("${service.propertyName}")) {
					defaultListenerMethod = listenerMethod
					defaultXmppCommandPrefix = xmppCommandPrefix
					defaultXmppCommandMethodSuffix = xmppCommandMethodSuffix
				}
				
			}
		}
	}
	
	def doWithApplicationContext = { applicationContext ->
		// Checks all services and controllers which implement		// PackageListener or MessageListener and add to xmppAgent		application.serviceClasses?.each { service ->
			def serviceClass = service.getClazz()
			def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')
			if (exposeList!=null && exposeList.contains('xmpp')) {
				println ">>>> Adding XMPP listener for ${service.shortName} to xmppAgent"
				applicationContext.xmppAgent.packetListeners.add(applicationContext.getBean("${service.shortName}XmppMessageListener"))
			}
        	if(PacketListener.class.isAssignableFrom(serviceClass)) {                def svc = applicationContext.getBean("${service.propertyName}")                applicationContext.xmppAgent.packetListeners.add(svc)            }        	if(RosterListener.class.isAssignableFrom(serviceClass)) {                def svc = applicationContext.getBean("${service.propertyName}")                applicationContext.xmppAgent.rosterListeners.add(svc)            }				}
				//initializes xmppAgent
		if (CFG.config.xmpp.autoStartup)
			applicationContext.xmppAgent.connect()

	}
	
	
	def doWithDynamicMethods = { ctx ->
	}
	
	
	def onChange = { event ->
		// TODO Implement code that is executed when any artefact that this plugin is
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.
	}
	
	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}					
}




//                serviceClass.metaClass.methods?.find { it.name.endsWith("Command") }?.each { m ->
//                    if (serviceClass.metaClass.respondsTo(serviceClass, m.name, Packet)) {
//
//                        def methodName = m.name
//                        def closure = m
//                        println methodName
//
//                        def myListener = [ processPacket: { packet ->
//                                println "Received message from ${packet.from}, subject: ${packet.subject}, body: ${packet.body}"
//                                //serviceClass.metaClass.invokeMethod(serviceClass, m.name, packet)
//                                closure.call(packet)
//                         } ] as PacketListener
//
//                        def myFilter = [accept:{ packet ->
//                                //println "Received message from ${packet.from}, subject: ${packet.subject}, body: ${packet.body}"
//                                if (packet instanceof Message) {
//                                        Message msg = (Message) packet
//                                        return msg.body.startsWith("@" + methodName.replace("Command",""))
//                                }
//                                return false;
//                        }] as PacketFilter
//
//                        applicationContext.xmppAgent.packetListeners.add(myListener)
//
//                    }
//                }


//def svc = applicationContext.getBean("${serviceClass.propertyName}")
//applicationContext.xmppAgent.packetListeners.add(myListener)

		// TODO Checks all services and controllers which implement		// PackageListener or MessageListener and add to xmppconnection		//        for(serviceClass in application.serviceClasses) {		//            if(PacketListener.class.isAssignableFrom(serviceClass.clazz)) {		//		//                def myListener = [processPacket:{ packet ->		//                        println "Received message from ${packet.from}, subject: ${packet.subject}, body: ${packet.body}"		//		//                }] as PacketListener		//		//                def svc = applicationContext.getBean("${serviceClass.propertyName}")		//                applicationContext.xmppAgent.packetListeners.add(myListener)		//                applicationContext.xmppAgent.packetListeners.add(svc)		//                //TODO how to add new property to a GrailsService		//                //serviceClass.metaClass.xmppAgent << {-> applicationContext.xmppAgent }		//            }		//        }