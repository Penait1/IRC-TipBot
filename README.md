# IRC-TipBot
IRC Bot which lets users send cryptocurrency via their IRC Nicknames.

## About
By using this tipbot you are responsible for the user funds that it contains. While it has not been hacked yet,
I don't know if there was even an attempt to do so. While there are some failguards, like a check before each command
issued by an user if there is a balance in the database < 0, please be carefull with storing lots of crypto in this bot.

This tipbot is written in Java. It uses the PircBotX plugin to handle connections to the IRC network/User input. 
Further it uses a MySQL database to save everyones account/balance. I used Hibernate as ORM. 

## Setup

- A websocket is listening on port 5566 for incomming transactions (deposits/withdraws). This needs to be set in your crypto daemon settings.
```
blocknotify=echo block | nc -q 0 127.0.0.1 5566
walletnotify=echo wallet %s | nc -q 0 127.0.0.1 5566
```
Off course you should also set the RPC password/port/user in this file, so I suggest you do that aswell. 

- Create a MySQL database, user and password and remember them to be specified later in the config file. 

- Run your crypto daemon.

- Setup a config file in the root of the project. It should contain the following:
```
#Bot Settings
BOT_USERNAME=(Username here)
CHANNEL=(IRC channel to join)
NICKSERV_PASSWORD=(Nickserv password)
MESSAGE_DELAY=(Min Delay between user commands, if this is set to 2 it only executes one command every 2 seconds)
MIN_DECIMALS=(Min amount of decimals, recommended 4. If this is set to 4 a tip must be atleast 0.0001)

#Coin Settings
CRYPTO_PORT_NUMBER=(RPC port from the wallet here)
WALLET_URL=http://(IP Adres for the wallet here)
WALLET_USER=Penait1
WALLET_PASSWORD=plakband
CURRENCY=BLK

#Withdrawel and deposit settings
FEE=(Withdrawel fee)
MIN_WITHDRAW=(Min amount of coins to be withdrawn)
MIN_CONFIRMATIONS=(Amount of confirmations it waits for before a deposit is cleared)

#Email settings
FROM_EMAIL=(Email address it sends emails from)
TO_EMAIL=(Email address it sends emails to)
USERNAME=(Username from the email address, if you FROM_EMAIL is penait1@gmail.com, this value is penait1)
PASSWORD=(Password from the FROM_EMAIL address)

#Database parameters
DB_IP=jdbc:mysql://(IP Address from the database)/(Database name)
DB_USERNAME=(Database username)
DB_PASSWORD=(Database password)
```
- You should be good now to start the tipbot.
