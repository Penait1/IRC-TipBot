package connectivity;

import exception.WalletConnectionException;
import java.io.*;
import java.math.BigDecimal;
import model.Transaction;
import model.User;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tipbot.BasicCommands;
import utility.Mail;

public class Wallet {
    private static CryptoConfig config;
    private Mail mail;

    public Wallet() {
        this.mail = new Mail();
    }

    private JSONObject sendCommandToWallet(String a_sMethod, Object... a_params) throws WalletConnectionException {
        JSONObject jsonResponse = null;
        CloseableHttpClient client = HttpClients.createDefault();

        try {

            String urlString = config.getUrl() + ":" + config.getRpcPort();

            String userPassword = config.getRpcUser() + ":" + config.getRpcPass();
            String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());

            JSONObject paramsJson = new JSONObject();
            paramsJson.put("jsonrpc", "1.0");
            paramsJson.put("id", "1");
            paramsJson.put("method", a_sMethod);

            if (a_params != null) {
                if (a_params.length > 0) {
                    JSONArray paramArray = new JSONArray();
                    for (Object baz : a_params) {
                        paramArray.add(baz);
                    }
                    paramsJson.put("params", paramArray);
                }
            }

            HttpPost httpPost = new HttpPost(urlString);
            String comando = paramsJson.toJSONString();
            StringEntity entidad = new StringEntity(comando);
            httpPost.setEntity(entidad);
            httpPost.setHeader("Authorization", "Basic " + encoding);
            HttpResponse respuesta = client.execute(httpPost);
            BufferedReader rd = new BufferedReader(new InputStreamReader(respuesta.getEntity().getContent()));

            String returnString = rd.readLine();
            rd.close();
            JSONParser parser = new JSONParser();
            jsonResponse = (JSONObject) parser.parse(returnString);

            
        } catch (IOException | ParseException e) {
            mail.sendError(new WalletConnectionException());
            throw new WalletConnectionException();
        }
        finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonResponse;
    }

    /**
     * Sets the configuration for the wallet
     *
     * @param url
     * @param port
     * @param rpcUser
     * @param rpcPassword
     */
    public static void setConfig(String url, int port, String rpcUser, String rpcPassword) {
        CryptoConfig conf = new CryptoConfig();
        conf.setUrl(url);
        conf.setRpcPort(Integer.toString(port));
        conf.setRpcUser(rpcUser);
        conf.setRpcPass(rpcPassword);
        config = conf;
    }

    /**
     * Generates a new address for the given username. Uses the username as
     * label for the new address.
     *
     * @param user
     * @return a new generated address
     */
    public String generateNewAddress(User user) throws WalletConnectionException {
        JSONObject responseFromWallet = sendCommandToWallet("getnewaddress", user.getUsername());
        return (String) responseFromWallet.get("result");
    }

    /**
     * Gets the current amount of confirmations for a transaction ID.
     *
     * @param transactionId
     * @return The current amount of confirmations
     */
    public long getConfirmationsForTransactionId(String transactionId) throws WalletConnectionException {
        JSONObject responseFromWallet = sendCommandToWallet("gettransaction", transactionId);
        JSONObject result = (JSONObject) responseFromWallet.get("result");
        return (long) result.get("confirmations");
    }

    /**
     * Checks if the transaction is a receiving transaction.
     *
     * @param transactionId
     * @return True if the transaction is a receiving one
     */
    public boolean isAReceiveTransaction(String transactionId) throws WalletConnectionException {
        JSONObject responseFromWallet = sendCommandToWallet("gettransaction", transactionId);
        JSONObject result = (JSONObject) responseFromWallet.get("result");
        JSONArray details = (JSONArray) result.get("details");
        JSONObject detailsObject = (JSONObject) details.get(0);
        return detailsObject.get("category").toString().equals("receive");
    }

    /**
     * Creates transaction object gathered with info from the gettransaction
     * from the wallet.
     *
     * @param transactionId
     * @return Transaction object
     */
    public Transaction getTransaction(String transactionId) throws WalletConnectionException {
        JSONObject responseFromWallet = sendCommandToWallet("gettransaction", transactionId);
        JSONObject result = (JSONObject) responseFromWallet.get("result");
        BigDecimal amount = new BigDecimal((double) result.get("amount"));
        long confirmations = (long) result.get("confirmations");

        JSONArray details = (JSONArray) result.get("details");
        JSONObject detailsObject = (JSONObject) details.get(0);
        String address = (String) detailsObject.get("address");
        String category = (String) detailsObject.get("category");

        BigDecimal fee = null;
        if (category.equals("send")) {
            fee = new BigDecimal((double) detailsObject.get("fee"));
        }

        Transaction transaction = new Transaction(amount, fee, transactionId, confirmations, address, category);
        return transaction;
    }

    /**
     * Sends an amount of coins to the specified address
     *
     * @param address
     * @param amount
     * @return TransactionID on success
     */
    public String sentCoinsToAddress(String address, BigDecimal amount) throws WalletConnectionException {
        JSONObject responseFromWallet = sendCommandToWallet("sendtoaddress", address, amount.subtract(BasicCommands.FEE));

        if (responseFromWallet.isEmpty()) {
            return null;
        }

        String transactionId = (String) responseFromWallet.get("result");
        return transactionId;
    }

    /**
     * Checks if a given address is valid.
     *
     * @param address
     * @return True if the address is valid.
     */
    public boolean isAValidAddress(String address) throws WalletConnectionException {
        JSONObject responseFromWallet = sendCommandToWallet("validateaddress", address);
        JSONObject result = (JSONObject) responseFromWallet.get("result");
        return (boolean) result.get("isvalid");
    }
}
