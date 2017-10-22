package com.github.nkonev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class FailoverUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(FailoverUtils.class);

    public static <T> T retry(int times, Supplier<T> function){
        Throwable saved=null;
        for (int i=1; i<=times; ++i) {
            try {
                LOGGER.info("Trying {}/{} to perform action {}", i, times, function);
                return function.get();
            } catch (Throwable e) {
                LOGGER.warn("Next attempt to perform action {}", function);
                saved = e;
                continue;
            }
        }
        throw new RuntimeException("Couldn't successful perform action "+function+" after "+times+" times", saved);
    }

    /**
     *
     * @param f условие выхода из цикла
     * @param seconds
     * @throws Exception
     */
    public static void assertPoll(Supplier<Boolean> f, int seconds) throws Exception {
        boolean success = false;
        for (int i=0; i<seconds && !success; ++i) {
            if (i > 0) {
                TimeUnit.SECONDS.sleep(1);
            }
            success = f.get();
        }
        if (!success) {
            throw new RuntimeException("Not get success after " + seconds + " seconds");
        }
    }
}