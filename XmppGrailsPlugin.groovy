import org.jivesoftware.smack.RosterListenerimport org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CFG
import grails.util.GrailsUtilimport grails.util.GrailsNameUtils

import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.PacketListenerimport org.jivesoftware.smack.RosterListener
import org.jivesoftware.smack.filter.PacketFilterimport org.jivesoftware.smack.Chatimport org.jivesoftware.smack.packet.Presenceimport org.codehaus.groovy.grails.commons.ServiceArtefactHandler
class XmppGrailsPlugin {
	// the plugin version
	def version = "0.1"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "1.1 > *"	// the other plugins this plugin depends on
	//def dependsOn = [services: GrailsUtil.getGrailsVersion(), controllers: GrailsUtil.getGrailsVersion()]	
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
	"grails-app/views/error.gsp",
	"grails-app/services/org/grails/xmpp/*Service.groovy",	"grails-app/controllers/org/grails/xmpp/*Controller.groovy",	"grails-app/conf/XmppBootStrap.groovy",	"src/groovy/org/grails/xmpp/ClosureMessageListenerAdapter.groovy",	"scripts/*.groovy"	]
	
	def author = "Fábio Franco Uechi"
 	def authorEmail = "fabio.uechi@gmail.com"
	def title = "Provides XMPP based services to grails applications"
	def description = '''\\
Eases the development of real-time web applications. Built upon the Smack API
and provide IM based services to grails application.
'''
	
	// URL to the plugin's documentation
	def documentation = "http://grails.org/GrailsXmpp+Plugin"
	def loadAfter = ['services', 'controllers']
	def observe = ['services', 'controllers']
		def listenerConfigs = [:]	
	def doWithSpring = {
				registerXmppBaseBeans(delegate, log)
		xmppAgent (org.grails.xmpp.XmppAgent){ bean ->			bean.destroyMethod = "destroy"			connection = xmppConnection			username = CFG.config.xmpp.username			password = CFG.config.xmpp.password		}
		/*
		 * For each service containing the expose xmpp attribute
		 * creates and registers the respectice MessageListenerAdapter bean
		 * in the spring context
		 */
		application.serviceClasses?.each { service ->
			registerXmppMessageListenerBean(service, delegate, log)
		}
	}
	def registerXmppBaseBeans (beanBuilder, log) {		beanBuilder.with {			connectionConfiguration (org.jivesoftware.smack.ConnectionConfiguration, CFG.config.xmpp.connection.host, CFG.config.xmpp.connection.port, CFG.config.xmpp.connection.service ) {				SASLAuthenticationEnabled = CFG.config.xmpp.connection.SASLAuthenticationEnabled				reconnectionAllowed = true			}						xmppConnection (org.jivesoftware.smack.XMPPConnection, ref(connectionConfiguration)) {			}					}	}		def registerXmppMessageListenerBean(service, beanBuilder, log) {			def serviceClass = service.getClazz()			def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')			if (exposeList!=null && exposeList.contains('xmpp')) {								def listenerMethod = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'listenerMethod')				if (!listenerMethod)				listenerMethod = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_LISTENER_METHOD								def xmppCommandPrefix = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'xmppCommandPrefix')				// comparing to null to allow "prefixless" commands				if (xmppCommandPrefix == null)				xmppCommandPrefix = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_COMMAND_PREFIX								def xmppCommandMethodSuffix = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'xmppCommandMethodSuffix')				// comparing to null to allow "suffixless" commands				if (xmppCommandMethodSuffix == null)				xmppCommandMethodSuffix = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_COMMAND_METHOD_SUFFIX								beanBuilder.with {					"${service.shortName}XmppMessageListener"(org.grails.xmpp.MessageListenerAdapter, ref("${service.propertyName}")) {						defaultListenerMethod = listenerMethod						defaultXmppCommandPrefix = xmppCommandPrefix						defaultXmppCommandMethodSuffix = xmppCommandMethodSuffix					}				}			}	}	
	def doWithApplicationContext = { applicationContext ->
		// Checks all services and controllers which implement		// PackageListener or MessageListener and add to xmppAgent		application.serviceClasses?.each { service ->			registerXmppMessageListener(service, applicationContext)
		}
		//initializes xmppAgent
		if (CFG.config.xmpp.autoStartup)
			applicationContext.xmppAgent.connect()
	}
	def registerXmppMessageListener(service, applicationContext) {		def serviceClass = service.getClazz()		def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')				if (exposeList!=null && exposeList.contains('xmpp')) {			//println ">>>> Adding XMPP listener for ${service.shortName} to xmppAgent"			applicationContext.xmppAgent.packetListeners.add(applicationContext.getBean("${service.shortName}XmppMessageListener"))		}    	if(PacketListener.class.isAssignableFrom(serviceClass)) {            def svc = applicationContext.getBean("${service.propertyName}")            applicationContext.xmppAgent.packetListeners.add(svc)        }    	if(RosterListener.class.isAssignableFrom(serviceClass)) {            def svc = applicationContext.getBean("${service.propertyName}")            applicationContext.xmppAgent.rosterListeners.add(svc)        }	}		def doWithDynamicMethods = { ctx ->	    def xmppAgent = ctx.getBean("xmppAgent")	    [application.controllerClasses, application.serviceClasses].each {	        it.each {	            if (it.clazz.name != "XmppService") {	            	addXmppMethodsToClass(xmppAgent, it.clazz)	            }	        }	    }	}
    def addXmppMethodsToClass(xmppAgent, clazz) {        [            sendXmppMessage: "sendXmppMessage",             changeXmppRosterStatus: "changeXmppRosterStatus",             addXmppContact: "addXmppContact",            removeXmppContact: "removeXmppContact",          //  blockXmppContact: "blockXmppContact",            getAllXmppContacts: "getAllXmppContacts"        ].each { m, i ->        	clazz.metaClass."$m" << this."$i".curry(xmppAgent)        }    }		def onChange = { event ->
		// Code that is executed when any artefact that this plugin is
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.	    if (event.source && event.ctx) {	        def xmppAgent = event.ctx.getBean('xmppAgent')	        if (application.isControllerClass(event.source)) {	        	def controllerClass = event.application.getControllerClass(event.source?.name)        		addXmppMethodsToClass(xmppAgent, controllerClass.clazz)	        } else if (application.isServiceClass(event.source)) {	            boolean isNew = event.application.getServiceClass(event.source?.name) == null	            def serviceClass = application.addArtefact(ServiceArtefactHandler.TYPE, event.source).clazz	            def tgt = event.ctx.getBean(GrailsNameUtils.getPropertyName(event.source))	            if (!isNew) {	            		            	def exposeList = GrailsClassUtils.getStaticPropertyValue(event.source, 'expose')					if (exposeList!=null && exposeList.contains('xmpp')) {						xmppAgent.connection.removePacketListener(event.ctx.getBean("${GrailsNameUtils.getShortName(event.source)}XmppMessageListener"))						event.ctx.removeBeanDefinition("${GrailsNameUtils.getShortName(event.source)}XmppMessageListener")					}	            	if(PacketListener.class.isAssignableFrom(event.source)) {	            		xmppAgent.connection.removePacketListener(tgt)	            	}	            	if(RosterListener.class.isAssignableFrom(event.source)) {	            		xmppAgent.roster.removeRosterListener(tgt)	            	}	            }	            	            serviceClass = event.application.getServiceClass(event.source?.name)	            def newBeans = beans {                	registerXmppMessageListenerBean(serviceClass, delegate, log)                }                newBeans.beanDefinitions.each { n,d ->                    event.ctx.registerBeanDefinition(n, d)                }                addXmppMethodsToClass(xmppAgent, serviceClass.clazz)                xmppAgent.unregisterListeners()                doWithApplicationContext(event.ctx)	            xmppAgent.registerListeners()	        }	    }	}
	
	def onConfigChange = { event ->		def wasConnectedBeforeConfigChange = applicationContext.xmppAgent.connection?.isConnected()		def pList = applicationContext.xmppAgent.packetListeners		def rList = applicationContext.xmppAgent.rosterListeners						if (wasConnectedBeforeConfigChange) {			applicationContext.xmppAgent.disconnect()		} 			event.ctx.removeBeanDefinition("connectionConfiguration")		event.ctx.removeBeanDefinition("xmppConnection")			def newBeans = beans {	    	registerXmppBaseBeans(delegate, log)	    }	    newBeans.beanDefinitions.each { n,d ->	        event.ctx.registerBeanDefinition(n, d)	    }	    applicationContext.xmppAgent.connection = applicationContext.xmppConnection	    applicationContext.xmppAgent.username = CFG.config.xmpp.username	    applicationContext.xmppAgent.password = CFG.config.xmpp.password	    	    applicationContext.xmppAgent.packetListeners = pList 	    applicationContext.xmppAgent.rosterListeners = rList 	    doWithDynamicMethods(event.ctx)	    
		if (CFG.config.xmpp.autoStartup || wasConnectedBeforeConfigChange) {			applicationContext.xmppAgent.connect()		}	}	//	 xmpp utility closures	def addXmppContact = { xmppAgent, to ->		if (to instanceof List) {			to.each { user ->				xmppAgent.roster.createEntry(user, user, null)			}		} else {			xmppAgent.roster.createEntry(to, to, null)			}	}		def removeXmppContact = { xmppAgent, to ->			if (to instanceof List) {			to.each { user ->		    	user = StringUtils.substringBefore(user,"/")			    println "removing ${user}"				xmppAgent.roster.removeEntry(xmppAgent.roster.getEntry(user))			}		} else {			to = StringUtils.substringBefore(to,"/")			println "looking for entry ${to}"			def entry = xmppAgent.roster.getEntry(to)			if (entry) {				println "found ${to}"				xmppAgent.roster.removeEntry(entry)			}							else {				println "not found entry ${to}"			}						}	}		def sendXmppMessage = { xmppAgent, to, text ->		try{			Chat chat = xmppAgent.connection.chatManager.createChat(to, null)			def msgObj = new Message(to, Message.Type.chat)			msgObj.body = text						if (to instanceof List) {				to.each { addr ->					msgObj.to = addr					chat.sendMessage(msgObj)				}			} else {				chat.sendMessage(msgObj)				}					} catch (Exception e) {			log.error "Failed to send message", e		}	}		def getAllXmppContacts = { xmppAgent ->		return xmppAgent.roster.entries	}		def changeXmppRosterStatus = { xmppAgent, newStatus, presenceType ->		Presence presence = new Presence(presenceType);		presence.setStatus(newStatus);		xmppAgent.connection.sendPacket(presence);	}	
}