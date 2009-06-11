package org.grails.xmpp;

/**
 *
 * @author fabito
 */
class ClosureMessageListenerAdapter extends MessageListenerAdapter {

	ClosureMessageListenerAdapter(delegate) {
		super(delegate)
	}

	protected Object invokeListenerMethod(String methodName, Object[] arguments) {
		def closure = null
		try {
			closure = getDelegate()[methodName]
		} catch (Exception e) {}

		if (closure) {
			closure.call(arguments)
		} else {
			super.invokeListenerMethod(methodName, arguments)
		}
	}

}
