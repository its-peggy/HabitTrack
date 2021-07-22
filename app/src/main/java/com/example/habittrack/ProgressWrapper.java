package com.example.habittrack;

import com.example.habittrack.models.Progress;

import java.io.Serializable;
import java.util.List;

public class ProgressWrapper implements Serializable {
    List<Progress> progress;
    public ProgressWrapper (List<Progress> p){
        progress = p;
    }
    public List<Progress> getProgress() {
        return progress;
    }
}