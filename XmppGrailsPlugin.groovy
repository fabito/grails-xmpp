import org.codehaus.groovy.grails.commons.GrailsClassUtils
import grails.util.GrailsUtil

import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.PacketListener
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

        connectionConfiguration (org.jivesoftware.smack.ConnectionConfiguration, "talk.google.com", 5222, "gmail.com" ) {
            SASLAuthenticationEnabled = false
            reconnectionAllowed = true
        }

        xmppConnection (org.jivesoftware.smack.XMPPConnection, ref(connectionConfiguration)) {
            
        }

        xmppAgent (org.grails.xmpp.XmppAgent){ bean ->
            bean.destroyMethod = "destroy"
            connection = xmppConnection
            username = "123"
            password = "123"
        }

        application.serviceClasses?.each { service ->
            def serviceClass = service.getClazz()
            def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')
            if (exposeList!=null && exposeList.contains('xmpp')) {
                def sName = service.propertyName.replaceFirst("Service","")

                def listenerMethod = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'listenerMethod')
                if (!listenerMethod)
                    listenerMethod = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_LISTENER_METHOD

                def xmppCommandPrefix = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'xmppCommandPrefix')
                if (!xmppCommandPrefix)
                    xmppCommandPrefix = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_COMMAND_PREFIX

                def xmppCommandMethodSuffix = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'xmppCommandMethodSuffix')
                if (!xmppCommandMethodSuffix)
                    xmppCommandMethodSuffix = org.grails.xmpp.MessageListenerAdapter.ORIGINAL_DEFAULT_COMMAND_METHOD_SUFFIX

                "${sName}XmppMessageListener"(org.grails.xmpp.MessageListenerAdapter, ref("${service.propertyName}")) {
                    defaultListenerMethod = listenerMethod
                    defaultXmppCommandPrefix = xmppCommandPrefix
                    defaultXmppCommandMethodSuffix = xmppCommandMethodSuffix
                }
                
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->

        application.serviceClasses?.each { service ->
            def serviceClass = service.getClazz()
            def exposeList = GrailsClassUtils.getStaticPropertyValue(serviceClass, 'expose')
            if (exposeList!=null && exposeList.contains('xmpp')) {
                println ">>>> starting XMPP listener for ${service.shortName}"

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

                def sName = service.propertyName.replaceFirst("Service","")
                applicationContext.xmppAgent.packetListeners.add(applicationContext.getBean("${sName}XmppMessageListener"))

            }
	}


        // TODO Checks all services and controllers which implement
        // PackageListener or MessageListener and add to xmppconnection
        //        for(serviceClass in application.serviceClasses) {
        //            if(PacketListener.class.isAssignableFrom(serviceClass.clazz)) {
        //
        //                def myListener = [processPacket:{ packet ->
        //                        println "Received message from ${packet.from}, subject: ${packet.subject}, body: ${packet.body}"
        //
        //                }] as PacketListener
        //
        //                def svc = applicationContext.getBean("${serviceClass.propertyName}")
        //                applicationContext.xmppAgent.packetListeners.add(myListener)
        //                applicationContext.xmppAgent.packetListeners.add(svc)
        //                //TODO how to add new property to a GrailsService
        //                //serviceClass.metaClass.xmppAgent << {-> applicationContext.xmppAgent }
        //            }
        //        }
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