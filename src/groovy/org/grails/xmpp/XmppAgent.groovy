package org.grails.xmpp

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.ChatManager
import org.jivesoftware.smack.ChatManagerListener
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.proxy.ProxyInfo
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.Roster
import org.jivesoftware.smack.RosterListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.net.InetAddress
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.jivesoftware.smack.SASLAuthentication
import org.jivesoftware.smack.filter.*
import org.springframework.beans.factory.InitializingBean
import org.jivesoftware.smack.ReconnectionManager


/**
 *
 * @author fabito
 */
class XmppAgent implements InitializingBean  {

    final Log log = LogFactory.getLog(XmppAgent.class);

    String username = null;
    String password = null;
    XMPPConnection connection = null;
    Roster roster = null;
    ConnectionConfiguration connectionConfiguration = null;
    List<PacketListener> packetListeners = []
    List<RosterListener> rosterListeners = []
    ProxyInfo proxyInfo	

    PacketFilter msgFilter = null;

	
    public void destroy() throws Exception {
        Presence unavailablePresence = new Presence(Presence.Type.unavailable)
        unavailablePresence.setStatus("I´m down, sorry for the inconvenience...")
        this.connection.disconnect(unavailablePresence)
    }

    def disconnect(){
    	destroy()
    }
    
    def connect(){
    	if (this.connection.isConnected())
    		return

    	log.info("Estabilishing connection...")
        this.connection.connect()
        log.info("Successfully connected")

		/*
		 * TODO verify why the hell this is raising 
		 * "Could not find which method <init>() to invoke from this list" 
		 
    	ReconnectionManager recMgr = new ReconnectionManager()
    	this.connection.addConnectionListener(recMgr)
    	
    	*/

        SASLAuthentication.supportSASLMechanism("PLAIN", 0)
        log.info("Logging in with user: " + username)
        def hostName = 'unknown';
        try {
                hostName = InetAddress.getLocalHost().getHostName();
        } catch ( Exception e )
        {
        }
        def appName = ConfigurationHolder.config.grails.project.groupId;       
        
        connection.login(this.username, this.password, appName +'@' + hostName + '-' + Long.toHexString(System.currentTimeMillis()))
        registerListeners()
    }
    
    def registerListeners = {
        log.info("Registering PacketListeners.");
        packetListeners.each { listener ->
        	log.info "Adding " + (listener instanceof MessageListenerAdapter ? listener.delegate.getClass() : listener.getClass())
            assert listener instanceof PacketListener
        	this.connection.addPacketListener(listener as PacketListener, msgFilter)
        }
        this.roster = connection.getRoster()
        log.info("Registering RosterListeners.");
        rosterListeners.each { listener ->
            log.info "Adding " + listener.getClass()
            this.roster.addRosterListener(listener as RosterListener)
        }
    }
    
    def unregisterListeners = {
            log.info("Unregistering PacketListeners.");
            packetListeners.each { listener ->
            	log.info "Removing " + (listener instanceof MessageListenerAdapter ? listener.delegate.getClass() : listener.getClass())
            	this.connection.removePacketListener(listener)
            }
            this.roster = connection.getRoster()
            log.info("Unregistering RosterListeners.");
            rosterListeners.each { listener ->
                log.info "Removing " + listener.getClass()
                this.roster.removeRosterListener(listener)
            }
    	packetListeners = []
    	rosterListeners = []
    }
	
    void afterPropertiesSet() {
    	msgFilter = new AndFilter()
    	msgFilter.addFilter(new MessageTypeFilter(Message.Type.chat) as PacketFilter)
    	msgFilter.addFilter(new NotFilter(new PacketExtensionFilter("paused", "http://jabber.org/protocol/chatstates")) as PacketFilter )
    	msgFilter.addFilter(new NotFilter(new PacketExtensionFilter("composing", "http://jabber.org/protocol/chatstates")) as PacketFilter )
    }


}
