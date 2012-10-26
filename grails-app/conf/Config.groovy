xmpp.autoStartup = true
xmpp.username="grails-xmpp"
xmpp.password="grails"
xmpp.connection.host="jabber.org"
xmpp.connection.port=5222
xmpp.connection.service="jabber.org"
xmpp.connection.SASLAuthenticationEnabled=true

//xmpp.autoStartup = true
//xmpp.username="greenteam.brightzone"
//xmpp.password="timeverde"
//xmpp.connection.host="talk.google.com"
//xmpp.connection.port=5222
//xmpp.connection.service="gmail.com"
//xmpp.connection.SASLAuthenticationEnabled=false

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    }

    debug 'org.grails.xmpp'

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	       'org.codehaus.groovy.grails.web.pages', //  GSP
	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails."web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails."web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	       'org.springframework',
	       'org.hibernate'

    warn   'org.mortbay.log'
}


// The following properties have been added by the Upgrade process...
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
