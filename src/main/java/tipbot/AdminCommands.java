package tipbot;

import java.util.Arrays;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;

public class AdminCommands extends ListenerAdapter {
    String[] commands = new String[] {"!stop", "!say", "!rejoin", "!enable", "!disable"};
    private long timestamp;
    private final static int MESSAGE_DELAY = TipBot.MESSAGE_DELAY;
    
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
        String fullCommand = event.getMessage().substring(1);
        String[] commandSplit = fullCommand.split(" ");
        String command = commandSplit[0];

        // Spam prevention: 2 seconds delay needed
        if (Arrays.asList(commands).contains(command)) {
            if (((event.getTimestamp() / 1000) - timestamp) <= MESSAGE_DELAY) {
                return;
            } else {
                timestamp = (event.getTimestamp() / 1000);
            }
        }

        // Checking if the user is identified, if not; exit
        if (Arrays.asList(commands).contains(command) && !event.getMessage().startsWith("+") && event.getUser().isIrcop()) {
            event.respond("You need to be identified by NickServ to use this command!");
            return;
        }
        
        switch (command.toUpperCase()) {
            case "!STOP":
                event.getBot().stopBotReconnect();
                event.getBot().close();           
                break;
            case "!SAY":
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < commandSplit.length; i++) {
                    message.append(commandSplit[i]).append(" ");
                }
                event.getBot().sendIRC().message(TipBot.getCHANNEL(), message.toString());
                break;
            case "!REJOIN":
                event.getBot().sendRaw().rawLine("/join " + TipBot.getCHANNEL());
                break;
            case "!DISABLE":
                BasicCommands.addDisabledCommand(commandSplit[1]);
                event.respond("Command " + commandSplit[1] + " Disabled!");               
                break;
            case "!ENABLE":
                BasicCommands.removeDisabledCommand(commandSplit[1]);
                event.respond("Command " + commandSplit[1] + " Enabled!");
                break;
        }
    }  
}
