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
        for (int index = 0; index <= ranks; ++index) {
            this.ranks.add(null);
        }
    }

    public RankedFormulasInterpretation(ArrayList<PlFormula> ranks) {
        this.ranks = new ArrayList<>(ranks);
    }

    /**
     * Get the number of finite ranks
     * 
     * @return
     */
    public int getRankCount() {
        return this.ranks.size() - 1;
    }

    /**
     * Get the formula on the specified finite rank index
     * 
     * @param index - the finite rank index
     * @return
     */
    public PlFormula getRank(int index) {
        if (index >= this.ranks.size() - 1 || index < 0) {
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        return this.ranks.get(index);
    }

    /**
     * Set the formula for the specified finite rank index
     * 
     * @param index       - the finite rank index
     * @param rankFormula - the rank formula
     */
    public void setRank(int index, PlFormula rankFormula) {
        if (index >= this.ranks.size() - 1 || index < 0) {
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.set(index, rankFormula);
    }

    /**
     * Get the formula on the infinite rank
     * 
     * @return
     */
    public PlFormula getInfiniteRank() {
        return this.ranks.get(this.ranks.size() - 1);
    }

    /**
     * Set the formula for the infinite rank
     * 
     * @param rankFormula
     */
    public void setInfiniteRank(PlFormula rankFormula) {
        this.ranks.set(this.ranks.size() - 1, rankFormula);
    }

    /**
     * Add a new empty (null) finite rank below the infinite rank
     * 
     * @return
     */
    public int addRank() {
        int position = this.getRankCount();
        this.ranks.add(position, null);
        return position;
    }

    /**
     * Add a new finite rank below the infinite rank containing the specified
     * formula
     * 
     * @param rankFormula
     * @return
     */
    public int addRank(PlFormula rankFormula) {
        int position = this.getRankCount();
        this.ranks.add(position, rankFormula);
        return position;
    }

    /**
     * Add a new empty (null) finite rank at the specified index
     * 
     * @param index
     */
    public void addRank(int index) {
        if (index >= this.ranks.size() - 1 || index < 0) {
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.add(index, null);
    }

    /**
     * Add new rank containing the specified formula at the specified index
     * 
     * @param index
     * @param rankFormula
     */
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
