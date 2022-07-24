package com.mbdr.modelbased.structures;

import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.exceptions.RankOutOfBounds;

public class RankedFormulasInterpretation {

    private ArrayList<PlFormula> ranks;

    public RankedFormulasInterpretation() {
        this(1);
    }

    public RankedFormulasInterpretation(int ranks) {
        this.ranks = new ArrayList<>();
        for (int index = 0; index < ranks; ++index) {
            this.ranks.add(null);
        }
    }

    public RankedFormulasInterpretation(ArrayList<PlFormula> ranks) {
        this.ranks = ranks;
    }

    public int getRankCount() {
        return this.ranks.size() - 1;
    }

    // get rankFormula from finite rank
    public PlFormula getRank(int index) {
        if (index >= this.ranks.size() - 1 || index < 0) {
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        return this.ranks.get(index);
    }

    public void setRank(int index, PlFormula rankFormula) {
        if (index >= this.ranks.size() - 1 || index < 0) {
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.set(index, rankFormula);
    }

    public PlFormula getInfiniteRank() {
        return this.ranks.get(this.ranks.size() - 1);
    }

    public void setInfiniteRank(PlFormula rankFormula) {
        this.ranks.set(this.ranks.size() - 1, rankFormula);
    }

    // Add an empty (null) new finite rank below the infinite rank
    public int addRank() {
        int position = this.getRankCount();
        this.ranks.add(position, null);
        return position;
    }

    public int addRank(PlFormula rankFormula) {
        int position = this.getRankCount();
        this.ranks.add(position, rankFormula);
        return position;
    }

    // Add a new empty (null) finite rank at specified index
    public void addRank(int index) {
        if (index >= this.ranks.size() - 1 || index < 0) {
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.add(index, null);
    }

    public void addRank(int index, PlFormula rankFormula) {
        if (index >= this.ranks.size() - 1 || index < 0) {
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.add(index, rankFormula);
    }

    public String toString() {
        String template = "%-3s:\t%s\n";
        String output = String.format(template, "∞", this.getInfiniteRank());
        for (int index = this.getRankCount() - 1; index >= 0; --index) {
            output += String.format(template, index, this.getRank(index));
        }
        return output.trim();
    }
}
