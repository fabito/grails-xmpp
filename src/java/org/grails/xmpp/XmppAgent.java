package org.grails.xmpp;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

//import br.com.ciandt.gtalk.event.RosterPresenceChangedEvent;
/**
 * XMPP Agent
 *
 * Conecta num servidor de IM (jabber) e gerencia as presenças e mensagens
 * trocadas com todos os Rosters
 *
 * Publica o evento de alteração de presença.
 *
 * TODO Notify command
 * TODO Job para checar atualziacoes em blog.
 *
 * @author fuechi
 *
 */
public class XmppAgent implements InitializingBean,
        ApplicationContextAware, RosterListener, MessageListener,
        ChatManagerListener {

    private static final Log log = LogFactory.getLog(XmppAgent.class);
    private String username = null;
    private String password = null;
    private XMPPConnection connection = null;
    private Roster roster = null;
    private Map<String, Chat> chats = null;
    private ChatManager chatManager = null;
    private ConnectionConfiguration connectionConfiguration = null;
    private ApplicationContext applicationContext = null;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.connectionConfiguration,
                "connectionConfiguration é obrigatório");
        Assert.notNull(this.username, "Username é obrigatório");
        Assert.notNull(this.password, "Password é obrigatório");

        log.info(this.connectionConfiguration.toString());
        this.connection = new XMPPConnection(this.connectionConfiguration);
        try {
            this.connection.connect();
        } catch (XMPPException e) {
            log.error(e);
            return;
        }
        log.info("Conexao realizada com sucesso.");

        log.info("Fazendo login com usuario: " + username);
        connection.login(this.username, this.password);

        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Roger that!");

        log.info("Notificando contatos");
        this.connection.sendPacket(presence);

        this.roster = connection.getRoster();
        this.roster.addRosterListener(this);
        this.chatManager = connection.getChatManager();
        this.chatManager.addChatListener(this);
        this.chats = new HashMap<String, Chat>();

    }

    public void destroy() throws Exception {
        Presence unavailablePresence = new Presence(Presence.Type.unavailable);
        unavailablePresence.setStatus("I´m down, sorry for the inconvenience...");
        this.connection.disconnect(unavailablePresence);
    }

    public void entriesAdded(Collection<String> entries) {
        log.info("entriesAdded " + entries);
        for (String entry : entries) {
        }
    }

    public void entriesDeleted(Collection<String> entries) {
        log.info("entriesDeleted " + entries);
        for (String entry : entries) {
        }
    }

    public void entriesUpdated(Collection<String> entries) {
        log.info("entriesUpdated " + entries);
        for (String entry : entries) {
        }
    }

    public void presenceChanged(Presence presence) {
        log.info(ToStringBuilder.reflectionToString(presence,
                ToStringStyle.MULTI_LINE_STYLE));

//		presenceChangedEvent.setPresence(presence);
//		applicationContext.publishEvent(presenceChangedEvent);

        String from = presence.getFrom();
        if (presence.isAvailable()) {
            if (!this.chats.containsKey(from)) {
                Chat chat = chatManager.createChat(from, this);
                this.chats.put(from, chat);
                log.info("chat criado para " + from);
                try {

                    chat.sendMessage("Welcome to The Green Team!! Type 'help' for the command list.");

                } catch (XMPPException e) {
                    log.error("Erro no enviando boas vindas.", e);
                }
            }
        } else {
            if (this.chats.containsKey(from)) {
                this.chats.remove(from);
                log.info("chat removido para " + from);
            }
        }
    }

    public void processMessage(Chat chat, Message message) {
        String messageBody = message.getBody();
        log.info("Mensagem recebida de: " + message.getFrom());
        log.info(messageBody);

        try {
            if (messageBody != null) {
                if (messageBody.trim().equalsIgnoreCase("help")) {

                    StringBuffer help = new StringBuffer();
                    help.append("help - this command\n");
                    help.append("exec - allows you to execute groovy scripts in the server. " +
                            "The stdout is sent back to you via IM.\n" +
                            "Example: exec println 'Groooovy Rocks!!!'");

                    chat.sendMessage(help.toString());

                } else if (messageBody.startsWith("exec")) {

                    Binding binding = new Binding();
                    binding.setVariable("agent", this);

                    StringWriter out = new StringWriter();
                    PrintWriter pw = new PrintWriter(out);

                    GroovyShell shell = new GroovyShell(binding);
                    shell.setVariable("out", pw);

                    try {

                        Object output = shell.evaluate(StringUtils.substringAfter(messageBody, "exec"));
                        if (output != null) {
                            pw.println(output);
                        }

                    } catch (Throwable t) {
                        t.printStackTrace(pw);
                    }

                    chat.sendMessage(out.toString());

                } else {
                    chat.sendMessage(messageBody);
                }
            }
        } catch (XMPPException e) {
            log.error("Erro no envio da resposta.", e);
        } catch (CompilationFailedException c) {
        }
        // enviar para MessageProcessor
    }

    public void chatCreated(Chat chat, boolean createdLocally) {
        log.info("Chat iniciado:" + ToStringBuilder.reflectionToString(chat,
                ToStringStyle.MULTI_LINE_STYLE));
        // if (createdLocally) {
        chat.addMessageListener(this);
        this.chats.put(chat.getParticipant(), chat);
        // }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    public void setConnectionConfiguration(
            ConnectionConfiguration connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void sendMessage(String userJID, String message)
            throws XMPPException {
        Chat chat;
        if (this.chats.containsKey(userJID)) {
            chat = this.chats.get(userJID);
        } else {
            chat = this.chatManager.createChat(userJID, this);
        }
        chat.sendMessage(message);
    }

    public void changeStatus(String newStatus) {
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus(newStatus);
        log.info("Changing status to " + newStatus);
        this.connection.sendPacket(presence);
    }

    public Roster getRoster() {
        return roster;
    }
}
