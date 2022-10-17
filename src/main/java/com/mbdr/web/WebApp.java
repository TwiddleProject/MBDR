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
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/app";                   // change to host files on a subpath, like '/assets'
                staticFiles.directory = "frontend/out/";              // the directory where your files are located
                staticFiles.location = Location.EXTERNAL;      // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
                staticFiles.precompress = false;                // if the files should be pre-compressed and cached in memory (optimization)
                staticFiles.aliasCheck = null;                  // you can configure this to enable symlinks (= ContextHandler.ApproveAliases())
                // staticFiles.headers = Map.of(...);              // headers that will be set for the files
                staticFiles.skipFileFunction = req -> false;    // you can use this to skip certain files in the dir, based on the HttpServletRequest
            });
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/static";                   // change to host files on a subpath, like '/assets'
                staticFiles.directory = "frontend/out/static/";              // the directory where your files are located
                staticFiles.location = Location.EXTERNAL;      // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
                staticFiles.precompress = false;                // if the files should be pre-compressed and cached in memory (optimization)
                staticFiles.aliasCheck = null;                  // you can configure this to enable symlinks (= ContextHandler.ApproveAliases())
                // staticFiles.headers = Map.of(...);              // headers that will be set for the files
                staticFiles.skipFileFunction = req -> false;    // you can use this to skip certain files in the dir, based on the HttpServletRequest
            });
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/_next";                   // change to host files on a subpath, like '/assets'
                staticFiles.directory = "frontend/out/_next/";              // the directory where your files are located
                staticFiles.location = Location.EXTERNAL;      // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
                staticFiles.precompress = false;                // if the files should be pre-compressed and cached in memory (optimization)
                staticFiles.aliasCheck = null;                  // you can configure this to enable symlinks (= ContextHandler.ApproveAliases())
                // staticFiles.headers = Map.of(...);              // headers that will be set for the files
                staticFiles.skipFileFunction = req -> false;    // you can use this to skip certain files in the dir, based on the HttpServletRequest
            });
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
