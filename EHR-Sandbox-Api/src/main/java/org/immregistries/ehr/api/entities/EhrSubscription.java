package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.Subscription;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * based on Fhir Subscription, made to register them in the database
 * TODO materialize cardinality properly
 */
@Entity
@Table(name = "ehr_subscription")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "identifier",
        scope = EhrSubscription.class)
public class EhrSubscription extends EhrEntity {
    public EhrSubscription() {
    }

    public EhrSubscription(org.hl7.fhir.r5.model.Subscription subscription) {
        externalId = new org.hl7.fhir.r5.model.IdType(subscription.getId()).getIdPart();
        identifier = subscription.getIdentifierFirstRep().getValue();
        name = subscription.getName();
        status = subscription.hasStatus() ? subscription.getStatus().toCode() : null;
        topic = subscription.getTopic();
        end = subscription.getEnd();
        reason = subscription.getReason();
        channelType = subscription.hasChannelType() ? subscription.getChannelType().getCode() : null;
        headers = subscription.getParameter().stream()
                .collect(Collectors.toMap(
                        Subscription.SubscriptionParameterComponent::getName,
                        Subscription.SubscriptionParameterComponent::getValue)
                );
        heartbeatPeriod = subscription.getHeartbeatPeriod();
        timeout = subscription.getTimeout();
        contentType = subscription.getContentType();
        content = subscription.hasContent() ? subscription.getContent().toCode() : null;
        notificationUrlLocation = subscription.getEndpoint();
        maxCount = subscription.getMaxCount();
    }

    public EhrSubscription(org.hl7.fhir.r4b.model.Subscription subscription) {
        externalId = new org.hl7.fhir.r4b.model.IdType(subscription.getId()).getIdPart();
//        identifier = subscription.getIdentifierFirstRep().getValue();
//        name = subscription.getName();
        status = subscription.hasStatus() ? subscription.getStatus().toCode() : null;
//        topic = subscription.getTopic();
        end = subscription.getEnd();
        reason = subscription.getReason();
        if (subscription.hasChannel()) {
            channelType = subscription.getChannel().hasType() ? subscription.getChannel().getType().toCode() : null;
            notificationUrlLocation = subscription.getChannel().getEndpoint();
            headers = subscription.getChannel().getHeader().stream()
                    .collect(Collectors.toMap(
                            (header)-> header.getValue().split(":")[0],
                            (header)-> header.getValue().split(":")[1])
                    );
        }
//        heartbeatPeriod = subscription.getHeartbeatPeriod();
//        timeout = subscription.getTimeout();
//        contentType = subscription.getContentType();
//        content = subscription.hasContent() ? subscription.getContent().toCode() : null;
//        maxCount = subscription.getMaxCount();
    }

    public org.hl7.fhir.r5.model.Subscription toSubscription() {
        org.hl7.fhir.r5.model.Subscription subscription = new org.hl7.fhir.r5.model.Subscription();
        subscription.setId(externalId);
        subscription.addIdentifier(new org.hl7.fhir.r5.model.Identifier().setValue(identifier));
        subscription.setName(name);
        subscription.setStatus(org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes.valueOf(status));
        subscription.setTopic(topic);
        subscription.setEnd(end);
        subscription.setReason(reason);
        subscription.setChannelType(new org.hl7.fhir.r5.model.Coding().setCode(channelType));
        for (Map.Entry<String,String> entry : headers.entrySet()) {
            subscription.addParameter().setName(entry.getKey()).setValue(entry.getValue());
        }
        subscription.setHeartbeatPeriod(heartbeatPeriod);
        subscription.setTimeout(timeout);
        subscription.setContent(org.hl7.fhir.r5.model.Subscription.SubscriptionPayloadContent.valueOf(content));
        subscription.setContentType(contentType);
        subscription.setEndpoint(notificationUrlLocation);
        subscription.setMaxCount(maxCount);
        return subscription;
    }

    @Id
    @Column(name = "subscription_identifier", nullable = false, length = 45)
    private String identifier;

    @Column(name = "external_id", nullable = false, length = 45)
    private String externalId = "";

    @Column(name = "subscription_name", length = 45)
    private String name;

    @Column(name = "subscription_status", nullable = false, length = 45)
    private String status;

    @Column(name = "subscription_topic", nullable = false, length = 90)
    private String topic;

    @Column(name = "subscription_end", length = 45)
    private Date end;

    @Column(name = "subscription_reason", length = 90)
    private String reason;

    @Column(name = "subscription_channel_type", length = 45)
    private String channelType;

    @ElementCollection
    @MapKeyColumn(name = "header_name")
    @Column(name = "header_value")
    private Map<String,String> headers = new HashMap<>(2);

    @Column(name = "subscription_heartbeat_period")
    private Integer heartbeatPeriod;

    @Column(name = "subscription_timeout", length = 45)
    private Integer timeout;

    @Column(name = "subscription_content_type", length = 45)
    private String contentType;

    @Column(name = "subscription_content", length = 45)
    private String content;

    @Column(name = "subscription_notification_url_location", length = 45)
    private String notificationUrlLocation;

    @Column(name = "maxCount")
    private Integer maxCount;

    @OneToOne(mappedBy = "ehrSubscription", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private EhrSubscriptionInfo subscriptionInfo;

    @ManyToOne
    @JoinColumn(name = "immunization_registry_id")
    private ImmunizationRegistry immunizationRegistry;

    public ImmunizationRegistry getImmunizationRegistry() {
        return immunizationRegistry;
    }

    public void setImmunizationRegistry(ImmunizationRegistry immunizationRegistry) {
        this.immunizationRegistry = immunizationRegistry;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public String getNotificationUrlLocation() {
        return notificationUrlLocation;
    }

    public void setNotificationUrlLocation(String notificationUrlLocation) {
        this.notificationUrlLocation = notificationUrlLocation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    public void setHeartbeatPeriod(Integer heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public EhrSubscriptionInfo getSubscriptionInfo() {
        return subscriptionInfo;
    }

    public void setSubscriptionInfo(EhrSubscriptionInfo subscriptionInfo) {
        this.subscriptionInfo = subscriptionInfo;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}