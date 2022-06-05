package dev.soffa.foundation.data.app;

import dev.soffa.foundation.data.EntityRepository;

import javax.inject.Named;


@Named
public interface UserRepository extends EntityRepository<User> {
}
