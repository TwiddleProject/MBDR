package com.mbdr.common.exceptions;

/**
 * Exception for missing ranking for reasoner
 */
public class MissingRanking extends RuntimeException{
    
    /**
     * Default constructor
     */
    public MissingRanking(){
        super();
    }

    /**
     * Parameterized constructor
     * 
     * @param message The error message
     */
    public MissingRanking(String message){
        super(message);
    }
}
