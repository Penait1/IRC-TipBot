package model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User implements Serializable {
    
    @Id
    private String username;
    
    @Column(name="coins", precision = 19, scale = 4)
    private BigDecimal coins;
    private String address;
    private boolean logged_in;

    public User() {
    }
    
    public User (String username) {
        this.username = username;
        this.coins = new BigDecimal("0");
        this.logged_in = false;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getCoins() {
        return coins;
    }

    public String getAddress() {
        return address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCoins(BigDecimal coins) {
        this.coins = coins;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isLogged_in() {
        return logged_in;
    }

    public void setLogged_in(boolean logged_in) {
        this.logged_in = logged_in;
    }
}

