package config;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import connectivity.Wallet;
import service.TransactionService;
import tipbot.TipBot;
import utility.Mail;

public class ReadConfiguration {
    public void getPropValues() throws IOException {
        FileReader configFile = new FileReader("config.properties");;
        try {
            Properties prop = new Properties();

            prop.load(configFile);

            TipBot.BOT_USERNAME = prop.getProperty("BOT_USERNAME");
            TipBot.setCHANNEL(prop.getProperty("CHANNEL"));
            TipBot.NICKSERV_PASSWORD = prop.getProperty("NICKSERV_PASSWORD");
            TipBot.MESSAGE_DELAY = Integer.parseInt(prop.getProperty("MESSAGE_DELAY"));

            TipBot.FEE = new BigDecimal(prop.getProperty("FEE"));
            TipBot.MIN_WITHDRAW = new BigDecimal(prop.getProperty("MIN_WITHDRAW"));
            TipBot.CURRENCY = prop.getProperty("CURRENCY");
            TransactionService.setMIN_CONFIRMATIONS(Integer.parseInt(prop.getProperty("MIN_CONFIRMATIONS")));
            TipBot.MIN_DECIMALS = Integer.parseInt(prop.getProperty("MIN_DECIMALS"));

            Mail.setFromEmail(prop.getProperty("FROM_EMAIL"));
            Mail.setUsername(prop.getProperty("USERNAME"));
            Mail.setPassword(prop.getProperty("PASSWORD"));
            Mail.setTargetEmail(prop.getProperty("TO_EMAIL"));
            
            TipBot.DB_IP = prop.getProperty("DB_IP");
            TipBot.DB_USERNAME = prop.getProperty("DB_USERNAME");
            TipBot.DB_PASSWORD = prop.getProperty("DB_PASSWORD");

            Wallet.setConfig(prop.getProperty("WALLET_URL"), Integer.parseInt(prop.getProperty("CRYPTO_PORT_NUMBER")),
                    prop.getProperty("WALLET_USER"), prop.getProperty("WALLET_PASSWORD")
            );

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            configFile.close();
        }
    }
}
