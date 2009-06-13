package org.grails.xmpp

import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.Chat

import org.jivesoftware.smack.packet.Message

class MyService {

    static expose = ['xmpp']
    static xmppCommandPrefix = "#"
    static xmppCommandMethodSuffix = "XmppCommand"

    def xmppService
    def xmppAgent

    boolean transactional = false

    def serviceMethod() {
    }

    void processPacket(Packet packet) {
        if (packet instanceof Message) {
            Message m = (Message) packet
            processMessage(m);
        }
    }

    def processMessage(m) {
        println m.body
        xmppService.sendMessage(m.from, m.body)
    }

    def helpXmppCommand(Packet packet){
    	log.info "helping!!!!"
        if (packet instanceof Message) {
            Message m = (Message) packet
            xmppService.removeContact(xmppAgent, m.from)
        }
    }

    def inviteXmppCommand(Packet packet){
    	println "inviting!!!!"
        if (packet instanceof Message) {
            Message m = (Message) packet
            xmppService.sendInvitation(xmppAgent, "fabio.uechi@jabber.org")
        }
    }

    def listAllXmppCommand(Packet packet){
        if (packet instanceof Message) {
            Message m = (Message) packet
            def list = xmppService.getContactList(xmppAgent)
            xmppService.sendMessage(xmppAgent, m.from, list.toString())
        }
    }


}