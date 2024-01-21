package net.ixdarklord.ultimine_addition.common.data.item;

import net.ixdarklord.ultimine_addition.client.handler.ClientMinerCertificateHandler;
import net.ixdarklord.ultimine_addition.common.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.common.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.util.ChatFormattingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MinerCertificateData extends DataHandler<MinerCertificateData, ItemStack> {
    private ItemStack stack;
    @Nullable
    private Legacy legacy;
    private boolean isAccomplished;
    private boolean isCelebration;
    private boolean completeSound;

    public void tick() {
        if (this.legacy != null) {
            if (this.legacy.getRequiredAmount() == 0) {
                int min = ConfigHandler.COMMON.LEGACY_REQUIRED_AMOUNT_MIN.get();
                int max = ConfigHandler.COMMON.LEGACY_REQUIRED_AMOUNT_MAX.get();
                int value = RandomSource.create().nextIntBetweenInclusive(min, max);
                this.legacy.setRequiredAmount(value);
                saveData(stack);
            } else if (!this.isAccomplished && this.legacy.getMinedBlocks() == this.legacy.getRequiredAmount()) {
                completeSound(true).setAccomplished(true).saveData(stack);
            }
        } else if (!isAccomplished) {
            completeSound(true).setAccomplished(true).saveData(stack);
        }
    }

    @Override
    public ItemStack get() {
        return this.stack;
    }

    public @Nullable Legacy getLegacy() {
        return legacy;
    }

    public boolean isAccomplished() {
        return this.isAccomplished;
    }

    public MinerCertificateData setAccomplished(boolean state) {
        this.isAccomplished = state;
        return this;
    }

    public MinerCertificateData createCelebration(boolean state) {
        isCelebration = state;
        return this;
    }

    public MinerCertificateData completeSound(boolean state) {
        this.completeSound = state;
        return this;
    }

    @Override
    public void clientUpdate() {
        if (this.stack.getItem() instanceof MinerCertificateItem && this.completeSound) {
            ClientMinerCertificateHandler.playClientSound();
        }
        if (this.stack.getItem() instanceof MinerCertificateItem && this.isCelebration) {
            ClientMinerCertificateHandler.sendClientMessage();
            ClientMinerCertificateHandler.playAnimation(this.stack);
        }
    }

    @Override
    public void saveData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        if (this.legacy != null)
            NBT.put("legacy", this.legacy.save());
        NBT.putBoolean("is_accomplished", this.isAccomplished);
        stack.getOrCreateTag().put(this.NBTBase, NBT);
        super.saveData(stack);
    }

    @Override
    public MinerCertificateData loadData(ItemStack stack) {
        CompoundTag NBT = (CompoundTag) stack.getOrCreateTag().get(this.NBTBase);
        if (NBT == null) NBT = new CompoundTag();

        if (ConfigHandler.COMMON.PLAYSTYLE_MODE_SAFE.get() == PlaystyleMode.LEGACY)
            this.legacy = new Legacy().load(this, NBT.getCompound("legacy"));
        this.isAccomplished = NBT.getBoolean("is_accomplished");
        this.stack = stack;
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
        buf.writeBoolean(this.completeSound);
        buf.writeBoolean(this.isAccomplished);
        buf.writeBoolean(this.isCelebration);
    }

    public static MinerCertificateData fromNetwork(FriendlyByteBuf buf) {
        return new MinerCertificateData().loadData(buf.readItem())
                .completeSound(buf.readBoolean())
                .setAccomplished(buf.readBoolean())
                .createCelebration(buf.readBoolean());
    }

    public static class Legacy {
        private MinerCertificateData data;
        private int requiredAmount;
        private int minedBlocks;

        public int getRequiredAmount() {
            return requiredAmount;
        }

        public void setRequiredAmount(int requiredAmount) {
            this.requiredAmount = requiredAmount;
        }

        public int getMinedBlocks() {
            return minedBlocks;
        }

        public void addMiningPoint(int sum) {
            this.minedBlocks = Math.min(this.minedBlocks + sum, requiredAmount);
        }

        public void createInfoComponent(List<Component> tooltipComponents, boolean isShiftPressed) {
            if (!data.stack.hasTag()) {
                if (isShiftPressed)
                    tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.certificate.legacy.info"));
                return;
            }

            if (!isShiftPressed) {
                if (!data.isAccomplished)
                    tooltipComponents.add(1, createBrackets(Component.translatable("tooltip.ultimine_addition.certificate.legacy.sealed").withStyle(ChatFormatting.GRAY)));
                else
                    tooltipComponents.add(1, createBrackets(Component.translatable("tooltip.ultimine_addition.certificate.legacy.opened").withStyle(ChatFormatting.GOLD)));
            } else {
                if (!data.isAccomplished) {
                    ChatFormatting formatting = ChatFormattingUtils.get3ColorPercentageFormat(minedBlocks, requiredAmount);
                    Component component = Component.literal(String.valueOf(minedBlocks)).withStyle(formatting);
                    tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest.info", requiredAmount).withStyle(ChatFormatting.DARK_AQUA));
                    tooltipComponents.add(Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest", component).withStyle(ChatFormatting.GRAY)));
                } else tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest.congrats", minedBlocks).withStyle(ChatFormatting.GREEN));
            }
        }

        @SuppressWarnings("UnnecessaryUnicodeEscape")
        private Component createBrackets(Component component) {
            return Component.literal("『").withStyle(ChatFormatting.DARK_GRAY).append(component).append(Component.literal("\u300F").withStyle(ChatFormatting.DARK_GRAY));
        }

        public CompoundTag save() {
            CompoundTag NBT = new CompoundTag();
            NBT.putInt("required_amount", requiredAmount);
            NBT.putInt("mined_blocks", minedBlocks);
            return NBT;
        }

        public Legacy load(MinerCertificateData data, CompoundTag NBT) {
            this.data = data;
            this.requiredAmount = NBT.getInt("required_amount");
            this.minedBlocks = NBT.getInt("mined_blocks");
            return this;
        }
    }
}
