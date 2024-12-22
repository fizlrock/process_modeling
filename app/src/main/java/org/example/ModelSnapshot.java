package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;


@ToString
@Getter
public class ModelSnapshot {
  public Long time;
  public Integer aliveProcesses;
  public Integer plannedEventCount;
  public Map<String, Object> params = new HashMap<>();
}
