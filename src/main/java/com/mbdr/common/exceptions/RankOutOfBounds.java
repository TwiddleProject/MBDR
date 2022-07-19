package com.mbdr.common.exceptions;

public class RankOutOfBounds extends RuntimeException {
    
    public RankOutOfBounds() {
        super();
    }

    public RankOutOfBounds(String message)
    {
        super(message);
    }
}
