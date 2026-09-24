package com.mutuals.activity.service;

import com.mutuals.activity.entity.Activity;
import com.mutuals.activity.entity.ActivityType;
import com.mutuals.activity.repository.ActivityRepository;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityPublisher {

    private final ActivityRepository activityRepository;

    @Transactional
    public Activity publish(User actor, ActivityType type, String title, Integer value) {
        Activity activity = new Activity();
        activity.setActor(actor);
        activity.setType(type);
        activity.setTitle(title);
        activity.setValue(value);
        return activityRepository.save(activity);
    }
}
