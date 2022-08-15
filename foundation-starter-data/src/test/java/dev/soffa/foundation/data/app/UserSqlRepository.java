package dev.soffa.foundation.data.app;

import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.SimpleRepository;

import javax.inject.Named;

@Named
public class UserSqlRepository extends SimpleRepository<User, String> implements UserRepository {
    public UserSqlRepository(DB db) {
        super(db, "users");
    }
}
