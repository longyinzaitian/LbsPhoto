package com.lbsphoto.app;

import android.net.Uri;

import java.io.Serializable;
import java.util.TreeMap;

public class SelectedTreeMap implements Serializable {
    private TreeMap<Long, Uri> treeMap;

    public TreeMap<Long, Uri> getTreeMap() {
        return treeMap;
    }

    public void setTreeMap(TreeMap<Long, Uri> treeMap) {
        this.treeMap = treeMap;
    }
}
