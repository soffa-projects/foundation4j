package dev.soffa.foundation.data.spring.features;

import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.SimpleRepository;
import dev.soffa.foundation.extra.settings.Setting;
import dev.soffa.foundation.extra.settings.SettingRepository;
import org.springframework.stereotype.Component;

@Component
public class SettingRepositoryImpl extends SimpleRepository<Setting, String> implements SettingRepository {

    public SettingRepositoryImpl(DB db) {
        super(db, Setting.class);
    }

}
