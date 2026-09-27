package net.ixdarklord.ultimine_addition.common.data.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

final class CardEntry {
    static final Codec<CardEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CardHistory.MAP_CODEC.forGetter(entry -> entry.history),
            CardProgress.CODEC.optionalFieldOf("Progress").forGetter(entry -> Optional.ofNullable(entry.progress))
    ).apply(instance, (history, progress) -> new CardEntry(history, progress.orElse(null))));

    final CardHistory history;
    @Nullable CardProgress progress;
    int version;

    CardEntry(CardHistory history, @Nullable CardProgress progress) {
        this.history = history;
        this.progress = progress;
    }

    CardEntry() {
        this(new CardHistory(), null);
    }
}
