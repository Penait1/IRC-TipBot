package tipbot;

import model.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import service.UserService;

public class CacheListener extends ListenerAdapter {

    private UserService userService;
    private model.TipBot bot;

    public CacheListener() {
        this.userService = new UserService();
        bot = TipBot.getBot();
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getMessage().startsWith("+")) {
            User user = userService.getOrCreateUser(event.getUser().getNick());     
            this.userService.setLoggedIn(user, true);

        }
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        boolean userIdentified = bot.isUserIdentified(event.getUser().getNick());

        //Checking if the user is logged in or someone is spoofing their nickname 
        if (userIdentified == true) {
            User user = userService.getOrCreateUser(event.getUser().getNick());
            this.userService.setLoggedIn(user, true);    
        }
    }

    @Override
    public void onQuit(QuitEvent event) throws Exception {
        User user = userService.getOrCreateUser(event.getUser().getNick());
        this.userService.setLoggedIn(user, false);
    }

    @Override
    public void onPart(PartEvent event) throws Exception {
        User user = userService.getOrCreateUser(event.getUser().getNick());
        this.userService.setLoggedIn(user, false);
    }

    @Override
    public void onKick(KickEvent event) throws Exception {
        User user = userService.getOrCreateUser(event.getUser().getNick());
        this.userService.setLoggedIn(user, false);
    }

    @Override
    public void onNickChange(NickChangeEvent event) throws Exception {
        User user = userService.getOrCreateUser(event.getOldNick());
        this.userService.setLoggedIn(user, false);

        boolean userIdentified = bot.isUserIdentified(event.getUser().getNick());

        //Checking if the user is logged in or someone is spoofing their nickname 
        if (userIdentified == true) {
            user = userService.getOrCreateUser(event.getNewNick());
            this.userService.setLoggedIn(user, true);
        }
    }
}
