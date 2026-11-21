package com.aiotter;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.text.Normalizer;
import java.util.*;

public class AiOtterEntity extends TamableAnimal {
    private boolean staying = false;
    private int talkCooldown = 0;
    private int aiCooldown = 0;
    private boolean nightAnnounced = false;
    private final List<String[]> memory = new ArrayList<>();

    public AiOtterEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    // ---------------------------------------------------------------- IA de movimiento
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(3, new FollowOwnerSimpleGoal(1.15, 5.0f, 2.5f));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9) {
            @Override public boolean canUse() { return !staying && !isTame() && super.canUse(); }
        });
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    /** Nunca atacar a su dueño (por ejemplo si lo golpeas sin querer). */
    @Override
    public boolean canAttack(LivingEntity target) {
        return !isOwnedBy(target) && super.canAttack(target);
    }

    private class FollowOwnerSimpleGoal extends Goal {
        private final double speed; private final float start; private final float stopDist;
        private int recalc;
        FollowOwnerSimpleGoal(double speed, float start, float stopDist) {
            this.speed = speed; this.start = start; this.stopDist = stopDist;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        @Override public boolean canUse() {
            LivingEntity o = getOwner();
            return isTame() && !staying && o != null && !o.isSpectator()
                    && distanceToSqr(o) > start * start;
        }
        @Override public boolean canContinueToUse() {
            LivingEntity o = getOwner();
            return o != null && !staying && !getNavigation().isDone() && distanceToSqr(o) > stopDist * stopDist;
        }
        @Override public void start() { recalc = 0; }
        @Override public void stop() { getNavigation().stop(); }
        @Override public void tick() {
            LivingEntity o = getOwner();
            if (o == null) return;
            getLookControl().setLookAt(o, 10.0f, (float) getMaxHeadXRot());
            if (--recalc > 0) return;
            recalc = 10;
            if (distanceToSqr(o) >= 144.0) {               // más de 12 bloques: teletransporte
                moveTo(o.getX() + 1.0, o.getY(), o.getZ() + 1.0, getYRot(), getXRot());
                getNavigation().stop();
            } else {
                getNavigation().moveTo(o, speed);
            }
        }
    }

    // ---------------------------------------------------------------- interacción
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean fish = stack.is(Items.COD) || stack.is(Items.SALMON);
        boolean client = level().isClientSide;

        if (!isTame() && fish) {
            if (!client) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                tame(player);
                getNavigation().stop();
                setHealth(getMaxHealth());
                if (player instanceof ServerPlayer sp) say(sp, Lines.pick(Lines.TAMED));
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (isTame() && isOwnedBy(player)) {
            if (fish && getHealth() < getMaxHealth()) {
                if (!client) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    heal(6.0f);
                    if (player instanceof ServerPlayer sp) say(sp, Lines.pick(Lines.THANKS_FISH));
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (hand == InteractionHand.MAIN_HAND && stack.isEmpty()) {
                if (!client && player instanceof ServerPlayer sp) {
                    if (player.isShiftKeyDown()) {
                        staying = !staying;
                        getNavigation().stop();
                        say(sp, Lines.pick(staying ? Lines.STAY : Lines.FOLLOW));
                    } else {
                        say(sp, Lines.pick(Lines.GREET));
                    }
                }
                return InteractionResult.sidedSuccess(client);
            }
        }
        return super.mobInteract(player, hand);
    }

    // ---------------------------------------------------------------- hablar
    public static String normalize(String s) {
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "");
    }

    public void say(ServerPlayer owner, String msg) {
        talkCooldown = 300;
        Component text = Component.literal("[Nutria IA] ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(msg).withStyle(ChatFormatting.WHITE));
        owner.sendSystemMessage(text);
        level().playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(),
                SoundSource.NEUTRAL, 0.8f, 1.6f);
    }

    public void handleChat(ServerPlayer player, String raw) {
        List<String> words = Arrays.asList(normalize(raw).split("[^a-z]+"));
        if (words.contains("quedate") || words.contains("stay") || words.contains("espera") || words.contains("quieta")) {
            staying = true; getNavigation().stop();
            say(player, Lines.pick(Lines.STAY)); return;
        }
        if (words.contains("sigueme") || words.contains("follow") || words.contains("ven") || words.contains("vamos")) {
            staying = false;
            say(player, Lines.pick(Lines.FOLLOW)); return;
        }

        if (aiCooldown > 0) return;                       // anti-spam
        aiCooldown = AiOtterConfig.get().aiCooldownSeconds * 20;

        String cleaned = raw.length() > 300 ? raw.substring(0, 300) : raw;
        AiBrain.ask(player.getServer(), cleaned, buildContext(player), new ArrayList<>(memory),
                reply -> {
                    memory.add(new String[]{cleaned, reply});
                    while (memory.size() > 6) memory.remove(0);
                    say(player, reply);
                },
                () -> say(player, Lines.pick(Lines.OFFLINE)));
    }

    private String buildContext(ServerPlayer p) {
        long t = level().getDayTime() % 24000L;
        boolean night = t >= 13000 && t < 23000;
        String biome = level().getBiome(blockPosition()).unwrapKey()
                .map(k -> k.location().getPath()).orElse("desconocido");
        int hostiles = level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(12), e -> true).size();
        return "jugador: " + p.getName().getString()
                + "; vida del jugador: " + Math.round(p.getHealth()) + "/20"
                + "; hambre: " + p.getFoodData().getFoodLevel() + "/20"
                + "; tu vida: " + Math.round(getHealth()) + "/" + Math.round(getMaxHealth())
                + "; " + (night ? "es de noche" : "es de día")
                + "; bioma: " + biome
                + "; " + (level().isRaining() ? "está lloviendo" : "cielo despejado")
                + "; monstruos cerca: " + hostiles
                + "; " + (staying ? "estás en modo guardia (quieta)" : "estás siguiendo al jugador");
    }

    // ---------------------------------------------------------------- comentarios espontáneos
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (aiCooldown > 0) aiCooldown--;
        if (talkCooldown > 0) { talkCooldown--; return; }
        if (!isTame() || tickCount % 20 != 0) return;
        if (!(getOwner() instanceof ServerPlayer p)) return;

        if (p.getHealth() <= 6.0f) { say(p, Lines.pick(Lines.LOW_HEALTH)); return; }
        if (!level().getEntitiesOfClass(Creeper.class, getBoundingBox().inflate(9), e -> true).isEmpty()) {
            say(p, Lines.pick(Lines.CREEPER)); return;
        }
        long t = level().getDayTime() % 24000L;
        boolean night = t >= 13000 && t < 23000;
        if (night && !nightAnnounced) { nightAnnounced = true; say(p, Lines.pick(Lines.NIGHT)); return; }
        if (!night) nightAnnounced = false;
        if (p.getFoodData().getFoodLevel() <= 6 && random.nextInt(6) == 0) { say(p, Lines.pick(Lines.HUNGRY)); return; }
        if (p.isInWater() && random.nextInt(8) == 0) { say(p, Lines.pick(Lines.WATER)); return; }
        if (random.nextInt(60) == 0) say(p, Lines.pick(Lines.IDLE));
    }

    // ---------------------------------------------------------------- guardado
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Staying", staying);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        staying = tag.getBoolean("Staying");
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }
}
