package dev.soffa.foundation.events;

import dev.soffa.foundation.core.EventHandler;
import dev.soffa.foundation.model.ServiceId;

public interface OnServiceStarted extends EventHandler<ServiceId, Void> {
}
