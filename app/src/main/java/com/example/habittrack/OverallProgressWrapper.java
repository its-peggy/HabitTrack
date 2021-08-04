package com.example.habittrack;

import com.example.habittrack.models.OverallProgress;

import java.io.Serializable;
import java.util.List;

public class OverallProgressWrapper implements Serializable {
    List<OverallProgress> overallProgressList;
    public OverallProgressWrapper(List<OverallProgress> list) {
        overallProgressList = list;
    }
    public List<OverallProgress> getOverallProgressList() {
        return overallProgressList;
    }
}
