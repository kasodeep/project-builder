## Learnings

### Exception Handling

- Using a standard error response to what the framework provides for consistency.
- Errors have priority and a general @ExceptionHandler with avoid leaking sensitive data.
- We have error messaged where each message contains parameters for better details.
- How can we manage db based errors ??

### Mapper

1. We create mappers whose namespace maps our repository interface containing method signatures.
2. Then the method name must map to the id of the query, and resultMap denoting the type returned.
3. When we have 2 params, the dot `.` operator must be used. Else direct names are fine #{name}.
4. Batis maps the ResultRow to a List<T>, but can't map GROUP BY to a list, custom type handler.

### Models

- Domain Model
  User
  ├── id
  ├── username
  ├── email
  ├── role
  └── team (object)

- Persistence Model
  user
  ├── id
  ├── username
  ├── email
  ├── role
  └── team_id (FK)

### DB Config

1. We remove jpa as it causes spring to behave weird with hikari-cp.
2. When providing a config it provides a connection pool.
3. The session remains for transactional methods. More on isolation levels.

### Date

Date only → LocalDate
Event timestamp → Instant

### Consistency

1. When the application becomes read heavy and concurrency issues maybe present.
2. We can apply pessimistic lock, isolation level = SERIALIZABLE, or optimistic lock.

### Events

1. To avoid running events in the same transaction as the db logic.
2. We can use the @TransactionalEventListener for events with phase = afterCommit.
3. afterCommit is okay for:
   - cache invalidation
   - email sending
   - logging

4. Your analytics system is derived state.
5. Derived state must be:
✔ rebuildable
✔ replayable
✔ auditable

### Outbox Pattern

1. Since service logic, needs transaction, events firing under the same call leads to errors, no trace.
2. We store the events with the payload serialized in the outbox table. Marked PENDING.
3. Then our worker batches the events together, fires them and handled by the listener.
4. The pattern allows us to move to a separate db or listener outside our app.
5. We use JSONB for db storage.

### Async Config

### Optimizations

1. Removed Team from user to add teamId allowing reduction in join for each auth query.

```sql
SELECT id
FROM project
WHERE id = #{projectId}
FOR UPDATE
```

```java

@PostMapping("/auth/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,
                                          HttpServletResponse response) {

    User user = authService.authenticate(request.username(), request.password());

    String accessToken = jwtService.createAccessToken(user);   // 5–15 min
    String refreshToken = jwtService.createRefreshToken(user); // 15 days

    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)              // true even in local if using https
            .sameSite("Strict")
            .path("/auth/refresh")
            .maxAge(Duration.ofDays(15))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    return ResponseEntity.ok(new AuthResponse(accessToken));
}

@PostMapping("/auth/refresh")
public ResponseEntity<AuthResponse> refresh(
        @CookieValue("refreshToken") String refreshToken) {

    User user = jwtService.validateRefreshToken(refreshToken);

    String newAccessToken = jwtService.createAccessToken(user);

    return ResponseEntity.ok(new AuthResponse(newAccessToken));
}

@PostMapping("/auth/logout")
public ResponseEntity<Void> logout(HttpServletResponse response) {

    ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/auth/refresh")
            .maxAge(0)
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    return ResponseEntity.ok().build();
}
```
