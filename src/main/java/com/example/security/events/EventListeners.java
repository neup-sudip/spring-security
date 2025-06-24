package com.example.security.events;

import com.example.security.entity.UserActivity;
import com.example.security.repos.UserActivityDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class EventListeners {

    private final UserActivityDao userActivityDao;

    @Async("userEventExecutor")
    @EventListener
    public void handleUserActivityEvent(UserActivityEvent event) {
        try {
            UserActivity activity = event.getActivity();
            userActivityDao.save(activity);
        } catch (Exception e) {
            log.error("Error saving activity: {}", e.getMessage());
        }
    }
}