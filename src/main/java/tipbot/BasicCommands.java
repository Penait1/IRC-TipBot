package tipbot;

import connectivity.Wallet;
import exception.WalletConnectionException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import model.Tip;
import model.User;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import service.TransactionService;
import service.UserService;
import utility.Mail;

public class BasicCommands extends ListenerAdapter {

    // Configuration
    public final static BigDecimal FEE = TipBot.FEE;
    public final static BigDecimal MIN_WITHDRAW = TipBot.MIN_WITHDRAW;
    public final static String CURRENCY = TipBot.CURRENCY;
    private final static int MESSAGE_DELAY = TipBot.MESSAGE_DELAY;
    private final static int MIN_DECIMALS = TipBot.MIN_DECIMALS;

    // Magic numbers
    private final static int FIRST_VALUE_GREATER = 1;
    private final static int VALUES_EQUAL = 0;
    private final static int EQUAL = 0;
    private String[] commands = new String[]{"!tip", "!balance", "!withdraw", "!deposit"};
    private static ArrayList<String> DISABLED_COMMANDS = new ArrayList();

    // Attributes
    private final DecimalFormat decimalFormatter;
    private static PircBotX bot;
    private long timestamp;
    private long timestampPrivate;

    private UserService userService;
    private TransactionService transactionService;
    private Wallet wallet;
    private Mail mail;

    public BasicCommands() {
        bot = TipBot.getBot();
        decimalFormatter = new DecimalFormat("0.0###", new DecimalFormatSymbols(Locale.US));
        this.wallet = new Wallet();
        this.mail = new Mail();
        this.userService = new UserService();
        this.transactionService = new TransactionService();
    }

    @Override
    public void onConnect(ConnectEvent event) throws Exception {
        bot = event.getBot();
        this.userService = new UserService();
        this.userService.resetLoggedIn();
        event.getBot().sendRaw().rawLine("CAP REQ identify-msg");
    }

    @Override
    public void onMessage(MessageEvent event) {

        String fullCommand = event.getMessage().substring(1);
        String command = fullCommand.split(" ")[0];

        // Spam prevention: 2 seconds delay needed
        if (Arrays.asList(commands).contains(command)) {
            if (((event.getTimestamp() / 1000) - timestamp) <= MESSAGE_DELAY) {
                return;
            } else {
                timestamp = (event.getTimestamp() / 1000);
            }
        }

        // Checking if the user is identified, if not; exit
        if (Arrays.asList(commands).contains(command) && !event.getMessage().startsWith("+")) {
            event.getChannel().send().message(
                    event.getUser().getNick() + ": You need to be identified by NickServ to use this command!");
            return;
        }

        //Checking if the command is disabled by an admin
        if (DISABLED_COMMANDS.contains(command)) {
            event.respond("It appears this command is disabled at this time, please try again later.");
            return;
        }

        //Retrieving the user object to be able to pass to the methods
        User user = this.userService.getOrCreateUser(event.getUser().getNick());

        switch (command.toUpperCase()) {
            case "!BALANCE":
                balance(event, user);
                break;
            case "!TIP":
                tip(event, user);
                break;
            case "!DEPOSIT":
                deposit(event, user);
                break;
            case "!WITHDRAW":
                event.respond("Please send a private message to Penait1-Tipbot to withdraw. (Use the command !tipbot to learn more)");
                break;
            case "!TIPBOT":
                event.getChannel().send().message(event.getUser().getNick()
                        + ": I've sent you a private message with some information about the tipbot!");
                event.getUser().send().message(
                        "Hi! I'm a Tipbot for #Blackcoin. To learn more about my commands sent !commands to me in a private message. If you have a problem with the bot you can message Penait1, my owner. Terms of Use: http://blackcoin.co/irc/terms.html");
                break;
        }
    }

