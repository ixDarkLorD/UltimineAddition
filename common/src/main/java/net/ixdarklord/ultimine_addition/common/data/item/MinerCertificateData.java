package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.coolcatlib.api.network.v1.codec.ByteBufCodecs;
import net.ixdarklord.coolcatlib.api.network.v1.codec.StreamCodec;
import net.ixdarklord.coolcatlib.api.utils.ChatFormattingUtils;
import net.ixdarklord.coolcatlib.api.utils.CodecUtils;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.FTBUltimineAddition;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PacketHandler;
import net.ixdarklord.ultimine_addition.network.packets.MinerCertificatePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MinerCertificateData extends ItemDataComponent<MinerCertificateData> {
    public static final ResourceLocation DATA_ID = FTBUltimineAddition.id("miner_certificate_data");
    public static final Codec<MinerCertificateData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.BOOL.fieldOf("IsAccomplished").forGetter(MinerCertificateData::isAccomplished),
                    MinerCertificateData.Legacy.CODEC.optionalFieldOf("Legacy").forGetter(MinerCertificateData::getLegacy))
            .apply(instance, (isAccomplished, legacyOpt) ->
                    new MinerCertificateData(isAccomplished, legacyOpt.orElse(null))));

    public static final StreamCodec<ByteBuf, MinerCertificateData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MinerCertificateData::isAccomplished,
            ByteBufCodecs.optional(MinerCertificateData.Legacy.STREAM_CODEC), MinerCertificateData::getLegacy,
            ByteBufCodecs.BOOL, (data) -> data.completeSound,
            ByteBufCodecs.BOOL, (data) -> data.isCelebration,
            (isAccomplished, legacyOpt, completeSound, isCelebration) -> (
                    new MinerCertificateData(isAccomplished, legacyOpt.orElse(null)))
                    .completeSound(completeSound)
                    .playCelebration(isCelebration)
    );

    private boolean isAccomplished;
    private @Nullable Legacy legacy;
    private boolean completeSound;
    private boolean isCelebration;

    private MinerCertificateData(boolean isAccomplished, @Nullable Legacy legacy) {
        super(DATA_ID, CODEC);
        this.isAccomplished = isAccomplished;
        this.legacy = legacy;
        this.getLegacy().ifPresent((l) -> l.data = this);
    }

    public static MinerCertificateData create() {
        return new MinerCertificateData(false, null);
    }

    public static MinerCertificateData load(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
        MinerCertificateData data = tag.isEmpty() ? create() : CodecUtils.decode(CODEC, tag);
        return data.setStack(stack);
    }

    public static boolean hasData(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof MinerCertificateItem) {
            CompoundTag tag = stack.getOrCreateTag().getCompound(DATA_ID.toString());
            return !tag.isEmpty();
        } else {
            return false;
        }
    }

    public void tick(int slotIndex, ServerPlayer player) {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY) {
            if (this.legacy == null) {
                int min = ConfigHandler.SERVER.LEGACY_REQUIRED_AMOUNT.getMin();
                int max = ConfigHandler.SERVER.LEGACY_REQUIRED_AMOUNT.getMax();
                this.legacy = new Legacy(RandomSource.create().nextIntBetweenInclusive(min, max));
                this.save();
            }
        } else if (this.legacy != null) {
            this.legacy = null;
            this.save();
        }

        if (this.legacy != null) {
            if (!this.isAccomplished && this.legacy.getMinedBlocks() == this.legacy.getRequiredAmount()) {
                this.completeSound(true).setAccomplished(true).sendToClient(slotIndex, player).save();
            }
        } else if (!this.isAccomplished) {
            this.completeSound(true).setAccomplished(true).sendToClient(slotIndex, player).save();
        }

    }

    public MinerCertificateData setAccomplished(boolean state) {
        this.isAccomplished = state;
        return this;
    }

    public MinerCertificateData playCelebration(boolean state) {
        this.isCelebration = state;
        return this;
    }

    public MinerCertificateData completeSound(boolean state) {
        this.completeSound = state;
        return this;
    }

    public Optional<Legacy> getLegacy() {
        return Optional.ofNullable(this.legacy);
    }

    public boolean isAccomplished() {
        return this.isAccomplished;
    }

    public MinerCertificateData sendToClient(int slotIndex, ServerPlayer player) {
        PacketHandler.sendToPlayer(new MinerCertificatePacket(slotIndex, this.getStack(), this), player);
        return this;
    }

    public MinerCertificateData onClientUpdate() {
        if (this.completeSound) {
            ClientHandler.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.0F);
            ClientHandler.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        }

        if (this.isCelebration) {
            ClientHandler.playAnimation(this.getStack());
        }

        return this;
    }

    public MinerCertificateData sendClientMessage(Player player) {
        if (!ServicePlatform.get().players().isPlayerUltimineCapable(player)) {
            player.displayClientMessage(Component.translatable("info.ultimine_addition.obtain").withStyle(ChatFormatting.GOLD), true);
        } else {
            player.displayClientMessage(Component.translatable("info.ultimine_addition.obtained_already").withStyle(ChatFormatting.RED), true);
        }

        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof MinerCertificateData data)) {
            return false;
        } else {
            return this.isAccomplished == data.isAccomplished && this.isCelebration == data.isCelebration && this.completeSound == data.completeSound && Objects.equals(this.legacy, data.legacy);
        }
    }

    public int hashCode() {
        return Objects.hash(this.legacy, this.isAccomplished, this.isCelebration, this.completeSound);
    }

    public static class Legacy {
        public static final Codec<Legacy> CODEC = RecordCodecBuilder.create((instance) -> instance.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("MinedBlocks", 0).forGetter(Legacy::getMinedBlocks), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("RequiredAmount").forGetter(Legacy::getRequiredAmount)).apply(instance, Legacy::new));
        public static final StreamCodec<ByteBuf, Legacy> STREAM_CODEC;
        private MinerCertificateData data;
        private int minedBlocks;
        private final int requiredAmount;

        public Legacy(int requiredAmount) {
            this(0, requiredAmount);
        }

        private Legacy(int minedBlocks, int requiredAmount) {
            this.minedBlocks = minedBlocks;
            this.requiredAmount = requiredAmount;
        }

        public int getRequiredAmount() {
            return this.requiredAmount;
        }

        public int getMinedBlocks() {
            return this.minedBlocks;
        }

        public void addPoint(int sum) {
            this.minedBlocks = Math.min(this.minedBlocks + sum, this.requiredAmount);
        }

        public void createInfoComponent(List<Component> tooltipComponents, boolean isShiftPressed) {
            CompoundTag tag = this.data.getStack().getTag();
            if (tag != null && !tag.getCompound(MinerCertificateData.DATA_ID.toString()).isEmpty()) {
                if (!isShiftPressed) {
                    if (!this.data.isAccomplished) {
                        tooltipComponents.add(1, this.createBrackets(Component.translatable("tooltip.ultimine_addition.certificate.legacy.sealed").withStyle(ChatFormatting.GRAY)));
                    } else {
                        tooltipComponents.add(1, this.createBrackets(Component.translatable("tooltip.ultimine_addition.certificate.legacy.opened").withStyle(ChatFormatting.GOLD)));
                    }
                } else if (!this.data.isAccomplished) {
                    ChatFormatting formatting = ChatFormattingUtils.getProgressColor(this.minedBlocks, this.requiredAmount);
                    Component component = Component.literal(String.valueOf(this.minedBlocks)).withStyle(formatting);
                    tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest.info", this.requiredAmount).withStyle(ChatFormatting.DARK_AQUA));
                    tooltipComponents.add(Component.literal("➤ ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest", component).withStyle(ChatFormatting.GRAY)));
                } else {
                    tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.certificate.legacy.quest.congrats", this.minedBlocks).withStyle(ChatFormatting.GREEN));
                }

            } else {
                if (isShiftPressed) {
                    tooltipComponents.add(Component.translatable("tooltip.ultimine_addition.certificate.legacy.info"));
                }

            }
        }

        private Component createBrackets(Component component) {
            return Component.literal("『").withStyle(ChatFormatting.DARK_GRAY).append(component).append(Component.literal("』").withStyle(ChatFormatting.DARK_GRAY));
        }

        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, Legacy::getMinedBlocks, ByteBufCodecs.INT, Legacy::getRequiredAmount, Legacy::new);
        }
    }
}
