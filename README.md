## Learnings

### JPA vs Mapper

1. When we have relations such as many users belong to one team, we can use old jpa to map them.
2. But it can cause unexpected joins and N+1 query problem.
3. Mapper provides full control over the query and hence we can operate using default association within a result, or select within a result (Avoid).

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

### Date

Date only → LocalDate
Event timestamp → Instant

### Problems

1. DB Max Pool config
2. Whitelist URL Issue
3. Sending error like:
   {
   "timestamp": "2026-01-30T08:51:19.568Z",
   "status": 401,
   "error": "Unauthorized",
   "path": "/api/v1/team/all"
   }
4. Logs related to db
5. all project related.
6. Mapper related (when 2 or more params we need . else directly)