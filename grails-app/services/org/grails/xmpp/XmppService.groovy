package org.grails.xmpp

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Message

class XmppService {

    boolean transactional = false

    def xmppAgent


    def sendInvite(String to) {
    }
    
    def sendMessage(String to, String text) {
        try{
            Chat chat = xmppAgent.connection.chatManager.createChat(to, null)
            def msgObj = new Message(to, Message.Type.chat)
            msgObj.body = text
            chat.sendMessage(msgObj)
        } catch (Exception e) {
            log.error "Failed to send message", e
        }
    }

    def getContacts() {
        return xmppAgent.roster.entries
    }

    def setStatus(String newStatus){
    }


}
