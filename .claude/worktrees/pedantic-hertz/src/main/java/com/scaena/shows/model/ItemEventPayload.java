package com.scaena.shows.model;

/**
 * NOTE: record component cannot be named "notify" because it would generate an accessor
 * named notify(), which clashes with Object#notify().
 */
public record ItemEventPayload(ItemPayload item, ItemDelivery delivery, ItemNotifyPayload notifyPayload) {}
