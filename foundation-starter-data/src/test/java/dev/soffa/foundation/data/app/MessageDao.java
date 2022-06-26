package dev.soffa.foundation.data.app;

import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.SimpleRepository;
import dev.soffa.foundation.data.app.model.Message;
import dev.soffa.foundation.model.TenantId;
import org.springframework.stereotype.Component;

@Component
public class MessageDao extends SimpleRepository<Message, String> {
    public MessageDao(DB db) {
        super(db, "messages", TenantId.DEFAULT_VALUE);
    }
}
