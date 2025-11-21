package com.orchestra.domain.model;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioDependency implements Serializable {
    private String scenarioKey;
    private List<String> onStatus;
}

