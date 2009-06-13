package org.grails.xmpp

import org.jivesoftware.smack.RosterListenerimport org.jivesoftware.smack.packet.Presence
/**
 * @author fabito
 *
 */
class RosterListenerImplementerService implements RosterListener {
	
	boolean transactional = false
	
	/**
	 * Called when roster entries are added.
	 */
	void entriesAdded(Collection<String> addresses) {
		println addresses
	}
	
	/**
	 * Called when a roster entries are removed.
	 */
	void entriesDeleted(Collection<String> addresses) {
		println addresses
	}
	
	/**
	 * Called when a roster entries are updated.
	 */
	void entriesUpdated(Collection<String> addresses) {
		println addresses
	}
	
	/**
	 * Called when the presence of a roster entry is changed.
	 */
	void presenceChanged(Presence presence) {
		println presence.toXML()
	}
	
}
