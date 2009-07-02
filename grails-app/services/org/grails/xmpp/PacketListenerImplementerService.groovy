package org.grails.xmpp

import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.packet.Packetclass PacketListenerImplementerService implements PacketListener {

	boolean transactional = false

	void processPacket(Packet packet) {
		println "TRALALALALLALALLALALAALALAL" 
	}

}
