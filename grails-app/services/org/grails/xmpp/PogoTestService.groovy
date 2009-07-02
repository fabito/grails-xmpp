package org.grails.xmpp

class PogoTestService {

    static expose = ['xmpp']
    static xmppCommandPrefix = "!"
    static xmppCommandMethodSuffix = ""
    static listenerMethod = "customOnXmppMessage"

    boolean transactional = false

    def help(p) {
        println "HELPING!!!"
    }

    def ls(p) {
    	println "LSING"
    }

    def find(p) {
    	println "FINDING"
    	println p.type
    }
    
    def customOnXmppMessage(m) {
    	println "CAIU NO customOnXmppMessage"
    	println m.toXML()
    }

}