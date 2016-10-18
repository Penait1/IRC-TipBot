package connectivity;

import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import javax.persistence.EntityManager;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;
import tipbot.TipBot;
import utility.Mail;

public class ValidateCache implements Runnable {
    public ValidateCache() {
    }


    @Override
    public void run() {
        try {
            UserChannelDao<User, Channel> userChannelDao = TipBot.getBot().getUserChannelDao();
            Channel channel = userChannelDao.getChannel(TipBot.getCHANNEL());
            ImmutableSortedSet<String> usersNicks = channel.getUsersNicks();

            EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
            List<model.User> users = entityManager.createQuery("SELECT u FROM User u WHERE u.logged_in = true").getResultList();

            for (model.User user : users) {
                if (!usersNicks.contains(user.getUsername())) {
                    entityManager.getTransaction().begin();
                    user.setLogged_in(false);

                    if (!entityManager.contains(user)) {
                        entityManager.merge(user);
                    }
                    entityManager.getTransaction().commit();
                }
            }
            entityManager.close();
        } catch (Exception e) {
            Mail mail = new Mail();
            mail.sendError(e);
        }
    }
}
