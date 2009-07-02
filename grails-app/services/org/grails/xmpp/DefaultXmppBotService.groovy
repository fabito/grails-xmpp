package org.grails.xmpp

class DefaultXmppBotService {

    boolean transactional = false
    static expose = ['xmpp']

    def helpXmppCommand(msg){
    	onXmppMessage(m)
    }

    def inviteXmppCommand(msg){
    	println "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"  
    	sendXmppMessage(msg.from, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")  
    }

    def listAllXmppCommand(msg){
    	def entries = getAllXmppContacts()
    	sendXmppMessage(msg.from, entries)
    }
    
    def onXmppMessage(m) {
    	def help = """Available commands are: 
@help
@invite
@listAll
"""
    	sendXmppMessage(m.from, help)
    }

}