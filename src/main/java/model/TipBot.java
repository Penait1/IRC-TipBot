package model;

import java.util.concurrent.TimeUnit;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.NoticeEvent;

public class TipBot extends PircBotX{
    
    public TipBot(Configuration c) {
        super(c);
    }

    public boolean isUserIdentified(String targetNick) throws InterruptedException, NullPointerException {
        boolean identified = false;

        // Checking via chat if the user is logged in
        WaitForQueue queue = new WaitForQueue(this);
        this.sendIRC().message("nickserv", "acc " + targetNick);
        NoticeEvent currentEvent = queue.waitFor(NoticeEvent.class, 10, TimeUnit.SECONDS);
        queue.close();

        // Getting the login status from the user
        String[] split = currentEvent.getMessage().split(" ");
        int parseInt = Integer.parseInt(split[2]);

        // 3 means the user is logged in and everything is fine
        if (parseInt == 3) {
            identified = true;
        }
        return identified;
    }
}
