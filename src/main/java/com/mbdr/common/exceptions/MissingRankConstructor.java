package com.mbdr.common.exceptions;

public class MissingRankConstructor extends RuntimeException{
    public MissingRankConstructor(){
        super();
    }

    public MissingRankConstructor(String message){
        super(message);
    }
}
