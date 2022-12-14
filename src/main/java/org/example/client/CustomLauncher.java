package org.example.client;

import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;

public class CustomLauncher extends Launcher {

    private static final boolean ENABLE_METRICS = Boolean.getBoolean("enableMetrics");

    public static void main(String[] args) {
        new CustomLauncher().dispatch(args);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        options.setMetricsOptions(new MicrometerMetricsOptions()
                .setEnabled(ENABLE_METRICS)
                .setJmxMetricsOptions(new VertxJmxMetricsOptions()
                        .setEnabled(true)
                        .setStep(1)
                        .setDomain("my.metrics.domain")));
    }
}
