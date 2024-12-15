package org.example;

public interface Model {

  /**
   * Запланировать событие event на время time
   * Событие не может быть null
   * Время абсолютное
   * 
   * @param event
   * @param time
   */
  void planEvent(Object event, Long time);

  /**
   * Запланировать событие event через time
   * Событие не может быть null
   * 
   * @param event
   * @param time
   */
  default void planEventAfter(Object event, Long time) {
    planEvent(event, getTime() + time);
  }

  /**
   * Получить текущее модельное время
   * 
   * @return
   */
  long getTime();

  /**
   * Выслать сигнал. По умолчание планирует событие на текущий момент.
   * 
   * @param signal
   */
  default void sendSignal(Object signal) {
    planEvent(signal, getTime());
  }

  /**
   * Ожидать получения сигнала(события).
   * После возникновения удаляет его из календаря
   * 
   * @param <T>
   * @param signal
   */
  <T> void waitSignal(Class<T> signal);

  /**
   * Блокировка процесса до вознивения события
   * 
   * @param <T>
   * @param event
   */
  <T> void waitEvent(Class<T> event);

  /**
   * Получить ближайшее событие из календаря и обработать его
   */
  void takeEvent();

  /**
   * Обрабaтывать события из календаря, пока временя неизменно
   */
  void makeTimeStep();

}
