package model;

import tipbot.*;
import tipbot.TipBot;

import java.math.BigDecimal;

public class Tip {
    private static final int FIRST_VALUE_GREATER = 1;
    private static final int VALUES_EQUAL = 0;

    private User sender;
    private User receiver;
    private BigDecimal amount;


    public Tip(User sender, User receiver, BigDecimal amount) {
        this.sender   = sender;
        this.receiver = receiver;
        this.amount   = amount;
    }
    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Determines if a tip is valid. A tip is valid when:
     * 1. The sender has enough balance
     * 2. The amount > 0
     * 3. The amount has no less than (config file specified) decimals
     *
     * @return if the tip is valid or not
     */
    public boolean isValidTip() {
        if (this.amount == null) {
            return false;
        }

        BigDecimal zero = new BigDecimal("0");

        // Checking if the user has enough balance
        if (amount.compareTo(sender.getCoins()) == FIRST_VALUE_GREATER) {
            return false;
        }
        // Checking if the user filled in 0 as amount
        if (zero.compareTo(amount) == FIRST_VALUE_GREATER || zero.compareTo(amount) == VALUES_EQUAL) {
            return false;
        }

        // Checking if the amount has not lower than ... amount of decimals.
        if (!checkValidAmountOfDigits(amount.toPlainString())) {
            return false;
        }

        return true;
    }

    /**
     * Checks the string for amount of decimals.
     *
     * @param number BigDecimal as PLAIN string
     * @return good
     */
    private boolean checkValidAmountOfDigits(String number) {
        boolean good;

        if (number.contains(".")) {
            String[] split = number.split("\\.");

            if (split[1].length() <= TipBot.MIN_DECIMALS) {
                good = true;
            } else {
                good = false;
            }
        } else {
            good = true;
        }

        return good;
    }
}
