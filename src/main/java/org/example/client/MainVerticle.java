package org.example.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.util.stream.Collectors.toList;

public class MainVerticle extends AbstractVerticle {

    //language=MySQL
    private static final String GET_BY_CITY = "SELECT" +
            " zipcode0_.zip      AS zip1_0_,\n" +
            "       zipcode0_.city     AS city2_0_,\n" +
            "       zipcode0_.county   AS county3_0_,\n" +
            "       zipcode0_.state    AS state4_0_,\n" +
            "       zipcode0_.timezone AS timezone5_0_,\n" +
            "       zipcode0_.type     AS type6_0_\n" +
            "FROM" +
            " ZipCode zipcode0_\n" +
            "WHERE" +
            " zipcode0_.city = ?";

    private static final String GET_BY_ZIPCODE = "SELECT" +
            " zipcode0_.zip      AS zip1_0_0_,\n" +
            "       zipcode0_.city     AS city2_0_0_,\n" +
            "       zipcode0_.county   AS county3_0_0_,\n" +
            "       zipcode0_.state    AS state4_0_0_,\n" +
            "       zipcode0_.timezone AS timezone5_0_0_,\n" +
            "       zipcode0_.type     AS type6_0_0_\n" +
            "FROM" +
            " ZipCode zipcode0_\n" +
            "WHERE" +
            " zipcode0_.zip = ?\n";

    private Random random = new Random();

    private List<String> cities;
    private List<String> zipCodes;
    private MySQLPool pool;
    private long timer1;
    private long timer2;
    private boolean printed;

    @Override
    public void start() throws Exception {
        cities = readAll("cities");
        zipCodes = readAll("zip_codes");

        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setCachePreparedStatements(false)
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("baeldung")
                .setUser("root")
                .setPassword("root")
                .setSslMode(SslMode.REQUIRED)
                .setTrustAll(true);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(95)
                .setEventLoopSize(Runtime.getRuntime().availableProcessors() * 2);

        pool = MySQLPool.pool(vertx, connectOptions, poolOptions);

        timer1 = vertx.setPeriodic(2, id -> runQuery(GET_BY_CITY, cities));
        vertx.setTimer(1, l -> {
            timer2 = vertx.setPeriodic(2, id -> runQuery(GET_BY_ZIPCODE, zipCodes));
        });
    }

    private void runQuery(String query, List<String> params) {
        int index = random.nextInt(params.size());
        String param = params.get(index);
        pool.preparedQuery(query).execute(Tuple.of(param), ar -> {
            if (ar.failed()) {
                if (!printed) {
                    ar.cause().printStackTrace();
                    printed = true;
                }
                vertx.cancelTimer(timer1);
                vertx.cancelTimer(timer2);
            }
        });
    }

    private List<String> readAll(String resourcePath) {
        ClassLoader loader = MainVerticle.class.getClassLoader();
        try (InputStream resource = Objects.requireNonNull(loader.getResourceAsStream(resourcePath))) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            return reader.lines().collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
