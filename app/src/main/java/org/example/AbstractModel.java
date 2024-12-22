package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractModel {

  public static class InitEvent {
  }

  Queue<PlannedEvent> calendar = new PriorityQueue<>();

  public int getCalendarSize() {
    return calendar.size();
  }

  public int getAliveProcesses() {
    return aliveProcesses.size();
  }

  /**
   * Словарь <Событие> - <Список процессов которые активируются>
   */
  private Map<Object, List<ModelingProcess>> event_activate_process_map = new HashMap<>();

  private Map<Object, List<EventRequest>> eventRequests = new HashMap<>();

  private Set<Thread> aliveProcesses = new HashSet<>();

  {
    initProcesses();
    info("Найденные процессы: {}", event_activate_process_map);
    planEvent(new InitEvent(), 0l);
  }

  private long currentTime;

  private void startProcess(ModelingProcess proc) {
    debug("Запуск процесса: {}", proc.getName());
    var th = new Thread(proc.getRunnable());
    th.start();

    // Ожидание завершения или блокировки процесса
    while (th.getState() == Thread.State.RUNNABLE)
      ;
    // Если заблокировался, то добавляем его в список "живых" потоков
    if (th.isAlive())
      aliveProcesses.add(th);
    else
      debug("Завершение процесса: {}", proc.getName());
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

    debug("Планирование события {} на момент {} TU", event, time);
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

  // TODO написать доку
  protected <T> void waitEvent(Object o) {

    var requests = eventRequests.get(o);

    if (requests == null) {
      requests = new ArrayList<>();
      eventRequests.put(o, requests);
    }

    var eventRequest = new EventRequest(Thread.currentThread(), new Semaphore(0, true));
    requests.add(eventRequest);

    try {
      eventRequest.sem().acquire();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  private void debug(String message, Object... args) {
    message = "time: " + currentTime + " " + message;
    log.debug(message, args);
  }

  private void info(String message, Object... args) {
    message = "time: " + currentTime + " " + message;
    log.info(message, args);
  }

  private void takeEvent() {

    var plannedEvent = calendar.poll();
    var event = plannedEvent.event();
    currentTime = plannedEvent.time();
    debug("Обработка события: {}", event);

    // Проверить, ждет ли кто-то событие как сигнал, если да, удалить событие и
    // вернуть из метода

    // Найти все методы которые ожидают события и продолжить

    var requests = eventRequests.remove(event);
    if (requests != null) {
      debug("Возобновление методов ожидающих событие");

      for (EventRequest r : requests) {
        r.sem().release();
        try {
          // TODO ???
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        while ((r.th().getState() == Thread.State.RUNNABLE))
          ;

        info("state: {}", r.th().getState());
        if (r.th().getState() == Thread.State.TERMINATED) {
          debug("Завершение процесса");
          aliveProcesses.remove(r.th());
        }
      }
      // int queue = semaphore.getQueueLength();
    }

    // Найти все методы которые активируются от события и активировать
    var procToActive = event_activate_process_map.get(event.getClass());

    if (procToActive != null && !procToActive.isEmpty()) {
      procToActive.stream().forEach(p -> startProcess(p));
    }
    // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method 'takeEvent'");
  }

  public void run(long timeLimit) {
    info("Начало моделирования");
    while (!calendar.isEmpty() && getTime() < timeLimit)
      takeEvent();
    info("Конец моделирования");
    aliveProcesses.forEach(x -> x.interrupt());

  }

  public void makeTimeStep() {
    resetValues();

    long st = getTime();
    while (st == getTime())
      takeEvent();
  }

  public void resetValues() {

    var fields = this.getClass().getDeclaredFields();

    Stream.of(fields)
        .filter(f -> f.isAnnotationPresent(ResetOnTick.class))
        .peek(f -> f.canAccess(this))
        .peek(f -> log.debug("Обнуление: {}", f))
        .peek(f -> {
          try {
            f.set(this, getDefaultValue(f.getType()));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .forEach(x -> {
        });

    ;

  }

  private Object getDefaultValue(@SuppressWarnings("rawtypes") Class clazz) {
    if (clazz.equals(Long.class)) {
      return 0l;
    } else
      throw new UnsupportedOperationException("Неизвестно значение по умолчанию для типа: " + clazz);

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

  protected void sleep(long time) {
    throw new UnsupportedOperationException("Unimplemented method 'sleep'");
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
            }
          };

          var an = m.getAnnotation(MProccess.class);

          var proc = new ModelingProcess(m.getName(), runnable);

          var list = event_activate_process_map.get(an.activateOn());
          if (list == null) {
            list = new ArrayList<ModelingProcess>();

            list.add(proc);
            event_activate_process_map.put(an.activateOn(), list);
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
