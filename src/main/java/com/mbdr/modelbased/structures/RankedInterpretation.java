package com.mbdr.modelbased.structures;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

import com.mbdr.common.exceptions.RankOutOfBounds;

/**
 * This class represents a ranked interpretation consistent with its abstract definition in the literature.
 */
public class RankedInterpretation {

    // The RankedInterpretation itself is stored in terms of an ArrayList of Sets of TweetyProject NicePossibleWorlds
    private ArrayList<Set<NicePossibleWorld>> ranks;

    /**
     * Default constructor
     */
    public RankedInterpretation(){
        this(1);
    }

    /**
     * Constructor to allow specification of number of ranks - all of which default to empty sets - since no worlds have
     * yet been placed.
     * 
     * @param numRanks The number of ranks
     */
    public RankedInterpretation(int numRanks){
        this.ranks = new ArrayList<>();
        for(int index = 0; index <= numRanks; ++index){
            this.ranks.add(new HashSet<>());
        }
    }

    /**
     * Constructor to allow the flexible assignment of the entire ArrayList of pre-populated ranks as part of
     * initialisation
     * 
     * @param ranks The ranks
     */
    public RankedInterpretation(ArrayList<Set<NicePossibleWorld>> ranks){
        this.ranks = ranks;
    }

    /**
     * Get the number of finite ranks
     *
     * @return The number of finite ranks
     */
    public int getRankCount(){
        return this.ranks.size() - 1;
    }

    /**
     * Get the set of worlds located on a specified rank.
     * 
     * @param index Index of rank
     * @return Set of worlds
     */
    public Set<NicePossibleWorld> getRank(int index){
        if(index >= this.ranks.size()-1 || index < 0){
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        return this.ranks.get(index);
    }

    /**
     * Get all the worlds that have been assigned to the infinite rank.
     * 
     * @return Set of worlds
     */
    public Set<NicePossibleWorld> getInfiniteRank(){
        return this.ranks.get(this.ranks.size()-1);
    }

    /**
     * Add a set of worlds to a specified rank if it exists
     * @param index Index of rank to add worlds to
     * @param worlds Worlds to add to the rank
     */
    public void addToRank(int index, Set<NicePossibleWorld> worlds){
        if(index >= this.ranks.size()-1 || index < 0){
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        this.ranks.get(index).addAll(worlds);
    }

    /**
     * Add a set of worlds to the infinite rank
     * 
     * @param worlds set of worlds to add
     */
    public void addToInfiniteRank(Set<NicePossibleWorld> worlds){
        this.getInfiniteRank().addAll(worlds);
    }

    /**
     * Add a single world to a specified rank
     * 
     * @param index Index of rank to add world to
     * @param world World to add
     */
    public void addToRank(int index, NicePossibleWorld world){
        if(index >= getRankCount() || index < 0){
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        this.ranks.get(index).add(world);
    }

    /**
     * Add a single world to the last finite rank
     * 
     * @param world The world to add
     */
    public void addToRank(NicePossibleWorld world){
        this.ranks.get(this.getRankCount()-1).add(world);
    }

    /**
     * Add a single world to the infinite rank
     * 
     * @param world The world to add
     */
    public void addToInfiniteRank(NicePossibleWorld world){
        this.getInfiniteRank().add(world);
    }

    /**
     * Add a new, empty finite rank
     * 
     * @return The postion of the inserted rank
     */
    public int addRank(){
        return addRank(new HashSet<>());
    }

    /**
     * Add a new finite rank and place the given worlds on it
     * 
     * @param worlds Set of worlds to place on new rank
     * @return The index of this new rank in the ranked interpretation
     */
    public int addRank(Set<NicePossibleWorld> worlds){
        int position = this.getRankCount();
        this.ranks.add(position, worlds);
        return position;
    }

    /**
     * Add a new, empty rank at the specified position
     * 
     * @param index Requested index of new rank
     */
    public void addRank(int index){
        if(index > this.getRankCount() || index < 0){
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.add(index, new HashSet<NicePossibleWorld>());
    }

    /**
     * Add set of worlds to new rank at specified index
     * 
     * @param index Index of new rank
     * @param worlds Set of worlds to place on new rank
     */
    public void addRank(int index, Set<NicePossibleWorld> worlds){
        if(index > this.getRankCount() || index < 0){
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.add(index, worlds);
    }

    /**
     * Check whether the last finite rank is empty
     * 
     * @return Whether the last rank is empty
     */
    public boolean hasEmptyCurrentRank(){
        return this.ranks.get(this.getRankCount()-1).size() == 0;
    }

    /**
     * Returns the string representation of the RankedInterpretation in the usual format - a number of discrete
     * levels or ranks.
     * 
     * @return The string representation
     */
    public String toString(){
        String template = "%-3s:\t%s\n";
        String output = String.format(template, "∞", this.getInfiniteRank());
        for(int index = this.getRankCount()-1; index >= 0; --index){
            output += String.format(template, index, this.getRank(index));
        }
        return output.trim();
    }
    
}
