package com.aiotter;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod(AiOtterMod.MOD_ID)
public class AiOtterMod {
    public static final String MOD_ID = "aiotter";

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<EntityType<AiOtterEntity>> AI_OTTER = ENTITIES.register("ai_otter",
            () -> EntityType.Builder.of(AiOtterEntity::new, MobCategory.CREATURE)
                    .sized(0.7f, 0.6f)
                    .build("ai_otter"));

    public static final RegistryObject<Item> AI_OTTER_SPAWN_EGG = ITEMS.register("ai_otter_spawn_egg",
            () -> new ForgeSpawnEggItem(AI_OTTER, 0x35B6E8, 0x6E4A30, new Item.Properties()));

    public AiOtterMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus);
        ITEMS.register(bus);
        bus.addListener(this::onAttributes);
        bus.addListener(this::onCreativeTab);
        MinecraftForge.EVENT_BUS.addListener(this::onChat);
        AiOtterConfig.load();
    }

    private void onAttributes(EntityAttributeCreationEvent e) {
        e.put(AI_OTTER.get(), AiOtterEntity.createAttributes().build());
    }

    private void onCreativeTab(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.SPAWN_EGGS) e.accept(AI_OTTER_SPAWN_EGG);
    }

    /** El dueño escribe llamando a la nutria: "nutria, ..." / "otter" / "bip". */
    private void onChat(ServerChatEvent event) {
        var player = event.getPlayer();
        String raw = event.getRawText();
        String n = AiOtterEntity.normalize(raw);
        if (!(n.contains("nutria") || n.contains("otter") || n.contains("bip"))) return;

        player.getServer().execute(() -> {
            List<AiOtterEntity> otters = player.level().getEntitiesOfClass(
                    AiOtterEntity.class,
                    player.getBoundingBox().inflate(32),
                    e -> e.isTame() && e.isOwnedBy(player));
            if (!otters.isEmpty()) otters.get(0).handleChat(player, raw);
        });
    }
}
