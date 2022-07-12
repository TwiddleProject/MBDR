package com.mbdr.utils.exceptions;

public class RankOutOfBounds extends RuntimeException {
    
    public RankOutOfBounds() {
        super();
    }

    public RankOutOfBounds(String message)
    {
        super(message);
    }
}
