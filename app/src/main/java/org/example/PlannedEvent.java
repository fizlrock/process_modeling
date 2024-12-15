package org.example;

public record PlannedEvent(long time, Object event) implements Comparable<PlannedEvent> {

    @Override
    public int compareTo(PlannedEvent event) {
      long delta = event.time - this.time;
      if (delta < 0)
        return 1;
      else if (delta > 0)
        return -1;
      else
        return 0;
    }
  }
