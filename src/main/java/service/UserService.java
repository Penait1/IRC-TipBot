package service;

import connectivity.Wallet;
import exception.WalletConnectionException;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;

import model.Tip;
import model.Transaction;
import model.User;
import tipbot.BasicCommands;
import tipbot.TipBot;

public class UserService {

    private Wallet wallet;
    public final static BigDecimal FEE = BasicCommands.FEE;
    public final static String CURRENCY = BasicCommands.CURRENCY;

    public UserService() {
        this.wallet = new Wallet();
    }

    /**
     * Retrieves the current deposit address from an user.
     *
     * @param user
     * @return The users deposit address
     */
    public String getAddress(User user) throws WalletConnectionException {
        if (user.getAddress() != null) {
            return user.getAddress();
        }
        EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        entityManager.getTransaction().begin();
        user.setAddress(this.wallet.generateNewAddress(user));

        if (!entityManager.contains(user)) {
            entityManager.merge(user);
        }

        entityManager.getTransaction().commit();
        entityManager.close();
        return user.getAddress();
    }

    /**
     * Processes a withdrawel issued by an user. Updates the balance accordingly from the user.
     * The difference between the withdrawel fee and transaction fee will be added to the balance of the bot.
     *
     *
     * @param user
     * @param address
     * @param amount
     * @return transactionId from the sent transaction
     * @throws WalletConnectionException
     */
    public String processWithdrawel(User user, String address, BigDecimal amount) throws WalletConnectionException {
        String transactionId = wallet.sentCoinsToAddress(address, amount);

        //If the coins haven't been send because of an error
        if (transactionId == null) {
            return null;
        } else {
            /*If the coins have been send: Update it in the database
                and generate return message*/
            EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
            entityManager.getTransaction().begin();
            user.setCoins(user.getCoins().subtract(amount));

            //Retrieving withdrawel fee - fee
            Transaction transaction = wallet.getTransaction(transactionId);
            User bot = getOrCreateUser(TipBot.getBot().getNick());
            BigDecimal nettoWithdraw = TipBot.FEE.add(transaction.getFee());
            bot.setCoins(bot.getCoins().add(nettoWithdraw));

            if (!entityManager.contains(user)) {
                entityManager.merge(user);
            }

            if (!entityManager.contains(bot)) {
                entityManager.merge(bot);
            }

            entityManager.getTransaction().commit();
            entityManager.close();

        }
        return transactionId;
    }

    /**
     * Updates the balance from the tipping user and the tipped user. Amount is
     * substracted from the tipping user and added to the tipped user
     */
    public void giveTip(Tip tip) {

        EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        entityManager.getTransaction().begin();


        tip.getSender().setCoins(tip.getSender().getCoins().subtract(tip.getAmount()));
        tip.getReceiver().setCoins(tip.getReceiver().getCoins().add(tip.getAmount()));

        if (!entityManager.contains(tip.getSender())) {
            entityManager.merge(tip.getSender());
        }
        if (!entityManager.contains(tip.getReceiver())) {
            entityManager.merge(tip.getReceiver());
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    /**
     * Sets the logged in state from an user. This means he/she is or isn't
     * logged in with Nickserv in the channel. Used for caching purposes.
     *
     * @param user
     * @param loggedIn
     */
    public void setLoggedIn(User user, boolean loggedIn) {
        EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        entityManager.getTransaction().begin();
        user.setLogged_in(loggedIn);

        if (!entityManager.contains(user)) {
            entityManager.merge(user);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    /**
     * Checks if an user exists with the given username. If an user exists
     * return the user object, if not we create a new user and return the user
     * object.
     *
     * @param username
     * @return
     */
    public User getOrCreateUser(String username) {
        EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        User user = entityManager.find(User.class, username);

        if (user != null) {
            entityManager.close();
            return user;
        }
        User newUser = new User(username);
        entityManager.getTransaction().begin();
        entityManager.persist(newUser);
        entityManager.getTransaction().commit();
        entityManager.close();

        return newUser;
    }

    /**
     * Checks if there exists an user in the database that has a negative
     * balance. If this returns true it is a bad thing!
     *
     * @return True if someone in the database has a negative balance, false if
     * not.
     */
    public boolean isNegativeBalancePresent() {
        EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        List result = entityManager.createQuery("from User where coins < 0").getResultList();
        entityManager.close();
        return !result.isEmpty();
    }

    /**
     * Reset all logged_in statusus to false
     */
    public void resetLoggedIn() {
        EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("update User u set u.logged_in = :logged_in").setParameter("logged_in", false).executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    /**
     * Method to find the user which a deposit address belongs to
     *
     * @param depositAddress
     * @return User which belongs to the deposit address
     */
    public User getUserbelongingToDepositAddress(String depositAddress) {
        EntityManager entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        List resultList = entityManager.createQuery("FROM User where address = :address").setParameter("address", depositAddress).getResultList();
        entityManager.close();

        if (!resultList.isEmpty()) {
            return (User) resultList.get(0);
        }
        return null;
    }
}
