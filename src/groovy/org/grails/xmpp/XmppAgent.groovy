package org.grails.xmpp


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 *
 * @author fabito
 */
class XmppAgent {

    private static final Log log = LogFactory.getLog(XmppAgent.class);

    String username = null;
    String password = null;
    XMPPConnection connection = null;
    Roster roster = null;
    Map<String, Chat> chats = null;
    ChatManager chatManager = null;
    ConnectionConfiguration connectionConfiguration = null;
    List<PacketListener> packetListeners = []


    public void destroy() throws Exception {
        Presence unavailablePresence = new Presence(Presence.Type.unavailable);
        unavailablePresence
        .setStatus("I´m down, sorry for the inconvenience...");
        this.connection.disconnect(unavailablePresence);
    }

    def connect(){
        //log.info(this.connectionConfiguration.toString());
        //this.connection = new XMPPConnection(this.connectionConfiguration);

        log.info("estabilishing connection")
        this.connection.connect();

        log.info("Conexao realizada com sucesso.");

        log.info("Fazendo login com usuario: " + username);
        connection.login(this.username, this.password);

        log.info("Adding listeners.");
        packetListeners.each {
            println "Adding "
            assert it instanceof PacketListener
            this.connection.addPacketListener(it as PacketListener, null)
        }

        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Roger that!");

        log.info("Notificando contatos");
        this.connection.sendPacket(presence);


        this.roster = connection.getRoster();
        //this.roster.addRosterListener(this);
        this.chatManager = connection.getChatManager();
        //this.chatManager.addChatListener(this);
        this.chats = new HashMap<String, Chat>();
    }

    def disconnect(){
        Presence unavailablePresence = new Presence(Presence.Type.unavailable);
        unavailablePresence
                        .setStatus("I´m down, sorry for the inconvenience...");
        this.connection.disconnect(unavailablePresence);
    }
	
}