package com.smart.zwj.zroute_annotation.bean;

import java.util.List;

public class ZRouteItem {

    private String className;
    private List<String> paths;
    private String funName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getFunName() {
        return funName;
    }

    public void setFunName(String funName) {
        this.funName = funName;
    }
}
