package org.grails.xmpp;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;

/**
 *
 * @author fabito
 */
public class MessageListenerAdapter implements MessageListener, PacketListener {

    public static final String ORIGINAL_DEFAULT_COMMAND_PREFIX = "@";
    public static final String ORIGINAL_DEFAULT_COMMAND_METHOD_SUFFIX = "XmppCommand";
    /**
     * Out-of-the-box value for the default listener method: "handleMessage".
     */
    public static final String ORIGINAL_DEFAULT_LISTENER_METHOD = "handleMessage";
    private String defaultListenerMethod = ORIGINAL_DEFAULT_LISTENER_METHOD;
    private String defaultXmppCommandPrefix = ORIGINAL_DEFAULT_COMMAND_PREFIX;
    private String defaultXmppCommandMethodSuffix = ORIGINAL_DEFAULT_COMMAND_METHOD_SUFFIX;
    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
    private Object delegate;

    public MessageListenerAdapter(Object delegate) {
        this.delegate = delegate;
    }

    public void processMessage(Chat chat, Message message) {
        try {
            onMessage(chat, message);
        } catch (Throwable ex) {
            handleListenerException(ex);
        }
    }

    public void processPacket(Packet packet) {
        try {
            if (packet instanceof Message) {
                onMessage(null, (Message) packet);
            } else {
                logger.debug(packet.toXML());
                invokeListenerMethod(getDefaultListenerMethod(), new Object[]{packet});
            }
        } catch (Throwable ex) {
            handleListenerException(ex);
        }
    }

    private void onMessage(Chat chat, Message message) throws Throwable {

        String body = message.getBody();

        if (StringUtils.isNotEmpty(body) && body.startsWith(getDefaultXmppCommandPrefix())) {

            String methodName = body.substring(1);
            if (body.contains(" ")) {
                methodName = StringUtils.substringBetween(body, getDefaultXmppCommandPrefix(), " ");
            }

            if (StringUtils.isNotEmpty(methodName)) {
                methodName+=getDefaultXmppCommandMethodSuffix();
                if (chat == null) {
                    invokeListenerMethod(methodName, new Object[]{(Packet) message});
                } else {
                    invokeListenerMethod(methodName, new Object[]{chat, message});
                }
            }

        } else {
            logger.debug("ignore??");
            if (chat == null) {
                invokeListenerMethod(getDefaultListenerMethod(), new Object[]{(Packet) message});
            } else {
                invokeListenerMethod(getDefaultListenerMethod(), new Object[]{chat, message});
            }
        }

    }

    private void handleListenerException(Throwable ex) {
        if (ex instanceof NoSuchMethodException) {
            logger.warn("");
        }
        logger.error(ex);
    }

    protected Object invokeListenerMethod(String methodName, Object[] arguments) throws Throwable {

        if (logger.isDebugEnabled()) {
            logger.debug("invoking method " + methodName+ " on " + delegate.getClass());    
        }
        
        try {
            MethodInvoker methodInvoker = new MethodInvoker();
            methodInvoker.setTargetObject(delegate);
            methodInvoker.setTargetMethod(methodName);
            methodInvoker.setArguments(arguments);
            methodInvoker.prepare();
            return methodInvoker.invoke();
        } catch (InvocationTargetException ex) {
            Throwable targetEx = ex.getTargetException();
            throw targetEx;
        }
    }

    /**
     * Set a target object to delegate message listening to.
     * Specified listener methods have to be present on this target object.
     * <p>If no explicit delegate object has been specified, listener
     * methods are expected to present on this adapter instance, that is,
     * on a custom subclass of this adapter, defining listener methods.
     */
    public void setDelegate(Object delegate) {
        Assert.notNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
    }

    /**
     * Return the target object to delegate message listening to.
     */
    protected Object getDelegate() {
        return this.delegate;
    }

    /**
     * Specify the name of the default listener method to delegate to,
     * for the case where no specific listener method has been determined.
     * Out-of-the-box value is {@link #ORIGINAL_DEFAULT_LISTENER_METHOD "handleMessage"}.
     * @see #getListenerMethodName
     */
    public void setDefaultListenerMethod(String defaultListenerMethod) {
        this.defaultListenerMethod = defaultListenerMethod;
    }

    /**
     * Return the name of the default listener method to delegate to.
     */
    protected String getDefaultListenerMethod() {
        return this.defaultListenerMethod;
    }

    protected String getDefaultXmppCommandMethodSuffix() {
        return defaultXmppCommandMethodSuffix;
    }

    public void setDefaultXmppCommandMethodSuffix(String defaultXmppCommandMethodSuffix) {
        this.defaultXmppCommandMethodSuffix = defaultXmppCommandMethodSuffix;
    }

    protected String getDefaultXmppCommandPrefix() {
        return defaultXmppCommandPrefix;
    }

    public void setDefaultXmppCommandPrefix(String defaultXmppCommandPrefix) {
        this.defaultXmppCommandPrefix = defaultXmppCommandPrefix;
    }
}
