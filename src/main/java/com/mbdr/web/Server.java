package com.mbdr.web;

import java.util.ArrayList;
import java.util.Set;

import io.javalin.Javalin;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.structures.DefeasibleFormulaCollection;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.construction.ModelRank;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.utils.parsing.KnowledgeBaseReader;
import com.mbdr.utils.parsing.Parsing;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

import org.json.*;

public class Server {

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
            config.enableCorsForAllOrigins();
        }).start(getPort());

        app.get("/", ctx -> {
            ctx.html("TwiddleProject Home");
        });

        app.get("/api", ctx -> {
            ctx.html("TwiddleProject API");
        });

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

            RankedInterpretation rankedModel = new RankedInterpretation();

            try {
                ArrayList<String> rawFormulas = Parsing.readFormulasFromString(data);

                for (String raw : rawFormulas) {
                    System.out.println(raw);
                }

                DefeasibleFormulaCollection knowledge = Parsing.parseFormulas(rawFormulas);

                System.out.println("----------------------------");
                System.out.println("KB_C:\t" + knowledge.getPropositionalKnowledge());
                System.out.println("KB_D:\t" + knowledge.getDefeasibleKnowledge());
                System.out.println("----------------------------");

                rankedModel = new ModelRank().construct(new DefeasibleKnowledgeBase(knowledge));

            } catch (Exception e) {
                e.printStackTrace();
            }

            String result = "";

            result += rankedModel.toString();
            
            ctx.result(result);
        });
    }

}
