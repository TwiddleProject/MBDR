package com.mbdr;

import java.util.ArrayList;
import java.util.Set;

import io.javalin.Javalin;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

import org.json.*;

public class Server {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
        }).start(getHerokuAssignedPort());

        app.post("/rankedmodelrc", ctx -> {
            System.out.println("Body:" + ctx.body());
            String data = "";

            try {
                JSONObject jsonObject = new JSONObject(ctx.body());
                System.out.println("jsonObject:\t" + jsonObject);
                data = jsonObject.getString("data");
                System.out.println("data:\t" + data);

            } catch (JSONException err) {
                err.printStackTrace();
            }

            PlParser parser = new PlParser();
            PlBeliefSet KB_C = new PlBeliefSet();
            PlBeliefSet KB_D = new PlBeliefSet();

            ArrayList<Set<NicePossibleWorld>> rankedModel = new ArrayList<>();

            try {
                String[] lines = data.split("\n");
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

            String result = "";

            // for (Set<NicePossibleWorld> rank : rankedModel) {
            // for (NicePossibleWorld w : rank) {
            // result += w;
            // }
            // result += "\n";
            // }

            result += "∞" + " :\t" + rankedModel.get(rankedModel.size() - 1);
            for (int rank_Index = rankedModel.size() - 2; rank_Index >= 0; rank_Index--) {
                result += "\n" + rank_Index + " :\t" + rankedModel.get(rank_Index);
            }

            // ctx.result(rankedModel.toString());
            // ctx.result("{data:" + rankedModel.toString() + " }");
            // ctx.result("{data:" + result + " }");
            // String resultString = rankedModel.toString();
            // ctx.header("rankedModel", rankedModel.toString());
            // ctx.result("'" + resultString + "'");
            ctx.result(result);
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
