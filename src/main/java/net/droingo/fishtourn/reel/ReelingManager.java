package net.droingo.fishtourn.reel;

import net.droingo.fishtourn.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
            return;
        }

        SESSIONS.put(player.getUuid(), new ReelingSession(
                player.getUuid(),
                bobber.getId(),
                player.age,
                bobber.getPos()
        ));
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
            return;
        }

        float clampedAmount = Math.max(0.0F, Math.min(amount, 3.0F));

        if (clampedAmount <= 0.0F) {
            return;
        }

        session.addProgress(clampedAmount, player.age);
        moveBobberByProgress(player, player.fishHook, session);

        player.sendMessage(
                Text.literal("Reeling: " + Math.round(session.progress()) + "%")
                        .formatted(session.isComplete() ? Formatting.GREEN : Formatting.AQUA),
                true
        );
    }

    public static void completeReel(ServerPlayerEntity player, int bobberId) {
        if (player.fishHook == null || player.fishHook.getId() != bobberId) {
            stopSession(player.getUuid());
            return;
        }

        ReelingSession session = SESSIONS.get(player.getUuid());

        if (session == null || !session.isComplete()) {
            player.sendMessage(
                    Text.literal("You have not reeled enough yet!")
                            .formatted(Formatting.RED),
                    true
            );
            return;
        }

        ItemStack rodStack = getTournamentRodStack(player);

        if (rodStack.isEmpty()) {
            stopSession(player.getUuid());
            return;
        }

        moveBobberByProgress(player, player.fishHook, session);

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

            if (!session.isComplete() && player.age - session.lastInputAge() > 20) {
                session.decay();
            }

            moveBobberByProgress(player, player.fishHook, session);
        }
    }

    private static void moveBobberByProgress(ServerPlayerEntity player, FishingBobberEntity bobber, ReelingSession session) {
        Vec3d playerTarget = player.getPos().add(0.0D, 0.15D, 0.0D);
        double progress = Math.max(0.0D, Math.min(1.0D, session.progress() / 100.0D));

        Vec3d newPosition = session.startPosition().lerp(playerTarget, progress);

        bobber.refreshPositionAndAngles(
                newPosition.x,
                newPosition.y,
                newPosition.z,
                bobber.getYaw(),
                bobber.getPitch()
        );

        bobber.setVelocity(Vec3d.ZERO);
        bobber.velocityModified = true;

        syncEntityPosition(player, bobber);
    }

    private static void syncEntityPosition(ServerPlayerEntity player, Entity entity) {
        EntityPositionS2CPacket packet = new EntityPositionS2CPacket(entity);

        player.networkHandler.sendPacket(packet);

        for (ServerPlayerEntity otherPlayer : player.getServerWorld().getPlayers()) {
            if (otherPlayer != player && otherPlayer.squaredDistanceTo(entity) < 4096.0D) {
                otherPlayer.networkHandler.sendPacket(packet);
            }
        }
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