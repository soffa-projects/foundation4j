package com.company.app.gateways;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.soffa.foundation.data.spring.jpa.MapConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;


@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    @EmbeddedId
    private MessageId id;
    private String requestId;
    private MessageStatus status;
    @Transient
    private String value;
    private String report;
    private long counter;
    @Convert(converter = MapConverter.class)
    @JsonIgnore
    private Map<String, Object> metadata;

    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private PaymentOptions paymentOptions;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

}
