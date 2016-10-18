package model;

import org.junit.Test;
import tipbot.*;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TipTest {

    @Test
    public void isValidTip() throws Exception {
        User sender = new User();
        sender.setCoins(new BigDecimal("0"));
        User receiver = new User();

        Tip tip = new Tip(sender, receiver, new BigDecimal("1"));
        assertFalse(tip.isValidTip());

        tip.setAmount(new BigDecimal("0"));
        assertFalse(tip.isValidTip());

        tip.setAmount(new BigDecimal("0.000001"));
        assertFalse(tip.isValidTip());

        tip.setAmount(new BigDecimal("2"));
        sender.setCoins(new BigDecimal("2"));

        assertTrue(tip.isValidTip());
    }

}