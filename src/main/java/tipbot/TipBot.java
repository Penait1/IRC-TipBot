package tipbot;

import config.ReadConfiguration;
import connectivity.TransactionSocket;
import connectivity.ValidateCache;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.BasicConfigurator;
import org.pircbotx.Configuration;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.managers.BackgroundListenerManager;

public class TipBot {

    //DB connection
    private static EntityManagerFactory ENTITY_MANAGER_FACTORY;

    //Configuration variables, to change these edit config.properties 
    public static String BOT_USERNAME;
    private static String CHANNEL;
    public static String NICKSERV_PASSWORD;
    public static int MESSAGE_DELAY;

    public static BigDecimal FEE;
    public static BigDecimal MIN_WITHDRAW;
    public static String CURRENCY;
    public static int MIN_DECIMALS;

    public static String DB_IP;
    public static String DB_USERNAME;
    public static String DB_PASSWORD;

    //Other stuff
    private static BackgroundListenerManager manager = new BackgroundListenerManager();

    private static model.TipBot bot;

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        loadConfig();
        
        Map<String, Object> hibernateConfigOverrides = new HashMap<String, Object>();

        hibernateConfigOverrides.put("javax.persistence.jdbc.url", DB_IP);
        hibernateConfigOverrides.put("javax.persistence.jdbc.user", DB_USERNAME);
        hibernateConfigOverrides.put("javax.persistence.jdbc.password", DB_PASSWORD);

        ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("blah", hibernateConfigOverrides);

        Configuration configuration = new Configuration.Builder()
                .setName(BOT_USERNAME) //Set the nick of the bot. CHANGE IN YOUR CODE
                .addServer("irc.freenode.net") //Join the freenode network           
                .setNickservDelayJoin(true)
                .addAutoJoinChannel(CHANNEL) //Join the official #pircbotx channel
                .setListenerManager(manager)
                .addListener(new CacheListener())
                .setLogin("Penait1")
                .setNickservPassword(NICKSERV_PASSWORD)
                .setAutoReconnect(true)
                .setAutoReconnectDelay(600)
                .setAutoReconnectAttempts(10000)
                .buildConfiguration();

        bot = new model.TipBot(configuration);

        try {
            manager.addListener(new BasicCommands(), true);
            manager.addListener(new AdminCommands(), true);

        } catch (SecurityException ex) {
            Logger.getLogger(TipBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new ValidateCache(), 60, 60, TimeUnit.MINUTES);
        Executors.newSingleThreadExecutor().submit(new TransactionSocket());

        try {
            bot.startBot();
        } catch (IOException ex) {
            Logger.getLogger(TipBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IrcException ex) {
            Logger.getLogger(TipBot.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static model.TipBot getBot() {
        return bot;
    }

    public static void loadConfig() {
        ReadConfiguration properties = new ReadConfiguration();
        try {
            properties.getPropValues();
        } catch (IOException ex) {
            Logger.getLogger(TipBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static EntityManagerFactory getENTITY_MANAGER_FACTORY() {
        return ENTITY_MANAGER_FACTORY;
    }

    public static String getCHANNEL() {
        return CHANNEL;
    }

    public static void setCHANNEL(String CHANNEL) {
        TipBot.CHANNEL = CHANNEL;
    }
}
