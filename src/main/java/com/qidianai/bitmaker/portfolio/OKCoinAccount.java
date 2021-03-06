package com.qidianai.bitmaker.portfolio;

import com.qidianai.bitmaker.config.OKCoinCfg;
import com.qidianai.bitmaker.event.EvOrder;
import com.qidianai.bitmaker.event.EvResult;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.event.EvUserInfo;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonOrder;
import com.qidianai.bitmaker.marketclient.okcoin.JsonResult;
import com.qidianai.bitmaker.marketclient.okcoin.JsonUserInfo;
import com.qidianai.bitmaker.marketclient.okcoin.OKCoinClient;
import com.qidianai.bitmaker.notification.SMTPNotify;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.portfolio
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/

public class OKCoinAccount extends Account {
    private Logger log = LogManager.getLogger(getClass().getName());
    private OKCoinClient okCoinClient;
    private String apiKey = OKCoinCfg.apiKey;
    private String secretKey = OKCoinCfg.secretKey;
    private String url = OKCoinCfg.url;
    private String tag;
    private String namespace;

    private JsonUserInfo lastUserInfo;
    private ConcurrentHashMap<String, Order> orderMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Order> activeOrderMap = new ConcurrentHashMap<>();
    private double initialCny = -1;
    /**
     * Available Chinese Yuan
     */
    private double availableCny;
    /**
     * Available ether
     */
    private double availableEth;

    public double getInitialCny() {
        return initialCny;
    }

    public ConcurrentHashMap<String, Order> getOrderMap() {
        return orderMap;
    }

    public ConcurrentHashMap<String, Order> getActiveOrderMap() {
        return activeOrderMap;
    }

    public double getAvailableCny() {
        return availableCny;
    }

    public double getAvailableEth() {
        return availableEth;
    }


    public void setMarketAccount(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;

    }

    public void setMarketInfo(String apiKey, String secretKey, String url) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.url = url;
    }

    public void buyEth(double price, double amount) {
        log.warn("Buy eth " + price + " " + amount);

        String priceStr = String.format("%.6f", price);
        String amountStr = String.format("%.6f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "buy");
    }

    public void sellEth(double price, double amount) {
        log.warn("Sell eth " + price + " " + amount);

        String priceStr = String.format("%.6f", price);
        String amountStr = String.format("%.6f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "sell");
    }

    public void buyMarketEth(double price) {
        log.warn("Buy eth (market) " + price);

        String priceStr = String.format("%f", price);
        String amountStr = null;

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "buy_market");
    }

    public void sellMarketEth(double amount) {
        log.warn("Sell eth (market) "  + amount);

        String priceStr = null;
        String amountStr = String.format("%f", amount);

        okCoinClient.spotTrade("eth_cny", priceStr, amountStr, "sell_market");
    }

    public void cancelEth(String orderId) {
        okCoinClient.cancelOrder("eth_cny", Long.parseLong(orderId));
    }

    public void queryUserInfo() {
        okCoinClient.getUserInfo();
    }

    public double getTotalAssetValueCny(double lastEthPrice) {
        return availableCny + lastEthPrice * availableEth;
    }

    public void connectMarket() {
        if (apiKey == null || secretKey == null) {
            log.error("connectMarket Failed. apiKey or secretKey is not set.");
            return;
        }
        if (okCoinClient != null) {
            // stop client
            okCoinClient.disconnected();
        }
        okCoinClient = new OKCoinClient(apiKey, secretKey, url);
        okCoinClient.setEventDomain(tag, namespace);
        okCoinClient.connect();
        okCoinClient.login();
    }

    public void subscribeMarketQuotation() {
        //okCoinClient.getKlineEth("1min", "200", "");
        //okCoinClient.getKlineEth("5min", "200", "");
        //okCoinClient.getKlineEth("15min", "200", "");
        okCoinClient.getKlineEth("day", "200", "");
        //okCoinClient.getKlineEth("30min", "200", "");

        okCoinClient.subTickerEth();
        //okCoinClient.subKlineEth("1min");
        //okCoinClient.subKlineEth("5min");
        //okCoinClient.subKlineEth("15min");
        okCoinClient.subKlineEth("day");
        //okCoinClient.subKlineEth("30min");

    }

    public void getKline(String type, String size, String since) {
        okCoinClient.getKlineEth(type, size, since);
    }

    public void reSubscribeMarketQuotation() {
        okCoinClient.reconnect();
    }

    @Override
    public void prepare() {
        Reactor.getInstance(namespace).register(EvUserInfo.class, this);
        Reactor.getInstance(namespace).register(EvOrder.class, this);

        connectMarket();
        queryUserInfo();
    }

    @Override
    public void update() {
        if (lastUserInfo != null) {
            availableCny = lastUserInfo.info.free.cny;
            availableEth = lastUserInfo.info.free.eth;

            // update initial money CNY
            if (initialCny == -1) {
                initialCny = availableCny;
            }

            if (availableEth < 0.01 && activeOrderMap.size() == 0) {
                initialCny = availableCny;
            }
        }
    }

    @Override
    public void exit() {
        Reactor.getInstance(namespace).unregister(EvUserInfo.class, this);
        Reactor.getInstance(namespace).unregister(EvOrder.class, this);
    }

    @Override
    public void setEventDomain(String tag, String namespace) {
        this.tag = tag;
        this.namespace = namespace;
    }

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvUserInfo.class) {
            EvUserInfo evt = (EvUserInfo) ev;
            JsonUserInfo data = evt.getData();

            lastUserInfo = data;

        } else if (ev.getType() == EvOrder.class) {
            EvOrder evt = (EvOrder) ev;
            JsonOrder data = evt.getData();

            OkCoinOrder order = new OkCoinOrder();
            order.load(data);

            log.info(order);


            // update orderinfo
            orderMap.put(order.orderId, order);
            activeOrderMap.put(order.orderId, order);


            // Order Finished
            if (order.status == Order.OrderStatus.OrderDone) {
                activeOrderMap.remove(order.orderId);
                SMTPNotify.send(String.format("NewOrder %.2f %.2f %s %s", order.tradedPrice, order.tradedAmt, order.directType, order.orderId), order.toString());
            }
        } else if (ev.getType() == EvResult.class) {
            EvResult evt = (EvResult) ev;
            JsonResult data = evt.getData();

            if (data.channel.equals("ok_spotcny_trade")) {
                if (data.result) {
                    log.info("new order created " + data.order_id);
                } else {
                    log.info("new order created failed. error_code" + data.error_code);
                    SMTPNotify.send("Order Failure " + data.error_code, "Order Failure " + data.error_code);
                }
            }
        }
    }
}