    public void withdraw(PrivateMessageEvent event, User user) {
        checkNegative();

        try {
            String[] parts = event.getMessage().split(" ");
            // Checking if the parameters are filled in correctly
            if (parts.length != 3) {
                event.respond(event.getUser().getNick() + ": Wrong usage! Use !withdraw <Your address> <Amount>");

            } else {
                BigDecimal amount;

                if (parts[2].equals("all")) {
                    amount = user.getCoins();
                } else {
                    amount = new BigDecimal(parts[2]);
                }

                if (MIN_WITHDRAW.compareTo(amount) == FIRST_VALUE_GREATER) {
                    event.respond("Please withdraw atleast " + MIN_WITHDRAW + CURRENCY + ".");
                    return;
                }

                if (checkDigits(amount.toPlainString()) == false) {
                    event.respond("Please only withdraw amounts with " + MIN_DECIMALS + " or less decimals");
                    return;
                }

                if (amount.compareTo(user.getCoins()) == FIRST_VALUE_GREATER) {
                    event.respond("You tried to withdraw " + amount + CURRENCY
                            + " but you only have " + decimalFormatter.format(user.getCoins()) + CURRENCY);
                    return;
                }

                if (!wallet.isAValidAddress(parts[1])) {
                    event.respond("Please provide a valid address!");
                    return;
                }

                String transactionId = userService.processWithdrawel(user, parts[1], amount);

                if (transactionId != null) {
                    event.respond("You withdrew " + amount.subtract(FEE) + CURRENCY + " (" + amount
                            + CURRENCY + " - " + FEE + CURRENCY + " Transaction fee) to the address " + parts[1]
                            + ". Txid: " + transactionId);
                    mail.sendTransactionNotice("Withdrawel", amount, user);
                } else {
                    event.respond("Oops! It looks like something went wrong :( Please try again later...");
                }
            }

        } catch (NumberFormatException ex) {
            event.getUser().send().message("Wrong usage! Use !withdraw <Your address> <Amount>");
        } catch (NullPointerException e) {
            event.respond("It looks like the bot is busy at the moment. Please try again later.");
        } catch (WalletConnectionException ex) {
            event.respond("Error connecting with the wallet. Please try again later...");
        }
    }

    public void deposit(Event event, User user) {
        try {
            // Checking if there is a negative balance; if there is,
            // shutdown.
            checkNegative();

            event.respond("Your deposit address is: " + userService.getAddress(user)
                    + ". It will take 6 confirmations for your deposit to be cleared. You are notified when that happens. You have to be logged in with Nickserv for your deposit to be processed.");
        } catch (WalletConnectionException ex) {
            event.respond("Error connecting with the wallet. Please try again later...");
        }
    }

    public void balance(Event event, User user) {
        // Checking if there is a negative balance; if there is,
        // shutdown.
        checkNegative();
        event.respond("Your balance is: "
                + decimalFormatter.format(user.getCoins()) + CURRENCY
                + ". Your unconfirmed balance is: "
                + decimalFormatter.format(this.transactionService.getUnconfirmedDepositsForUser(user)) + CURRENCY + ".");
    }

