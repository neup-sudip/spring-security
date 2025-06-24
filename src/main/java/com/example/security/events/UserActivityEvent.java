package com.example.security.events;

import com.example.security.entity.UserActivity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserActivityEvent extends ApplicationEvent {

    private final UserActivity activity;

    public UserActivityEvent(Object source, UserActivity activity) {
        super(source);
        this.activity = activity;
    }
}