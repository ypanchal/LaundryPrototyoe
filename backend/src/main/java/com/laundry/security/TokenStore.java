package com.laundry.security;

import com.laundry.model.Role;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple in-memory session store, deliberately not JWT, to keep this
 * prototype easy to read. Tokens live only as long as the server process runs.
 *
 * For production: replace with Spring Security + JWT (stateless) or a
 * proper session store backed by Redis/DB.
 */
@Component
public class TokenStore {

    public record Session(String phone, Role role, String name) {}

    private final Map<String, Session> tokens = new ConcurrentHashMap<>();

    public String createToken(String phone, Role role, String name) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new Session(phone, role, name));
        return token;
    }

    public Session resolve(String token) {
        return tokens.get(token);
    }

    public void invalidate(String token) {
        tokens.remove(token);
    }
}
