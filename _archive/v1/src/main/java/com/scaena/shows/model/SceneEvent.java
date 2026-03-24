package com.scaena.shows.model;

public sealed interface SceneEvent permits SceneEvent.SoundEvent, SceneEvent.ParticleEvent,
        SceneEvent.MessageEvent, SceneEvent.TitleEvent, SceneEvent.BossbarEvent,
        SceneEvent.EffectEvent, SceneEvent.SequenceEvent, SceneEvent.ItemEvent {

    int atTick();

    record SoundEvent(int atTick, SoundPayload sound) implements SceneEvent {}
    record ParticleEvent(int atTick, ParticlePayload particle) implements SceneEvent {}
    record MessageEvent(int atTick, MessagePayload message) implements SceneEvent {}
    record TitleEvent(int atTick, TitlePayload title) implements SceneEvent {}
    record BossbarEvent(int atTick, BossbarPayload bossbar) implements SceneEvent {}
    record EffectEvent(int atTick, EffectPayload effect) implements SceneEvent {}
    record SequenceEvent(int atTick, SequenceRefPayload sequence) implements SceneEvent {}
    record ItemEvent(int atTick, ItemEventPayload item) implements SceneEvent {}
}
