package com.mbdr.common.exceptions;

public class MissingRanking extends RuntimeException{
    public MissingRanking(){
        super();
    }

    public MissingRanking(String message){
        super(message);
    }
}
