# Bean Validation Annotations — Practical Guide

> Used with `@Validated` / `@Valid` in Spring controllers
> Backed by **Hibernate Validator (JSR 380 / Jakarta Validation)**

---

## 1. Nullability & String Constraints

### `@NotNull`

* ❌ Fails if value is `null`
* ✅ Accepts empty strings (`""`)

```java

@NotNull
String name;
```

---

### `@NotEmpty`

* ❌ Fails if `null` or empty
* ✅ Accepts whitespace-only strings

```java

@NotEmpty
String name;
```

---

### `@NotBlank`

* ❌ Fails if `null`, empty, or only whitespace
* ✅ **Best for user-facing text**

```java

@NotBlank
String name;
```

---

## 2. Length Constraints

### `@Size`

```java

@Size(min = 3, max = 50)
String name;
```

* Works on:

    * `String`
    * `Collection`
    * `Map`
    * `Array`

---

## 3. Numeric Constraints

### `@Min` / `@Max`

```java

@Min(1)
@Max(100)
int quantity;
```

* Inclusive
* Works on numeric types

---

## 4. Date & Time Constraints

### `@Past`

```java

@Past
LocalDate birthDate;
```

✔ Must be strictly in the past

---

### `@Future`

```java

@Future
LocalDate expiryDate;
```

✔ Must be strictly in the future

---

### `@FutureOrPresent`

```java

@FutureOrPresent
LocalDate startDate;
```

✔ Today or later

---

## 5. Pattern / Regex

### `@Pattern`

```java

@Pattern(regexp = "^[a-zA-Z0-9_]+$")
String username;
```

✔ Only alphanumeric + underscore

---

## 6. Boolean Assertions

### `@AssertTrue`

```java

@AssertTrue
Boolean acceptedTerms;
```

✔ Must be `true`

---

## 7. Nested Object Validation

### `@Valid`

```java

@Valid
private Address address;
```

✔ Validates child object recursively

---

## 8. Cross-Field Validation (Class-Level)

Used when:

* Validation depends on **multiple fields**
* Example: date ranges, password confirmation

---

## Custom Annotation: `@ValidDateRange`

### Annotation Definition

```java

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {

    String message() default "end date must be after start date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

---

## Validator Implementation

### `DateRangeValidator`

```java
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateRangeValidator
        implements ConstraintValidator<ValidDateRange, ProjectCreateRequest> {

    @Override
    public boolean isValid(
            ProjectCreateRequest request,
            ConstraintValidatorContext context
    ) {
        // null handled by @NotNull on fields
        if (request == null) {
            return true;
        }

        LocalDate start = request.start();
        LocalDate end = request.end();

        if (start == null || end == null) {
            return true;
        }

        boolean valid = end.isAfter(start);

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "end date must be after start date"
                    )
                    .addPropertyNode("end")
                    .addConstraintViolation();
        }

        return valid;
    }
}
```

✔ Attaches error to `end` field
✔ Works perfectly with Spring’s `MethodArgumentNotValidException`

---

## 9. Applying to a Record DTO

```java

@ValidDateRange
public record ProjectCreateRequest(

        @NotBlank
        String name,

        @NotNull
        LocalDate start,

        @NotNull
        LocalDate end
) {
}
```

---

## 10. Controller Usage

```java

@PostMapping("/create")
public ResponseEntity<Void> createProject(
        @Validated @RequestBody ProjectCreateRequest request
) {
    projectService.createProject(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
}
```

---

## 11. What Error Looks Like (Your Global Handler)

```json
{
  "status": 400,
  "message": "end date must be after start date",
  "path": "/api/v1/project/create"
}
```

---
