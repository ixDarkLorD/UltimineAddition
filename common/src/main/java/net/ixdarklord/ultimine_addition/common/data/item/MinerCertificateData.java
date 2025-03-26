package net.ixdarklord.ultimine_addition.common.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ixdarklord.coolcatlib.api.util.ChatFormattingUtils;
import net.ixdarklord.ultimine_addition.client.handler.ClientMinerCertificateHandler;
import net.ixdarklord.ultimine_addition.common.data.DataHandler;
import net.ixdarklord.ultimine_addition.common.item.MinerCertificateItem;
import net.ixdarklord.ultimine_addition.common.network.PayloadHandler;
import net.ixdarklord.ultimine_addition.common.network.payloads.MinerCertificatePayload;
import net.ixdarklord.ultimine_addition.config.ConfigHandler;
import net.ixdarklord.ultimine_addition.config.PlaystyleMode;
import net.ixdarklord.ultimine_addition.core.ServicePlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MinerCertificateData extends DataHandler<MinerCertificateData, ItemStack> {
    public static final Codec<MinerCertificateData> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, MinerCertificateData> STREAM_CODEC;
    public static final DataComponentType<MinerCertificateData> DATA_COMPONENT;

    private Optional<Legacy> legacy;
    private boolean isAccomplished;
    private boolean isCelebration;
    private boolean completeSound;

    private MinerCertificateData() {
        this(Optional.empty(), false);
    }

    private MinerCertificateData(Optional<Legacy> legacy, boolean isAccomplished) {
        this.legacy = legacy;
        this.legacy.ifPresent(l -> l.data = this);
        this.isAccomplished = isAccomplished;
    }

    public static MinerCertificateData create() {
        return new MinerCertificateData();
    }

    public static MinerCertificateData loadData(ItemStack stack) {
        return stack.getOrDefault(DATA_COMPONENT, create()).setDataHolder(stack);
    }

    @Override
    public void saveData(ItemStack stack) {
        stack.set(DATA_COMPONENT, this);
        super.saveData(stack);
    }

    public MinerCertificateData sendToClient(ServerPlayer player) {
        PayloadHandler.sendToPlayer(new MinerCertificatePayload(this, this.get()), player);
        return this;
    }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Legacy.CODEC.optionalFieldOf("Legacy").forGetter(MinerCertificateData::getLegacy),
                Codec.BOOL.optionalFieldOf("IsAccomplished", false).forGetter(MinerCertificateData::isAccomplished)
        ).apply(instance, MinerCertificateData::new));

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NotNull MinerCertificateData decode(RegistryFriendlyByteBuf buf) {
                return new MinerCertificateData(buf.readOptional(Legacy.STREAM_CODEC), buf.readBoolean())
                        .completeSound(buf.readBoolean())
                        .playCelebration(buf.readBoolean());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, MinerCertificateData data) {
                buf.writeOptional(data.legacy, Legacy.STREAM_CODEC);
                buf.writeBoolean(data.isAccomplished);
                buf.writeBoolean(data.completeSound);
                buf.writeBoolean(data.isCelebration);
            }
        };

        DATA_COMPONENT = DataComponentType.<MinerCertificateData>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build();
    }

    public void tick(ServerPlayer player) {
        if (ConfigHandler.COMMON.PLAYSTYLE_MODE.get() == PlaystyleMode.LEGACY && this.legacy.isEmpty()) {
            int min = ConfigHandler.SERVER.LEGACY_REQUIRED_AMOUNT.getValue().getFirst();
            int max = ConfigHandler.SERVER.LEGACY_REQUIRED_AMOUNT.getValue().getLast();
            this.legacy = Optional.of(new Legacy(RandomSource.create().nextIntBetweenInclusive(min, max)));
            this.saveData(this.get());
        }

        if (ServicePlatform.get().players().isPlayerUltimineCapable(player)) return;
        if (this.legacy.isPresent()) {
            if (!this.isAccomplished && this.legacy.get().getMinedBlocks() == this.legacy.get().getRequiredAmount()) {
                this.completeSound(true).setAccomplished(true).sendToClient(player).saveData(this.get());
            }
        } else if (!isAccomplished) {
            this.completeSound(true).setAccomplished(true).sendToClient(player).saveData(this.get());
        }
    }

    public Optional<Legacy> getLegacy() {
        return legacy;
    }

    public boolean isAccomplished() {
        return this.isAccomplished;
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

    @Override
    public MinerCertificateData clientUpdate() {
        if (Objects.requireNonNull(this.get()).getItem() instanceof MinerCertificateItem && this.completeSound) {
            ClientMinerCertificateHandler.playClientSound();
        }
        if (Objects.requireNonNull(this.get()).getItem() instanceof MinerCertificateItem && this.isCelebration) {
            ClientMinerCertificateHandler.playAnimation(this.get());
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
        if (this == o) return true;
        if (!(o instanceof MinerCertificateData data)) return false;
        return isAccomplished == data.isAccomplished && isCelebration == data.isCelebration && completeSound == data.completeSound && Objects.equals(legacy, data.legacy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(legacy, isAccomplished, isCelebration, completeSound);
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
            if (!data.get().has(DATA_COMPONENT)) {
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
                    ChatFormatting[] formatting = ChatFormattingUtils.get3LevelChatFormatting(minedBlocks, requiredAmount);
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
    }
}
