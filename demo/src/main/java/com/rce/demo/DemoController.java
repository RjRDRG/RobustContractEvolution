package com.rce.demo;

import com.rce.demo.exceptions.BadRequestException;
import com.rce.demo.exceptions.NotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
class DemoController {
    private final ConcurrentHashMap<Long,User> users;

    DemoController() {
        users = new ConcurrentHashMap<>();
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable long id) {
        if(!users.containsKey(id))
            throw new NotFoundException("User not found");

        return users.get(id);
    }

    @PostMapping("/user")
    public long createUser(@RequestBody User user) {
        if(users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new BadRequestException("Email already exists");
        }

        long id = new Random().nextLong();
        users.put(id,user);
        return id;
    }
}
