package org.grails.xmpp

class PogoTestService {

    static expose = ['xmpp']
//      static xmppCommandPrefix = "@"
    static xmppCommandMethodSuffix = ""

    boolean transactional = false

    def help() {
        log.info "helping"
    }

    def ls() {
        log.info "lsing"

    }

    def find() {
        log.info "finding"
    }

}
