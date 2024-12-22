package org.example;

import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LifeModel extends AbstractModel {

  final long livingConditionsPeriod = 6;

  @Observed
  double livingConditions = 1;

  @Observed
  @ResetOnTick
  Long cellsAreBurn = 0l;

  @Observed
  Long aliveCells = 0l;

  long ageOfReproduction = 10;

  @MProccess(activateOn = CellBirthEvent.class)
  void cellLive() {
    long cellNumber = cellsAreBurn++;
    aliveCells++;

    long ageTime = ThreadLocalRandom.current().nextLong(5, 10); // Время взросления клетки
    long lifeTime = ThreadLocalRandom.current().nextLong(ageTime, 20);

    String cellGrownEventName = "cellGrown" + cellNumber;

    planEventAfter(cellGrownEventName, ageTime);
    waitEvent(cellGrownEventName);

    if (livingConditions > 0.5) {
      planEventAfter(new CellBirthEvent(), 0l);
      planEventAfter(new CellBirthEvent(), 0l);
    }

    String cellDieEventName = "cellDie" + cellNumber;

    planEventAfter(cellDieEventName, lifeTime - ageTime);
    waitEvent(cellDieEventName);

    aliveCells--;
  }

  @MProccess(activateOn = InitEvent.class)
  void livingConditionChanging() {

    while (true) {
      planEventAfter("conditionChanged", livingConditionsPeriod);
      waitEvent("conditionChanged");
      livingConditions *= ThreadLocalRandom.current().nextDouble(0.79, 1.05);
    }

  }

  @MProccess(activateOn = InitEvent.class)
  void init() {
    planEventAfter(new CellBirthEvent(), 0l);
  }
}

class CellBirthEvent {
}
