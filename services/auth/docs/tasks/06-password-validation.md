# 06 — Валидация пароля и validation starter

## Цель

Подключить `spring-boot-starter-validation` и реализовать правила пароля из [spec.md](../spec.md).


## Методология TDD

| Фаза | Действие |
|------|----------|
| **Red** | Написать падающий тест → `mvn test` должен упасть |
| **Green** | Минимальная реализация → тесты зелёные |
| **Refactor** | Улучшить код, тесты остаются зелёными |

## Предусловия

- [04-dto-request.md](./04-dto-request.md)

## Зависимости (`pom.xml`)

### Добавить

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## Правила пароля (из spec)

| Правило | Требование |
|---------|------------|
| Длина | 10–20 символов |
| Символы | латиница, цифры, спецсимволы |
| Строчная | ≥ 1 |
| Заглавная | ≥ 1 |
| Цифра | ≥ 1 |
| Спецсимволы | ≥ 3 (например `!@#$%^&*`) |

## Правила логина

| Правило | Требование |
|---------|------------|
| Символы | только латиница и цифры |
| Длина | до 20 символов |

## Шаги (Red → Green → Refactor)

### 1. Кастомная аннотация `@ValidPassword`

**Файл:** `validation/ValidPassword.java` + `PasswordValidator.java`

```java
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword { ... }
```

### 2. Аннотации на DTO

```java
public class RegisterDto {
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @JsonProperty("login")
    private String login;

    @NotBlank
    @ValidPassword
    @JsonProperty("password")
    private String password;
}
```

### 3. Unit-тесты `PasswordValidatorTest`

| Пароль | Ожидание |
|--------|----------|
| `short1!` | invalid (длина) |
| `alllowercase123!!!` | invalid (нет заглавной) |
| `Str0ng!!!Pass` | valid |

## Критерии готовности

- [ ] `spring-boot-starter-validation` в pom
- [ ] Все правила из spec покрыты тестами
- [ ] `@Valid` на DTO в контроллере ([12](./12-controller-rest.md))

## Связанные задачи

- [09-service-register.md](./09-service-register.md)
- [12-controller-rest.md](./12-controller-rest.md)
