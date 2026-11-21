package me.s0nd3r.watermc.engine.common.entity;

public interface EquipmentInfo {

    boolean lithium$shouldTickEnchantments();

    boolean lithium$hasUnsentEquipmentChanges();

    void lithium$onEquipmentChangesSent();
}
