import com.qidianai.bitmaker.config.IgniteCfg;
import com.qidianai.bitmaker.config.OKCoinCfg;
import com.qidianai.bitmaker.config.StrategyCfg;
import com.qidianai.bitmaker.eventsys.Reactor;

import com.qidianai.bitmaker.strategy.StrategyRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



public class App {
    private Logger log = LogManager.getLogger(getClass().getName());
    private static boolean running = false;

    public static void run() {
        running = true;
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: Java App <config file>");
            System.out.println("BitMaker v0.02 072801");
            System.exit(-1);
        }


        try {
            Properties prop = new Properties();
            InputStream configFile = new FileInputStream(args[0]);
            prop.load(configFile);

            OKCoinCfg.load(prop);
            IgniteCfg.load(prop);
            StrategyCfg.load(prop);

            //IgniteManager.startIgnite(IgniteCfg.cfgpath);
            Reactor.startReactor();

            StrategyRunner strategyRunner = StrategyRunner.getInstance();
            strategyRunner.start();

            run();

            Reactor.stopAllReactor();
            strategyRunner.stop();

        } catch (IOException e) {
            System.out.println("Error while loading configuration file: " + e.getMessage());
        }

    }
}
