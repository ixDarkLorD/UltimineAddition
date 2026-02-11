package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatlib.api.data.ItemDataComponent;
import net.ixdarklord.coolcatlib.api.utils.ChatFormattingUtils;
import net.ixdarklord.ultimine_addition.client.handler.ClientHandler;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.ixdarklord.ultimine_addition.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.network.payloads.MinerCertificatePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MinerCertificateData extends ItemDataComponent<MinerCertificateData> {
    public static final Codec<MinerCertificateData> CODEC =
            RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.BOOL.fieldOf("IsAccomplished").forGetter(MinerCertificateData::isAccomplished),
                    MinerCertificateData.Legacy.CODEC.optionalFieldOf("Legacy").forGetter(MinerCertificateData::getLegacy))
                    .apply(instance, (isAccomplished, legacyOpt) -> new MinerCertificateData(isAccomplished, legacyOpt.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinerCertificateData> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull MinerCertificateData decode(RegistryFriendlyByteBuf buf) {
            return new MinerCertificateData(buf.readBoolean(), buf.readOptional(Legacy.STREAM_CODEC).orElse(null))
                    .completeSound(buf.readBoolean())
                    .playCelebration(buf.readBoolean());
        }

        public void encode(RegistryFriendlyByteBuf buf, MinerCertificateData data) {
            buf.writeBoolean(data.isAccomplished);
            buf.writeOptional(data.getLegacy(), MinerCertificateData.Legacy.STREAM_CODEC);
            buf.writeBoolean(data.completeSound);
            buf.writeBoolean(data.isCelebration);
        }
    };

    public static final DataComponentType<MinerCertificateData> DATA_COMPONENT =
            DataComponentType.<MinerCertificateData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();

    private @Nullable Legacy legacy;
    private boolean isAccomplished;
    private boolean isCelebration;
    private boolean completeSound;

    private MinerCertificateData(boolean isAccomplished, @Nullable Legacy legacy) {
        super(DATA_COMPONENT);
        this.isAccomplished = isAccomplished;
        this.legacy = legacy;
        this.getLegacy().ifPresent((l) -> l.data = this);
    }

    public static MinerCertificateData create() {
        return new MinerCertificateData(false, null);
    }

    public static MinerCertificateData load(ItemStack stack) {
        return stack.getOrDefault(DATA_COMPONENT, create()).setStack(stack);
    }

    public static boolean hasData(@NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MinerCertificateItem && stack.has(DATA_COMPONENT);
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
        isCelebration = state;
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
        PayloadHandler.sendToPlayer(new MinerCertificatePayload(slotIndex, this.getStack(), this), player);
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
        if (!ServicePlatform.get().players().isPlayerUltimineCapable(player))
            player.displayClientMessage(Component.translatable("info.ultimine_addition.obtain").withStyle(ChatFormatting.GOLD), true);
        else
            player.displayClientMessage(Component.translatable("info.ultimine_addition.obtained_already").withStyle(ChatFormatting.RED), true);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MinerCertificateData that)) {
            return false;
        } else {
            return this.isAccomplished == that.isAccomplished && Objects.equals(this.legacy, that.legacy);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAccomplished, legacy);
    }

    public static class Legacy {
        public static final Codec<Legacy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("mined_blocks", 0).forGetter(Legacy::getMinedBlocks),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("required_amount").forGetter(Legacy::getRequiredAmount)
        ).apply(instance, Legacy::new));

        public static final StreamCodec<? super FriendlyByteBuf, Legacy> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Legacy::getMinedBlocks,
                ByteBufCodecs.INT, Legacy::getRequiredAmount,
                Legacy::new
        );

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
            return requiredAmount;
        }

        public int getMinedBlocks() {
            return minedBlocks;
        }

        public void addPoint(int sum) {
            this.minedBlocks = Math.min(this.minedBlocks + sum, requiredAmount);
        }

        public void createInfoComponent(List<Component> tooltipComponents, boolean isShiftPressed) {
            if (!data.getStack().has(DATA_COMPONENT)) {
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
                    ChatFormatting formatting = ChatFormattingUtils.getProgressColor(minedBlocks, requiredAmount);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Legacy legacy)) return false;
            return minedBlocks == legacy.minedBlocks && requiredAmount == legacy.requiredAmount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(minedBlocks, requiredAmount);
        }
    }
}
