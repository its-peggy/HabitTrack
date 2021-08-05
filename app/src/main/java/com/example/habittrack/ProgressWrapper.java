package com.example.habittrack;

import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;

import java.io.Serializable;
import java.util.List;

public class ProgressWrapper implements Serializable {
    List<Progress> progressList;
    public ProgressWrapper(List<Progress> progressList) {
        this.progressList = progressList;
    }
    public List<Progress> getProgressList() {
        return this.progressList;
    }
}
