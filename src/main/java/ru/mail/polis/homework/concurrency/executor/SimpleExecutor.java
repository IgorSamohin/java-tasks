package ru.mail.polis.homework.concurrency.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Нужно сделать свой экзекьютор с линивой инициализацией потоков до какого-то заданного предела.
 * Линивая инициализация означает, что если вам приходит раз в 5 секунд задача, которую вы выполняете 2 секунды,
 * то вы создаете только один поток. Если приходит сразу 2 задачи - то два потока.  То есть, если приходит задача
 * и есть свободный запущенный поток - он берет задачу, если такого нет, то создается новый поток.
 * <p>
 * Задачи должны выполняться в порядке FIFO
 * Потоки после завершения выполнения задачи НЕ умирают, а ждут.
 * <p>
 * Max 10 баллов
 * <p>
 * Напишите 3 теста (2 балла за тест)
 * 1) запуск 1 задачи несколько раз с интервалом (должен создаться только 1 поток)
 * 2) запуск параллельно n - 1 задач несколько раз (должно создаться n - 1 потоков) и задачи должны завершится
 * примерно одновременно
 * 3) запуск параллельно n + m задач несколько раз (должно создаться n потоков) и первые n задач должны завершится
 * примерно одновременно, вторые m задач должны завершиться чуть позже первых n и тоже примерно одновременно
 * Max 6 баллов
 */
public class SimpleExecutor implements Executor {
    private int max;
    private final BlockingQueue<Runnable> tasks = new LinkedBlockingDeque<>();
    private final List<Worker> threads = new ArrayList<>();

    public SimpleExecutor(int max) {
        this.max = max;
    }

    @Override
    public void execute(Runnable command) {
        Worker thread = getFreeTread();
        if (thread == null) {
            thread = new Worker();
            thread.start();
            threads.add(thread);
        }

        try {
            tasks.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            thread.waitTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Worker getFreeTread() {
        for (Worker e : threads) {
            if (e.getState() == Thread.State.WAITING) {
                return e;
            }
        }
        return null;
    }

    /**
     * Должен возвращать количество созданных потоков.
     */
    public int getLiveThreadsCount() {
        return 0;
    }

    private class Worker extends Thread {
        public synchronized void waitTask() throws InterruptedException {
            while(tasks.isEmpty()){
                wait();
            }
            start();
        }

        @Override
        public void run() {
            while (true){
                try {
                    tasks.take().run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
