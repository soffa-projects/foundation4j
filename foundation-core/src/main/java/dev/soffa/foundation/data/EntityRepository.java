package dev.soffa.foundation.data;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.Logger;
import dev.soffa.foundation.commons.RandomUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.error.FunctionalException;
import dev.soffa.foundation.model.TenantId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnusedReturnValue")
public interface EntityRepository<E> {

    int SHORT_ID_RETRIES_TRESHOLD_1 = 10;
    int SHORT_ID_RETRIES_TRESHOLD_2 = 50;

    default String shortId(String prefix, int length, int maxLength) {
        int count = 0;
        while (true) {
            String id = TextUtil.join("_", prefix, RandomUtil.nextString(length).toLowerCase());
            if (!exists(id)) {
                return id;
            }
            count++;
            if (count == SHORT_ID_RETRIES_TRESHOLD_1) {
                Logger logger = Logger.get(getClass());
                if (length == maxLength) {
                    logger.warn("Could not generate unique id after 10 tries with length to %d", length);
                } else {
                    logger.warn("Could not generate unique id after 10 tries, increasing length to %d", length);
                    return shortId(prefix, length + 1, maxLength);
                }
            } else if (count == SHORT_ID_RETRIES_TRESHOLD_2) {
                throw new FunctionalException("Could not generate unique id after 50 tries");
            }
        }
    }

    long count();

    default long count(Map<String, Object> filter) {
        return count(Criteria.of(filter));
    }

    long count(Criteria criteria);

    List<E> findAll();

    List<E> find(Criteria criteria);

    default List<E> find(Map<String, Object> filter) {
        return find(Criteria.of(filter));
    }

    Optional<E> get(Criteria criteria);

    Optional<E> get(TenantId tenant, Criteria criteria);

    default Optional<E> get(Map<String, Object> filter) {
        return get(Criteria.of(filter));
    }

    default Optional<E> get(TenantId tenant, Map<String, Object> filter) {
        return get(tenant, Criteria.of(filter));
    }

    Optional<E> findById(Object value);

    Optional<E> findById(TenantId tenant, Object value);

    E insert(E entity);

    E insert(TenantId tenant, E entity);

    E update(E entity);

    E update(TenantId tenant, E entity);

    int delete(E entity);

    int delete(TenantId tenant, E entity);

    default int delete(Map<String, Object> filter) {
        return delete(Criteria.of(filter));
    }

    int delete(Criteria criteria);

    default boolean exists(Map<String, Object> filter) {
        return exists(Criteria.of(filter));
    }

    default boolean exists(Criteria criteria) {
        return count(criteria) > 0;
    }

    default boolean exists(String id) {
        return exists(ImmutableMap.of("id", id));
    }

     void withLock();

}
