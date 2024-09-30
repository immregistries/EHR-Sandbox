package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.*;

import java.util.Date;

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

    public EhrSubscription(Subscription subscription) {
        externalId = new IdType(subscription.getId()).getIdPart();
        identifier = subscription.getIdentifierFirstRep().getValue();
        name = subscription.getName();
        status = subscription.hasStatus() ? subscription.getStatus().toCode() : null;
        topic = subscription.getTopic();
        end = subscription.getEnd();
        reason = subscription.getReason();
        channelType = subscription.hasChannelType() ? subscription.getChannelType().getCode() : null;
        header = subscription.hasParameter() ? subscription.getParameter().get(0).getName() + ":" + subscription.getParameter().get(0).getValue() : "";
        heartbeatPeriod = subscription.getHeartbeatPeriod();
        timeout = subscription.getTimeout();
        contentType = subscription.getContentType();
        content = subscription.hasContent() ? subscription.getContent().toCode() : null;
        notificationUrlLocation = subscription.getEndpoint();
        maxCount = subscription.getMaxCount();
    }

    public Subscription toSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(externalId);
        subscription.addIdentifier(new Identifier().setValue(identifier));
        subscription.setName(name);
        subscription.setStatus(Enumerations.SubscriptionStatusCodes.valueOf(status));
        subscription.setTopic(topic);
        subscription.setEnd(end);
        subscription.setReason(reason);
        subscription.setChannelType(new Coding().setCode(channelType));
        if (StringUtils.isNotBlank(header)) {
            subscription.addParameter().setName(header.split(":")[0]).setValue(header.split(":")[1]);
        }
        subscription.setHeartbeatPeriod(heartbeatPeriod);
        subscription.setTimeout(timeout);
        subscription.setContent(Subscription.SubscriptionPayloadContent.valueOf(content));
        subscription.setContentType(contentType);
        subscription.setEndpoint(notificationUrlLocation);
        subscription.setMaxCount(maxCount);
        return subscription;
    }

    @Id
    @Column(name = "identifier", nullable = false, length = 45)
    private String identifier;

    @Column(name = "external_id", nullable = false, length = 45)
    private String externalId = "";

    @Column(name = "name", length = 45)
    private String name;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "topic", nullable = false, length = 90)
    private String topic;

    @Column(name = "end", length = 45)
    private Date end;

    @Column(name = "reason", length = 90)
    private String reason;

    @Column(name = "channelType", length = 45)
    private String channelType;

    @Column(name = "header", length = 612)
    private String header;

    @Column(name = "heartbeatPeriod")
    private Integer heartbeatPeriod;

    @Column(name = "timeout", length = 45)
    private Integer timeout;

    @Column(name = "contentType", length = 45)
    private String contentType;

    @Column(name = "content", length = 45)
    private String content;

    @Column(name = "notificationUrlLocation", length = 45)
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

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
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
}