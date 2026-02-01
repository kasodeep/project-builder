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