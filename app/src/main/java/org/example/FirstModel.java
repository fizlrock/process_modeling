package org.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirstModel extends AbstractModel {





  @MProccess(activateOn = InitEvent.class)
  void initProcess() {
    log.info("Доброе утро");
    planEventAfter(new InitEvent(), 10l);

  }

}
