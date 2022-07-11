package com.mbdr.services;

import com.mbdr.structures.DefeasibleKnowledgeBase;

public interface RankConstructor<T> {
    
    public T construct(DefeasibleKnowledgeBase knowledge);

}