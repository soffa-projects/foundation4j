package dev.soffa.foundation.extra.journal;

import dev.soffa.foundation.annotation.Store;
import dev.soffa.foundation.commons.DateUtil;
import dev.soffa.foundation.commons.DigestUtil;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.context.Context;
import dev.soffa.foundation.context.ContextHolder;
import dev.soffa.foundation.data.EntityModel;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Store("f_journal")
public class Journal implements EntityModel {

    public static final String ID_PREFIX = "jr_";
    private String id;
    private String event;
    private String subject;
    private String kind;
    private String data;
    private String status;
    private String error;
    private String traceId;
    private String spanId;
    private String username;
    private String userSession;
    private Date date;
    private Date created;

    public Journal(String kind, String subject, String event) {
        this(ContextHolder.get().orElse(null), kind, subject, event);
    }

    public Journal(Context ctx, String kind, String subject, String event) {
        this.event = event;
        this.subject = subject;
        this.kind = kind;
        this.date = DateUtil.now();
        this.setContext(ctx);
    }

    @Override
    public void onInsert() {
        ContextHolder.get().ifPresent(this::setContext);
    }

    public Journal withContext(Context context) {
        setContext(context);
        return this;
    }

    public void setContext(Context context) {
        if (context==null) {
            return;
        }
        /*
        if (TextUtil.isEmpty(traceId)) {
            traceId = context.getTraceId();
        }
        if (TextUtil.isEmpty(spanId)) {
            spanId = context.getSpanId();
        }*/
        if (TextUtil.isEmpty(username) && context.getAuthentication() != null) {
            username = DigestUtil.md5(context.getAuthentication().getUsername());
        }
        if (TextUtil.isEmpty(userSession)) {
            userSession = DigestUtil.md5(context.getAuthorization());
        }
    }
}
