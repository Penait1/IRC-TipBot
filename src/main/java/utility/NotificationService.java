package utility;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import model.Transaction;
import model.User;
import org.pircbotx.PircBotX;
import tipbot.TipBot;

public class NotificationService {
    private final PircBotX bot;
    private final DecimalFormat decimalFormatter;

    public NotificationService() {
        this.bot = TipBot.getBot();
        decimalFormatter = new DecimalFormat("0.0###", new DecimalFormatSymbols(Locale.US));
    }

    /**
     * Sends a message to the user when his/her deposit has been cleared.
     * @param depositTransaction
     */
    public void sendDepositNotification(Transaction depositTransaction) {
        User user = depositTransaction.getUser();
        
        if (depositTransaction.getAmount().compareTo(new BigDecimal("0.0000")) == 0) {
            sendDepositToLowError(depositTransaction);
            return;
        }
        this.bot.sendIRC().message(user.getUsername(), "Hello! Your deposit of " + decimalFormatter.format(depositTransaction.getAmount()) + TipBot.CURRENCY + " has been cleared. Your balance is now: " + decimalFormatter.format(user.getCoins()) + TipBot.CURRENCY + ". Happy tipping!");
    }

    /**
     * Sends user a notification in IRC if his/her deposit was too low. This is the case if it has not enough decimals.
     * @param depositTransaction
     */
    private void sendDepositToLowError(Transaction depositTransaction) {
        User user = depositTransaction.getUser();
        this.bot.sendIRC().message(user.getUsername(), "Hello! Your deposit of " + decimalFormatter.format(depositTransaction.getAmount() + TipBot.CURRENCY + " was too low! Amounts lower than 0.0001 will be disposed."));
    }
}
