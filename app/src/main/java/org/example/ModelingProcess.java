package org.example;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

// public record ModelingProcess(String name, Runnable runnable) {
// }

@ToString
@RequiredArgsConstructor
@Getter
public class ModelingProcess {
  private final String name;

  @ToString.Exclude
  private final Runnable runnable;

}
