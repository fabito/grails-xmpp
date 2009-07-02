package org.grails.xmpp

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.RosterListenerimport org.jivesoftware.smack.packet.Presence
import org.apache.commons.lang.StringUtils

/**
 * Provides Basic XMPP Services
 */
class XmppService { 
	
	boolean transactional = false
	
	def sendInvitation = { xmppAgent, to ->
		if (to instanceof List) {
			to.each { user ->
				xmppAgent.roster.createEntry(user, user, null)
			}
		} else {
			xmppAgent.roster.createEntry(to, to, null)	
		}
	}
	
	def removeContact = { xmppAgent, to ->
	
		if (to instanceof List) {
			to.each { user ->
		    	user = StringUtils.substringBefore(user,"/")
			    println "removing ${user}"
				xmppAgent.roster.removeEntry(xmppAgent.roster.getEntry(user))
			}
		} else {
			to = StringUtils.substringBefore(to,"/")
			println "looking for entry ${to}"
			def entry = xmppAgent.roster.getEntry(to)
			if (entry) {
				println "found ${to}"
				xmppAgent.roster.removeEntry(entry)
			}				
			else {
				println "not found entry ${to}"
			}
				
		}
	}
	
	def sendMessage = { xmppAgent, to, text ->
		try{
			Chat chat = xmppAgent.connection.chatManager.createChat(to, null)
			def msgObj = new Message(to, Message.Type.chat)
			msgObj.body = text
			
			if (to instanceof List) {
				to.each { addr ->
					msgObj.to = addr
					chat.sendMessage(msgObj)
				}
			} else {
				chat.sendMessage(msgObj)	
			}
			
		} catch (Exception e) {
			log.error "Failed to send message", e
		}
	}
	
	def getContactList = { xmppAgent ->
		return xmppAgent.roster.entries
	}
	
	def changeStatus = { xmppAgent, newStatus, presenceType ->
		Presence presence = new Presence(presenceType);
		presence.setStatus(newStatus);
		xmppAgent.connection.sendPacket(presence);
	}
	
}