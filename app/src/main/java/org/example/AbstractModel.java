package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractModel {

  public static class InitEvent {
  }

  Queue<PlannedEvent> calendar = new PriorityQueue<>();

  /**
   * Словарь <Событие> - <Список процессов которые активируются>
   */
  private Map<Object, List<ModelingProcess>> event_process_map = new HashMap<>();

  private List<Thread> aliveProcesses = new ArrayList<>();

  {
    initProcesses();
    log.info("Найденные процессы: {}", event_process_map);
    planEvent(new InitEvent(), 0l);
  }

  long currentTime;

  private void startProcess(ModelingProcess proc) {
    log.debug("Запуск процесса: {}", proc.getName());
    var th = new Thread(proc.getRunnable());
    th.start();

    // Ожидание завершения или блокировки процесса
    while (th.getState() == Thread.State.RUNNABLE)
      ;
    // Если заблокировался, то добавляем его в список "живых" потоков
    if (th.isAlive())
      aliveProcesses.add(th);
    else
      log.debug("Завершение процесса: {}", proc.getName());
  }

  /**
   * Запланировать событие event на время time
   * Событие не может быть null
   * Время абсолютное
   * 
   * @param event
   * @param time
   */
  protected void planEvent(Object event, Long time) {
    if (time < currentTime)
      throw new IllegalArgumentException(
          String.format("Нельзя запланировать событие на указанное время: time: {} event_time {}",
              currentTime,
              time));

    log.debug("Планирование события {} на момент {} TU", event.getClass().getSimpleName(), time);
    calendar.add(new PlannedEvent(time, event));
  }

  /**
   * Ожидать получения сигнала(события).
   * После возникновения удаляет его из календаря
   * 
   * @param <T>
   * @param signal
   */
  protected <T> void waitSignal(Class<T> signal) {
    // TODO Auto-generated method stub

    throw new UnsupportedOperationException("Unimplemented method 'waitSignal'");
  }

  protected <T> void waitEvent(Class<T> event) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'waitEvent'");
  }

  public void takeEvent() {

    var plannedEvent = calendar.poll();
    var event = plannedEvent.event();
    currentTime = plannedEvent.time();
    log.debug("Обработка события: {}", event.getClass().getSimpleName());

    // Проверить, ждет ли кто-то событие как сигнал, если да, удалить событие и
    // вернуть из метода

    // Найти все методы которые ожидают события и продолжить

    // Найти все методы которые активируются от события и активировать
    var procToActive = event_process_map.get(event.getClass());

    if (procToActive != null && !procToActive.isEmpty()) {
      procToActive.stream().forEach(p -> startProcess(p));
    }
    // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method 'takeEvent'");
  }

  public void run() {
    while (!calendar.isEmpty())
      takeEvent();
  }

  public void makeTimeStep() {
    long st = getTime();
    while (st == getTime())
      takeEvent();
  }

  /**
   * Получить текущее модельное время
   * 
   * @return
   */
  public long getTime() {
    return currentTime;
  }

  /**
   * Выслать сигнал. По умолчание планирует событие на текущий момент.
   * 
   * @param signal
   */
  protected void sendSignal(Object signal) {
    planEvent(signal, getTime());
  }

  /**
   * Метод ищет методы объявленные с аннотацией MProcсess и добавляет их в
   * коллекцию event_process_map
   */
  private void initProcesses() {
    var methods = this.getClass().getDeclaredMethods();

    Stream.of(methods)
        .filter(m -> m.isAnnotationPresent(MProccess.class))
        .forEach(m -> {

          Runnable runnable = () -> {
            try {
              m.invoke(this);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          };
          var an = m.getAnnotation(MProccess.class);

          var proc = new ModelingProcess(m.getName(), runnable);

          var list = event_process_map.get(an.activateOn());
          if (list == null) {
            list = new ArrayList<ModelingProcess>();

            list.add(proc);
            event_process_map.put(an.activateOn(), list);
          } else
            list.add(proc);

        });
  }

  /**
   * Запланировать событие event через time
   * Событие не может быть null
   * 
   * @param event
   * @param time
   */
  protected void planEventAfter(Object event, Long time) {
    planEvent(event, getTime() + time);
  }
}