    public void tip(MessageEvent event, User user) {
        checkNegative();
        try {

            // Splitting the message to get all the variables
            String[] parts = event.getMessage().split(" ");

            // Checking if the user is not tipping himself
            if (user.getUsername().equals(parts[1])) {
                event.getChannel().send().message(user.getUsername() + ": You tried to tip yourself!");
                return;
            }

            BigDecimal amount;

            // If he is not, check the tip amount
            if (parts[2].equals("all")) {
                amount = user.getCoins();
            } else {
                amount = new BigDecimal(parts[2]);
            }

            // Checking if the target user is in the cache; if he/she is,
            // finish before the else part
            User targetUser = userService.getOrCreateUser(parts[1]);
            Tip tip = new Tip(user, targetUser, amount);

            if (!tip.isValidTip()) {
                event.respond("Your tip amount is not valid! Is it greater than 0? Does it not contain more than "
                        + TipBot.MIN_DECIMALS + " decimals?"
                );
                return;
            }

            if (targetUser.isLogged_in() == true) {
                this.userService.giveTip(tip);
                event.getChannel().send().message(user.getUsername() + ": You tipped " + targetUser.getUsername() + " "
                        + amount + CURRENCY + ". (!tipbot to learn more)");

                //If the user is not logged in, atleast not known
            } else {

                // Checking if the target user is identified with nickserv
                if (!TipBot.getBot().isUserIdentified(targetUser.getUsername())) {
                    event.getChannel().send().message(user.getUsername() + ": You tried to tip " + targetUser.getUsername()
                            + " but he/she isn't logged in with Nickserv. This is required.");
                    return;
                }

                // Sending the tip message in the channel
                this.userService.giveTip(tip);
                event.getChannel().send().message(user.getUsername() + ": You tipped " + targetUser.getUsername() + " "
                        + amount + CURRENCY + ". (!tipbot to learn more)");
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            event.getChannel().send()
                    .message(event.getUser().getNick() + ": Wrong usage! Use !tip <Target username> <Amount>");
        } catch (InterruptedException | NullPointerException e) {
            event.getChannel().send().message(event.getUser().getNick()
                    + ": It looks like the bot is busy at the moment. Please try again later.");
        }
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
        String fullCommand = event.getMessage().replace("+", "");
        String[] commandSplit = fullCommand.split(" ");
        String command = commandSplit[0];

        // Checking if the user is identified
        if (Arrays.asList(commands).contains(command) && !event.getMessage().startsWith("+")) {
            event.respond(event.getUser().getNick() + ": You need to be identified by NickServ to use this command!");
            return;
        }

        if (((event.getTimestamp() / 1000) - timestampPrivate) <= 2) {
            return;
        } else {
            timestampPrivate = (event.getTimestamp() / 1000);
        }

        //Checking if the command is disabled
        if (DISABLED_COMMANDS.contains(command)) {
            event.respond("It appears this command is disabled at this time, please try again later.");
            return;
        }

        //Retrieving the user object to be able to pass to the methods
        User user = this.userService.getOrCreateUser(event.getUser().getNick());

        if (command.equalsIgnoreCase("!withdraw")) {
            withdraw(event, user);
        }

        if (command.equalsIgnoreCase("!commands") || command.equalsIgnoreCase("-!tipbot")) {

            if (commandSplit.length > 1) {
                return;
            }

            event.getUser().send().message(
                    "!balance - Shows your balance, !tip <user> <amount> - Tips <amount> to <user>, !deposit - Shows your deposit address, !withdraw <address> <amount> - Withdraws <amount> to your own <address>, !mtip <targ1> <amt1> [<targ2> <amt2> ...] - Works like !tip, only allows you to tip multiple users at the same time");
        }

        if (command.equalsIgnoreCase("!balance") || command.equalsIgnoreCase("-!balance")) {
            // Checking if there is a negative balance; if there is, shutdown.
            checkNegative();

            balance(event, user);
        }

        if (command.equalsIgnoreCase("!deposit") || command.equalsIgnoreCase("-!deposit")) {

            // Checking if there is a negative balance; if there is, shutdown.
            checkNegative();

            deposit(event, user);
        }

    }

    public void checkNegative() {
        if (userService.isNegativeBalancePresent() == true) {
            bot.sendIRC().message(TipBot.getCHANNEL(), "The Tipbot will shutdown due to a critical error.");
            bot.close();
        }
    }

    public boolean checkDigits(String number) {
        boolean good = false;

        if (number.contains(".")) {
            String[] split = number.split("\\.");

            if (split[1].length() <= MIN_DECIMALS) {
                good = true;
            } else {
                good = false;
            }
        } else {
            good = true;
        }

        return good;
    }

    public static void addDisabledCommand(String command) {
        DISABLED_COMMANDS.add(command);
    }

    public static void removeDisabledCommand(String command) {
        if (!DISABLED_COMMANDS.contains(command)) {
            return;
        }
        DISABLED_COMMANDS.remove(command);
    }
}
