package org.grails.xmpp

import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.Chat


import org.jivesoftware.smack.packet.Message

class MyService implements PacketListener {

    static expose = ['xmpp']
    static xmppCommandPrefix = "#"
    static xmppCommandMethodSuffix = "XmppCommand"

    def xmppService

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
        processPacket(packet)
    }


}