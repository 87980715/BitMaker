package com.qidianai.bitmaker.quote;

import com.qidianai.bitmaker.event.EvKline;
import com.qidianai.bitmaker.event.EvTest;
import com.qidianai.bitmaker.event.EvTicker;
import com.qidianai.bitmaker.eventsys.Event;
import com.qidianai.bitmaker.eventsys.Reactor;
import com.qidianai.bitmaker.marketclient.okcoin.JsonKlineBatch;
import com.qidianai.bitmaker.marketclient.okcoin.JsonTicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.quote
 * Author: fox  
 * Date: 2017/7/22
 *
 **********************************************************/
public class BollingerBand extends Quotation {
    protected Logger log = LogManager.getLogger(getClass().getName());

    @Override
    public void handle(Event ev) {
        if (ev.getType() == EvTicker.class) {
            EvTicker evTicker = (EvTicker) ev;
            JsonTicker ticker = evTicker.getData();
            //log.info("lastPrice: " + ticker.last);
        } else if (ev.getType() == EvKline.class) {
            EvKline evKline = (EvKline) ev;
            JsonKlineBatch batch = evKline.getData();
            batch.getKlinelist().forEach(jsonKline -> {
                switch (jsonKline.klinePeriod) {
                    case kLine1Min:
                        break;

                    case kLine15Min:
                        break;

                    case kLine30Min:
                        break;
                }

                log.info(jsonKline.klinePeriod);
                log.info(jsonKline.timeStamp_ms);
                log.info(jsonKline.highPrice);
                log.info(jsonKline.easyDate);
            });
        }
    }

    @Override
    public void prepare() {
        Reactor.getInstance().register(EvTicker.class, this);
        Reactor.getInstance().register(EvKline.class, this);
    }

    @Override
    public void update() {

    }
}
