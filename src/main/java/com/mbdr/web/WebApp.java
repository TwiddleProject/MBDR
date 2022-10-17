package com.mbdr.web;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import static io.javalin.apibuilder.ApiBuilder.*;

public class WebApp {

    private static final int DEFAULT_PORT = 5000;
    
    private static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            return Integer.parseInt(envPort);
        }
        return DEFAULT_PORT;
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.allowHost("http://app.twiddleproject.com", "https://app.twiddleproject.com");
                });
            });
        }).start(getPort());

        app.get("/", ctx -> {
            ctx.redirect("/app/");
        });

        app.routes(() -> {
            path("api", () -> {
                get(ctx -> {
                    ctx.html("TwiddleProject API");
                });
                path("construction", () -> {
                    path("{algorithm}", () -> {
                        post(ConstructionController::getRankedRCModel);
                    });
                });
            });
        });
    }

}
