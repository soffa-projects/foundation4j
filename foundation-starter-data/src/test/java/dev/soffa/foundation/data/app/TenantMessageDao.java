package dev.soffa.foundation.data.app;

import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.SimpleRepository;
import dev.soffa.foundation.data.app.model.Message;
import org.springframework.stereotype.Component;

@Component
public class TenantMessageDao extends SimpleRepository<Message, String> {
    public TenantMessageDao(DB db) {
        super(db, "messages");
    }
}
