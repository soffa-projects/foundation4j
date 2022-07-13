package dev.soffa.foundation.spring.resource;

import dev.soffa.foundation.activity.ActivityService;
import dev.soffa.foundation.model.Counter;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// @Profile("test")
// @RestController
@AllArgsConstructor
// @RequestMapping("/_activities")
public class ActivityTestResource {

    private final ActivityService activities;

    @GetMapping("{name}/count")
    public ResponseEntity<Counter> getActivityCount(final @PathVariable String name) {
        long count = activities.count(name);
        return ResponseEntity.ok(new Counter(name, count));
    }
}
