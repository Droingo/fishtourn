package net.droingo.fishtourn.reel;

import net.droingo.fishtourn.fish.TournamentBobberAccess;
import net.droingo.fishtourn.item.ModItems;
import net.droingo.fishtourn.network.ReelFailPayload;
import net.droingo.fishtourn.network.ReelSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ReelingManager {
    private static final Map<UUID, ReelingSession> SESSIONS = new HashMap<>();

    private ReelingManager() {
    }

    public static void startSession(ServerPlayerEntity player, FishingBobberEntity bobber) {
        if (!isHoldingTournamentRod(player)) {
            return;
        }

        ReelingSession existing = SESSIONS.get(player.getUuid());

        if (existing != null && existing.bobberId() == bobber.getId()) {
            sendSync(player, existing);
            return;
        }

        ReelDifficulty difficulty = ReelDifficulty.roll(player.getRandom());

        ReelingSession session = new ReelingSession(
                player.getUuid(),
                bobber.getId(),
                player.age,
                bobber.getPos(),
                difficulty
        );

        SESSIONS.put(player.getUuid(), session);
        sendSync(player, session);
    }

    public static void stopSession(UUID playerUuid) {
        SESSIONS.remove(playerUuid);
    }

    public static void receiveInput(ServerPlayerEntity player, float amount, boolean clockwise) {
        if (!isHoldingTournamentRod(player)) {
            return;
        }

        if (player.fishHook == null) {
            stopSession(player.getUuid());
            return;
        }

        ReelingSession session = SESSIONS.get(player.getUuid());

        if (session == null) {
            return;
        }

        if (session.bobberId() != player.fishHook.getId()) {
            stopSession(player.getUuid());
            return;
        }

        if (session.isComplete()) {
            sendSync(player, session);
            return;
        }

        float clampedAmount = Math.max(0.0F, Math.min(amount, 3.0F));

        if (clampedAmount <= 0.0F) {
            return;
        }

        session.addReelInput(clampedAmount, player.age);
        moveBobberByProgress(player, player.fishHook, session);
        sendSync(player, session);
    }

    public static void completeReel(ServerPlayerEntity player, int bobberId) {
        if (player.fishHook == null || player.fishHook.getId() != bobberId) {
            stopSession(player.getUuid());
            return;
        }

        ReelingSession session = SESSIONS.get(player.getUuid());

        if (session == null || !session.isComplete()) {
            if (session != null) {
                sendSync(player, session);
            }

            player.sendMessage(
                    Text.literal("Keep reeling!")
                            .formatted(Formatting.YELLOW),
                    true
            );
            return;
        }

        ItemStack rodStack = getTournamentRodStack(player);

        if (rodStack.isEmpty()) {
            stopSession(player.getUuid());
            return;
        }

        Vec3d finalPosition = player.getPos().add(0.0D, 0.15D, 0.0D);

        player.fishHook.refreshPositionAndAngles(
                finalPosition.x,
                finalPosition.y,
                finalPosition.z,
                player.fishHook.getYaw(),
                player.fishHook.getPitch()
        );

        player.fishHook.setVelocity(Vec3d.ZERO);
        player.fishHook.velocityModified = true;

        if (player.fishHook instanceof TournamentBobberAccess bobberAccess) {
            bobberAccess.fishtourn$keepFishHooked();
        }

        player.sendMessage(
                Text.literal("Reel complete!")
                        .formatted(Formatting.GREEN, Formatting.BOLD),
                true
        );

        player.fishHook.use(rodStack);
        stopSession(player.getUuid());
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ReelingSession>> iterator = SESSIONS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ReelingSession> entry = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());

            if (player == null || !isHoldingTournamentRod(player) || player.fishHook == null) {
                iterator.remove();
                continue;
            }

            ReelingSession session = entry.getValue();

            if (session.bobberId() != player.fishHook.getId()) {
                iterator.remove();
                continue;
            }

            if (player.fishHook instanceof TournamentBobberAccess bobberAccess) {
                bobberAccess.fishtourn$keepFishHooked();
            }

            session.tickTension(player.age);

            if (session.hasLineSnapped()) {
                snapLine(player);
                iterator.remove();
                continue;
            }

            moveBobberByProgress(player, player.fishHook, session);
            sendSync(player, session);
        }
    }

    private static void moveBobberByProgress(ServerPlayerEntity player, FishingBobberEntity bobber, ReelingSession session) {
        Vec3d playerTarget = player.getPos().add(0.0D, 0.15D, 0.0D);

        if (session.isComplete()) {
            bobber.refreshPositionAndAngles(
                    playerTarget.x,
                    playerTarget.y,
                    playerTarget.z,
                    bobber.getYaw(),
                    bobber.getPitch()
            );
            bobber.setVelocity(Vec3d.ZERO);
            bobber.velocityModified = true;
            return;
        }

        double progress = Math.max(0.0D, Math.min(1.0D, session.progress() / 100.0D));

        double slackPullback = 0.0D;
        if (session.tension() < 35.0F) {
            slackPullback = (35.0D - session.tension()) / 35.0D * session.difficulty().slackPullbackMax();
        }

        double visualProgress = Math.max(0.0D, Math.min(1.0D, progress - slackPullback));

        Vec3d targetPosition = session.startPosition().lerp(playerTarget, visualProgress);
        Vec3d toTarget = targetPosition.subtract(bobber.getPos());

        double distanceSquared = toTarget.lengthSquared();

        if (distanceSquared < 0.0025D) {
            bobber.setVelocity(Vec3d.ZERO);
            bobber.velocityModified = true;
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        double speed = Math.min(0.55D, Math.max(0.06D, distance * 0.65D));

        bobber.setVelocity(toTarget.normalize().multiply(speed));
        bobber.velocityModified = true;
    }

    private static void snapLine(ServerPlayerEntity player) {
        if (player.fishHook != null) {
            player.fishHook.discard();
            player.fishHook = null;
        }

        player.getWorld().playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_ITEM_BREAK,
                SoundCategory.PLAYERS,
                0.8F,
                1.25F
        );

        player.sendMessage(
                Text.literal("Line snapped! Fish lost.")
                        .formatted(Formatting.RED, Formatting.BOLD),
                true
        );

        ServerPlayNetworking.send(player, new ReelFailPayload());
    }

    private static void sendSync(ServerPlayerEntity player, ReelingSession session) {
        ServerPlayNetworking.send(
                player,
                new ReelSyncPayload(
                        session.bobberId(),
                        session.progress(),
                        session.tension(),
                        session.difficulty().displayName()
                )
        );
    }

    private static boolean isHoldingTournamentRod(ServerPlayerEntity player) {
        return player.getMainHandStack().isOf(ModItems.TOURNAMENT_ROD)
                || player.getOffHandStack().isOf(ModItems.TOURNAMENT_ROD);
    }

    private static ItemStack getTournamentRodStack(ServerPlayerEntity player) {
        if (player.getMainHandStack().isOf(ModItems.TOURNAMENT_ROD)) {
            return player.getMainHandStack();
        }

        if (player.getOffHandStack().isOf(ModItems.TOURNAMENT_ROD)) {
            return player.getOffHandStack();
        }

        return ItemStack.EMPTY;
    }
}