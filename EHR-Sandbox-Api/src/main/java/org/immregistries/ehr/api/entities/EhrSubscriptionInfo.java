package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Used to store number of events related to a subscription, along with other information
 */
@Entity
@Table(name = "subscription_info")
public class EhrSubscriptionInfo {
    @Id
    @Column(name = "ehr_subscription")
    private String  subscriptionIdentifier;
    @OneToOne
    @MapsId
    @JoinColumn(name = "ehr_subscription")
    @JsonIgnore
    @JsonBackReference("subscription")
    private EhrSubscription ehrSubscription;

    @Column(name = "events_since_start")
    private int eventsSinceSubscriptionStart;

    public EhrSubscriptionInfo(EhrSubscription ehrSubscription) {
        this.subscriptionIdentifier = ehrSubscription.getIdentifier();
        this.ehrSubscription = ehrSubscription;
        ehrSubscription.setSubscriptionInfo(this);
    }

    public EhrSubscriptionInfo() {
    }

    public int getEventsSinceSubscriptionStart() {
        return eventsSinceSubscriptionStart;
    }

    public void setEventsSinceSubscriptionStart(int eventsSinceSubscriptionStart) {
        this.eventsSinceSubscriptionStart = eventsSinceSubscriptionStart;
    }

    public EhrSubscription getSubscriptionStore() {
        return ehrSubscription;
    }

    public void setSubscriptionStore(EhrSubscription ehrSubscription) {
        this.ehrSubscription = ehrSubscription;
    }

    public String getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    public void setSubscriptionIdentifier(String subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }
}
