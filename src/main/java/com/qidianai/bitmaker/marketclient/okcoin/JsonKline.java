package com.qidianai.bitmaker.marketclient.okcoin;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**********************************************************
 * BitMaker
 *
 * Package: com.qidianai.bitmaker.marketclient.okcoin
 * Author: fox  
 * Date: 2017/7/23
 *
 **********************************************************/
public class JsonKline {
    public enum KlinePeriod {
        kLineNone,
        kLine1Min,
        kLine15Min,
        kLine30Min,
    }

    public class KlineDateTime {
        int year;
        int month;
        int day;
        int hour;
        int min;
        int sec;

        @Override
        public String toString() {
            return String.format("%d%02d%02d%02d%02d%02d", year, month, day, hour, min, sec);
        }

        public int toInteger() {
            return Integer.parseInt(toString());
        }
    }

    public KlinePeriod klinePeriod;
    public Calendar klineDate;
    public String timeStamp_ms;
    public double openPrice;
    public double highPrice;
    public double lowPrice;
    public double closePrice;
    public double volumn;
    public KlineDateTime easyDate;

    public void load(KlinePeriod period, ArrayList<String> kline) throws ParseException {
        klinePeriod = period;
        timeStamp_ms = kline.get(0);
        openPrice = Double.parseDouble(kline.get(1));
        highPrice = Double.parseDouble(kline.get(2));
        lowPrice = Double.parseDouble(kline.get(3));
        closePrice = Double.parseDouble(kline.get(4));
        volumn = Double.parseDouble(kline.get(5));


        klineDate = new GregorianCalendar();
        klineDate.setTimeInMillis(Long.parseLong(timeStamp_ms));

        easyDate = new KlineDateTime();
        easyDate.year = klineDate.get(Calendar.YEAR);
        easyDate.month = klineDate.get(Calendar.MONTH) + 1;
        easyDate.day = klineDate.get(Calendar.DAY_OF_MONTH);
        easyDate.hour = klineDate.get(Calendar.HOUR_OF_DAY);
        easyDate.min = klineDate.get(Calendar.MINUTE);
        easyDate.sec = klineDate.get(Calendar.SECOND);
    }

    public int getDateInt() {
        return easyDate.toInteger();
    }

    public void update(JsonKline kline) {
        klinePeriod = kline.klinePeriod;
        klineDate = kline.klineDate;
        timeStamp_ms = kline.timeStamp_ms;
        openPrice = kline.openPrice;
        highPrice = kline.highPrice;
        lowPrice = kline.lowPrice;
        closePrice = kline.closePrice;
        volumn = kline.volumn;
        easyDate = kline.easyDate;
    }
}
