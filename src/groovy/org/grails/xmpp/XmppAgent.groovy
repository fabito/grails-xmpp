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
import org.jivesoftware.smack.SASLAuthenticationimport org.jivesoftware.smack.filter.MessageTypeFilterimport org.springframework.beans.factory.InitializingBeanimport org.jivesoftware.smack.ReconnectionManager

/**
 *
 * @author fabito
 */
class XmppAgent implements InitializingBean {

    private static final Log log = LogFactory.getLog(XmppAgent.class);

    String username = null;
    String password = null;
    XMPPConnection connection = null;
    Roster roster = null;
    Map<String, Chat> chats = null;
    ChatManager chatManager = null;
    ConnectionConfiguration connectionConfiguration = null;
    List<PacketListener> packetListeners = []
    List<RosterListener> rosterListeners = []
    ProxyInfo proxyInfo

    public void destroy() throws Exception {
        Presence unavailablePresence = new Presence(Presence.Type.unavailable)
        unavailablePresence.setStatus("I´m down, sorry for the inconvenience...")
        this.connection.disconnect(unavailablePresence)
    }
 
    def connect(){
        log.info(this.connectionConfiguration.toString());
        //this.connection = new XMPPConnection(this.connectionConfiguration);

        log.info("estabilishing connection")
        this.connection.connect()
        log.info("Connected")

        SASLAuthentication.supportSASLMechanism("PLAIN", 0)
        
        log.info("Logging in with user: " + username)
        connection.login(this.username, this.password, this.username + Long.toHexString(System.currentTimeMillis()))

        log.info("Adding Packet listeners.");
        packetListeners.each { listener ->
            log.info "Adding " + listener.getClass()
            assert listener instanceof PacketListener
            //this.connection.addPacketListener(listener as PacketListener, new MessageTypeFilter())
            this.connection.addPacketListener(listener as PacketListener, null)
        }

        this.roster = connection.getRoster()
        log.info("Adding Roster listeners.");
        rosterListeners.each { listener ->
            log.info "Adding " + listener.getClass()
            this.roster.addRosterListener(listener as RosterListener)
        }
        
        this.chatManager = connection.getChatManager()
        //this.chatManager.addChatListener(this)
        this.chats = new HashMap<String, Chat>()
    }

    def disconnect(){
        Presence unavailablePresence = new Presence(Presence.Type.unavailable)
        unavailablePresence.setStatus("I´m down, sorry for the inconvenience...")
        this.connection.disconnect(unavailablePresence)
    }
	
    void afterPropertiesSet() {
//    	ReconnectionManager recMgr = new ReconnectionManager()
//    	this.connection.addConnectionListener(recMgr)
    }

    
}