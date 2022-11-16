package dev.soffa.foundation.extra.settings;

import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.DefaultIdGenerator;
import dev.soffa.foundation.data.EntityRepository;

import java.util.Locale;
import java.util.Optional;

public interface SettingRepository extends EntityRepository<Setting, String> {

    default Optional<String> getValue(String key)  {
        Optional<Setting> setting = get(ImmutableMap.of("key", key.toLowerCase(Locale.ROOT)));
        if (setting.isPresent()) {
            return Optional.of(setting.get().getValue());
        }
        return Optional.empty();
    }
    default void setValue(String key, String value)  {
        Optional<Setting> setting = get(ImmutableMap.of("key", key.toLowerCase(Locale.ROOT)));
        if (setting.isPresent()) {
            setting.get().setValue(value);
            update(setting.get(), "value");
        }else {
            insert(new Setting(
                DefaultIdGenerator.uuid(Setting.ID_PREFIX),
                key.toLowerCase(Locale.ROOT),
                null,
                value
            ));
        }
    }


}
