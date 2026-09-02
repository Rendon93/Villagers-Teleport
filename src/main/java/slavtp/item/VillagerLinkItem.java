package slavtp.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class VillagerLinkItem extends Item{
    public VillagerLinkItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient() && entity instanceof VillagerEntity villager) {
            NbtCompound nbt = stack.getOrCreateNbt();

            nbt.putUuid("TargetVillager", villager.getUuid());
            nbt.putDouble("TargetX", villager.getX());
            nbt.putDouble("TargetY", villager.getY());
            nbt.putDouble("TargetZ", villager.getZ());
            nbt.putString("TargetDimension", villager.getWorld().getRegistryKey().getValue().toString());

            user.sendMessage(Text.literal("§a[SlaveTP] ¡Aldeano enlazado con éxito!"), true);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
