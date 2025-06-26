package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableAsync
public class SpringAsyncConfig {

//    @Bean(name = "userEventExecutor")
//    public ThreadPoolTaskExecutor userEventExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(1);
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(10);
//        executor.setThreadNamePrefix("UserEvent-");
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // will run extra task in main tread
//        executor.initialize();
//        return executor;
//    }

    @Bean(name = "userEventExecutor")
    public Executor userEventExecutor() {
        // Create a queue that blocks when full
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);

        // Custom thread factory (optional)
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(@Nullable Runnable runnable) {
                return new Thread(runnable, "UserEvent-" + counter.incrementAndGet());
            }
        };

        // Rejection policy that blocks until space is available
        RejectedExecutionHandler blockingHandler = (r, executor) -> {
            try {
                // Block until space is available
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("Interrupted while waiting to queue task", e);
            }
        };

        return new ThreadPoolExecutor(1, 10, 60, TimeUnit.SECONDS,
                queue, threadFactory, blockingHandler);
    }
}
