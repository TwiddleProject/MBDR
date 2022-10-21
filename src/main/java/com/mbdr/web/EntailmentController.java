package com.mbdr.web;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.tweetyproject.commons.ParserException;

import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.services.DefeasibleReasoner.InvalidFormula;
import com.mbdr.common.structures.DefeasibleFormulaCollection;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.construction.*;
import com.mbdr.modelbased.reasoning.LexicographicModelReasoner;
import com.mbdr.modelbased.reasoning.RationalCumulativeFormulaModelReasoner;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.utils.parsing.Parsing;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
 
import io.javalin.http.Context;

public class EntailmentController {

    private static Map<String, Class<? extends DefeasibleReasoner>> reasoners = Map.ofEntries(
        Map.entry("cumulativeformularank", RationalCumulativeFormulaModelReasoner.class),
        Map.entry("lexicographicmodelrank", LexicographicModelReasoner.class)
    );

    public static void getAnswers(Context ctx){
        String knowledge = "", queries = "";
        try {
            JSONObject jsonObject = new JSONObject(ctx.body());
            knowledge = jsonObject.getString("knowledge");
            queries = jsonObject.getString("queries");
        } 
        catch (JSONException err) {
            ctx.result("Could not parse JSON data!");
            return;
        }

        if (!reasoners.containsKey(ctx.pathParam("algorithm"))){
            ctx.result("No such algorithm!");
            return;
        }

        DefeasibleReasoner reasoner = null;

        try {
            ArrayList<String> rawKnowledge = Parsing.readFormulasFromString(knowledge);
            DefeasibleFormulaCollection knowledgeBase = Parsing.parseFormulas(rawKnowledge);
            reasoner = (DefeasibleReasoner) reasoners
                .get(ctx.pathParam("algorithm"))
                .getConstructor()
                .newInstance();
            reasoner.build(new DefeasibleKnowledgeBase(knowledgeBase));
        } 
        catch(ParserException | IOException e){
            ctx.result("Invalid knowledge base!\nMake sure each line is a valid formula.");
            return;
        }
        catch (Exception e) {
            ctx.result("Could not construct model!");
            return;
        }

        try {
            ArrayList<String> rawQueries = Parsing.readFormulasFromString(queries);
            String answers = reasoner.queryAll(rawQueries);
            ctx.result(answers);
            return;
        } 
        catch(InvalidFormula e){
            ctx.result("Invalid formulas in query set!");
            return;
        }
        catch (Exception e) {
            ctx.result("Could not perform queries!");
            return;
        }
    }
}