package org.example;

import java.util.stream.Stream;

/**
 * Snapshoter
 */
public class Snapshoter {

  public static ModelSnapshot takeSnapshot(AbstractModel model) {

    var snap = new ModelSnapshot();

    snap.time = model.getTime();
    snap.plannedEventCount = model.getCalendarSize();
    snap.aliveProcesses = model.getAliveProcesses();

    var fields = model.getClass().getDeclaredFields();

    var observed_fields = Stream.of(fields)
        .filter(f -> f.isAnnotationPresent(Observed.class));

    observed_fields.forEach(f -> {
      try {
        f.setAccessible(true);
        var value = f.get(model);
        var name = f.getName();

        snap.params.put(name, value);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
      }
    });

    return snap;
  }

}
