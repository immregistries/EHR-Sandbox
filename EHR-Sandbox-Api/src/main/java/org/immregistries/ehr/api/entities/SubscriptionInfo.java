package org.immregistries.ehr.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Used to store number of events related to a subscription, along with other information
 */
@Entity
@Table(name = "subscription_info")
public class SubscriptionInfo {
    @Id
    @Column(name = "subscription_store")
    private String  subscriptionIdentifier;
    @OneToOne
    @MapsId
    @JoinColumn(name = "subscription_store")
    @JsonIgnore
    private SubscriptionStore subscriptionStore;

    @Column(name = "events_since_start")
    private int eventsSinceSubscriptionStart;

    public SubscriptionInfo(SubscriptionStore subscriptionStore) {
        this.subscriptionIdentifier = subscriptionStore.getIdentifier();
        this.subscriptionStore = subscriptionStore;
        subscriptionStore.setSubscriptionInfo(this);
    }

    public SubscriptionInfo() {
    }

    public int getEventsSinceSubscriptionStart() {
        return eventsSinceSubscriptionStart;
    }

    public void setEventsSinceSubscriptionStart(int eventsSinceSubscriptionStart) {
        this.eventsSinceSubscriptionStart = eventsSinceSubscriptionStart;
    }

    public SubscriptionStore getSubscriptionStore() {
        return subscriptionStore;
    }

    public void setSubscriptionStore(SubscriptionStore subscriptionStore) {
        this.subscriptionStore = subscriptionStore;
    }

    public String getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    public void setSubscriptionIdentifier(String subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }
}
