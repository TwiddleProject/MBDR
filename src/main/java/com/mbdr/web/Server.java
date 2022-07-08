package com.mbdr.web;

import java.util.ArrayList;
import java.util.Set;

import io.javalin.Javalin;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.parsing.KnowledgeBaseReader;
import com.mbdr.utils.parsing.Parser;

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

            ArrayList<Set<NicePossibleWorld>> rankedModel = new ArrayList<>();

            try {
                ArrayList<String> rawFormulas = KnowledgeBaseReader.readFormulasFromString(data);

                for (String raw : rawFormulas) {
                    System.out.println(raw);
                }

                DefeasibleKnowledgeBase knowledge = Parser.parseFormulas(rawFormulas);

                System.out.println("----------------------------");
                System.out.println("KB_C:\t" + knowledge.getPropositionalKnowledge());
                System.out.println("KB_D:\t" + knowledge.getDefeasibleKnowledge());
                System.out.println("----------------------------");

                rankedModel = com.mbdr.modelbased.RationalClosure.ConstructRankedModel(knowledge, null);

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
