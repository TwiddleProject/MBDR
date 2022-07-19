package com.mbdr.modelbased.structures;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

// import com.mbdr.structures.Ranking;
import com.mbdr.utils.exceptions.RankOutOfBounds;


public class RankedInterpretation { //implements Ranking<NicePossibleWorld>
    
    private ArrayList<Set<NicePossibleWorld>> ranks;

    public RankedInterpretation(){
        this(1);
    }

    public RankedInterpretation(int ranks){
        this.ranks = new ArrayList<>();
        for(int index = 0; index <= ranks; ++index){
            this.ranks.add(new HashSet<>());
        }
    }

    public RankedInterpretation(ArrayList<Set<NicePossibleWorld>> ranks){
        this.ranks = ranks;
    }

    public int getRankCount(){
        return this.ranks.size() - 1;
    }

    public Set<NicePossibleWorld> getRank(int index){
        if(index >= this.ranks.size()-1 || index < 0){
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        return this.ranks.get(index);
    }

    public Set<NicePossibleWorld> getInfiniteRank(){
        return this.ranks.get(this.ranks.size()-1);
    }

    public void addToRank(int index, Set<NicePossibleWorld> worlds){
        if(index >= this.ranks.size()-1 || index < 0){
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        this.ranks.get(index).addAll(worlds);
    }

    public void addToInfiniteRank(Set<NicePossibleWorld> worlds){
        this.getInfiniteRank().addAll(worlds);
    }

    public void addToRank(int index, NicePossibleWorld world){
        if(index >= this.ranks.size()-1 || index < 0){
            throw new RankOutOfBounds("Rank " + index + " does not exist.");
        }
        this.ranks.get(index).add(world);
    }

    public void addToRank(NicePossibleWorld world){
        this.ranks.get(this.getRankCount()-1).add(world);
    }

    public void addToInfiniteRank(NicePossibleWorld world){
        this.getInfiniteRank().add(world);
    }

    public int addRank(){
        int position = this.getRankCount();
        this.ranks.add(position, new HashSet<NicePossibleWorld>());
        return position;
    }

    public void addRank(int index){
        if(index > this.getRankCount() || index < 0){
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.add(index, new HashSet<NicePossibleWorld>());
    }

    public void addRank(int index, Set<NicePossibleWorld> worlds){
        if(index > this.getRankCount() || index < 0){
            throw new RankOutOfBounds("Rank " + index + " is out of bounds.");
        }
        this.ranks.add(index, worlds);
    }

    public boolean hasEmptyCurrentRank(){
        return this.ranks.get(this.getRankCount()-1).size() == 0;
    }

    public String toString(){
        String template = "%-3s:\t%s\n";
        String output = String.format(template, "∞", this.getInfiniteRank());
        for(int index = this.getRankCount()-1; index >= 0; --index){
            output += String.format(template, index, this.getRank(index));
        }
        return output.trim();
    }
    
}
