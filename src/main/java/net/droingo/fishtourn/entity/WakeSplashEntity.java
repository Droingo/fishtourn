package net.droingo.fishtourn.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class WakeSplashEntity extends Entity {
    private int ageTicks = 0;

    public WakeSplashEntity(EntityType<? extends WakeSplashEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
        this.setInvisible(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        ageTicks++;

        if (ageTicks == 1) {
            this.setVelocity(0.0D, -0.18D, 0.0D);
        }

        this.setVelocity(this.getVelocity().add(0.0D, -0.025D, 0.0D));
        this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());

        if (this.isTouchingWater() || ageTicks > 30) {
            this.discard();
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.ageTicks = nbt.getInt("AgeTicks");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("AgeTicks", this.ageTicks);
    }
}