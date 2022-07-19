package com.mbdr.common.services;

import com.mbdr.common.structures.DefeasibleKnowledgeBase;

public interface RankConstructor<T> {
    
    public T construct(DefeasibleKnowledgeBase knowledge);

}
