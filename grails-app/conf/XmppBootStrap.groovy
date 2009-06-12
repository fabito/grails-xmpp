import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * Estabilish xmpp connection at application startup and interrupt it during shutdown
 * @author fabito
 */
class XmppBootStrap {

    //def xmppAgent

    def init = { servletContext ->
/*        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
        xmppAgent = ctx.xmppAgent
        if(ConfigurationHolder.config?.xmpp?.autoStartup) xmppAgent.connect()
*/    }

    def destroy = {
  /*      xmppAgent.disconnect();*/
    }


}