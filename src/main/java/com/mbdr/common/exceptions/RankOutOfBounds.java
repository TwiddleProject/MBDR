package com.mbdr.common.exceptions;

/**
 * Exception for out of bounds rank in ranking
 */
public class RankOutOfBounds extends RuntimeException {
    
    /**
     * Default constructor
     */
    public RankOutOfBounds() {
        super();
    }

    /**
     * Parameterized constructor
     * 
     * @param message The error message
     */
    public RankOutOfBounds(String message)
    {
        super(message);
    }
}
