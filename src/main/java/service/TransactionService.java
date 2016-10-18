package service;

import connectivity.Wallet;
import exception.WalletConnectionException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

import model.Transaction;
import model.User;
import tipbot.TipBot;
import utility.Mail;
import utility.NotificationService;

public class TransactionService {

    private EntityManager entityManager;
    private final NotificationService notificationService;
    private final UserService userService;
    private final Wallet wallet;
    private final model.TipBot tipBot;
    private static int MIN_CONFIRMATIONS;
    private final Mail mail;

    public TransactionService() {
        this.wallet              = new Wallet();
        this.notificationService = new NotificationService();
        this.userService         = new UserService();
        this.mail                = new Mail();
        this.tipBot              = TipBot.getBot();
    }

    /**
     * Creates a transaction. Is called by the TransactionSocket on wallet notify.
     *
     * @param transactionId
     */
    public void createTransaction(String transactionId) {
        this.entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        try {
            List results = this.entityManager.createQuery("SELECT t FROM Transaction t WHERE t.transactionId = :transactionId").setParameter("transactionId", transactionId).getResultList();

            if (results.isEmpty()) {
                Transaction transaction = wallet.getTransaction(transactionId);
                
                boolean aReceiveTransaction = wallet.isAReceiveTransaction(transactionId);
                if (aReceiveTransaction == true) {
                    User user = userService.getUserbelongingToDepositAddress(transaction.getAddress());
                    transaction.setUser(user);
                }
                
                if (aReceiveTransaction == false) {
                    transaction.setCompleted(true);
                }
                
                this.entityManager.getTransaction().begin();
                this.entityManager.persist(transaction);
                this.entityManager.getTransaction().commit();
            }
            this.entityManager.close();
        } catch (WalletConnectionException ex) {
            Logger.getLogger(TransactionService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks every transaction which status is not completed for the amount of
     * confirmations. If the amount of confirmations is > min confirm amount we
     * process the deposit.
     */
    public void checkTransactionsForConfirmations() {
        this.entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        try {
            List<Transaction> resultList = this.entityManager.createQuery("SELECT t FROM Transaction t WHERE t.completed = false AND t.category = 'receive'").getResultList();

            this.entityManager.getTransaction().begin();
            for (Transaction deposit : resultList) {
                long confirmations = wallet.getConfirmationsForTransactionId(deposit.getTransactionId());
                deposit.setConfirmations(confirmations);
                this.entityManager.merge(deposit);
                
                deposit.getUser().setLogged_in(this.tipBot.isUserIdentified(deposit.getUser().getUsername()));
                
                if (confirmations >= MIN_CONFIRMATIONS && deposit.getUser().isLogged_in()) {
                    processDeposit(deposit);
                    notificationService.sendDepositNotification(deposit);
                    this.mail.sendTransactionNotice("Deposit", deposit.getAmount(), deposit.getUser());
                }
                
            }
        } catch (WalletConnectionException | InterruptedException | NullPointerException ex) {
            Logger.getLogger(TransactionService.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
    }
    
    public BigDecimal getUnconfirmedDepositsForUser(User user) {
        this.entityManager = TipBot.getENTITY_MANAGER_FACTORY().createEntityManager();
        List resultList = this.entityManager.createQuery("SELECT sum(t.amount) from Transaction t WHERE t.category = 'receive' AND t.completed = false AND t.user = :user").setParameter("user", user).getResultList();
        this.entityManager.close();
        
        if (resultList.get(0) != null) {
            return (BigDecimal) resultList.get(0);
        }
        return new BigDecimal("0");
    }

    /**
     * If the minimum amount of confirmations is reached we update the deposit
     * status and add the deposit amount to the users balance.
     *
     * @param depositTransaction
     */
    private void processDeposit(Transaction depositTransaction) {
        User user = depositTransaction.getUser();
        user.setCoins(user.getCoins().add(depositTransaction.getAmount()));
        depositTransaction.setCompleted(true);
    }

    public static void setMIN_CONFIRMATIONS(int MIN_CONFIRMATIONS) {
        TransactionService.MIN_CONFIRMATIONS = MIN_CONFIRMATIONS;
    }  
}
