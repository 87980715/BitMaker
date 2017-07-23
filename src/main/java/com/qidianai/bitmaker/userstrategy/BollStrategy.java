package com.qidianai.bitmaker.userstrategy;

import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import com.qidianai.bitmaker.portfolio.Account;
import com.qidianai.bitmaker.portfolio.OKCoinAccount;
import com.qidianai.bitmaker.quote.BollingerBand;
import com.qidianai.bitmaker.strategy.Strategy;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.userstrategy
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class BollStrategy extends Strategy {
    OKCoinAccount account = new OKCoinAccount();
    BollingerBand bollband = new BollingerBand();
    JsonTicker lastTick = new JsonTicker();

    @Override
    public void prepare() {
        Reactor.getInstance().register(EvTicker.class, this);

        bollband.prepare();
        account.prepare();
        //account.subscribeMarketQuotation();
    }

    @Override
    public void run() {
        bollband.update();
        account.update();

        //System.out.println("upper: " + bollband.getUpperBand("30min"));
        //System.out.println("lower: " + bollband.getLowerBand("30min"));
        //System.out.println("percentB: " + bollband.getPercentB(lastTick.last, "30min"));
        //System.out.println("bandwidth: " + bollband.getBandWidth("30min"));


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvTicker.class) {
            EvTicker evTicker = (EvTicker) ev;
            JsonTicker ticker = evTicker.getData();
            lastTick = ticker;
        }
    }
}
