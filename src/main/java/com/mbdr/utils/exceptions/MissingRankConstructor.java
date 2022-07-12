package com.mbdr.utils.exceptions;

public class MissingRankConstructor extends RuntimeException{
    public MissingRankConstructor(){
        super();
    }

    public MissingRankConstructor(String message){
        super(message);
    }
}
