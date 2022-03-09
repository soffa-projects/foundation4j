package dev.soffa.foundation.data.spring.features;

import dev.soffa.foundation.data.DB;
import dev.soffa.foundation.data.SimpleEntityRepository;
import dev.soffa.foundation.extras.journal.Journal;
import dev.soffa.foundation.extras.journal.JournalRepository;
import org.springframework.stereotype.Component;

@Component
public class JournalRepositoryImpl extends SimpleEntityRepository<Journal> implements JournalRepository {

    public JournalRepositoryImpl(DB db) {
        super(db, Journal.class);
    }

}
