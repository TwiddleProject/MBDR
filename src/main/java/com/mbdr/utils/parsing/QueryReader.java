package com.mbdr.utils.parsing;

//TODO: refactor to no longer require fixed path to allow for reading from different directories - e.g. KB files and query files may be in different directories
public class QueryReader extends FileReader{

    public QueryReader(String path) {
        super(path);
    }

    public String getQueryFileName(String knowledgeBaseFileName){
        int dotIndex = knowledgeBaseFileName.lastIndexOf('.');
        String suffix = knowledgeBaseFileName.substring(dotIndex);
        return knowledgeBaseFileName.substring(0, dotIndex) + "_queries" + suffix;
    }

}
