package com.mbdr;

import java.util.ArrayList;
import java.util.Set;

import io.javalin.Javalin;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

public class Server {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
        }).start(getHerokuAssignedPort());

        app.get("/rankedmodelrc", ctx -> {
            System.out.println("Body:" + ctx.body());

            PlParser parser = new PlParser();
            PlBeliefSet KB_C = new PlBeliefSet();
            PlBeliefSet KB_D = new PlBeliefSet();

            ArrayList<Set<NicePossibleWorld>> rankedModel = new ArrayList<>();

            try {
                String[] lines = ctx.body().split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];

                    System.out.println(line);

                    if (line.contains("|~")) {
                        line = Utils.materialiseDefeasibleImplication(line);
                        KB_D.add((PlFormula) parser.parseFormula(line));
                    } else {
                        KB_C.add((PlFormula) parser.parseFormula(line));
                    }
                }
                System.out.println("----------------------------");
                System.out.println("KB_C:\t" + KB_C);
                System.out.println("KB_D:\t" + KB_D);
                System.out.println("----------------------------");

                rankedModel = RationalClosure.ConstructRankedModel(KB_C, KB_D);

            } catch (Exception e) {
                e.printStackTrace();
            }

            ctx.contentType("json");
            ctx.result(rankedModel.toString());
        });
    }

    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        return 8080;
    }

}
