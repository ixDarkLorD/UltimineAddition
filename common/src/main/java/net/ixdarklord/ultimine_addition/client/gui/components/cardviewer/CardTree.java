package net.ixdarklord.ultimine_addition.client.gui.components.cardviewer;

import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengeData;
import net.ixdarklord.ultimine_addition.common.data.challenge.ChallengesManager;
import net.ixdarklord.ultimine_addition.common.data.item.MiningSkillCardData;
import net.ixdarklord.ultimine_addition.common.data.record.CardHistory;
import net.ixdarklord.ultimine_addition.common.item.MiningSkillCardItem;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

/**
 * The layout (in viewport world coordinates) of one card's tiers, left to right, each with its challenges
 * listed below it. Past tiers come from the card's {@link CardHistory}, the current tier from the card itself,
 * and future tiers are placeholders.
 */
final class CardTree {
    static final int COLUMN_WIDTH = 132;
    static final int TIER_WIDTH = 112;
    static final int TIER_HEIGHT = 32;
    static final int ROW_TOP = TIER_HEIGHT + 14;
    static final int ROW_HEIGHT = 26;
    static final int ROW_INDENT = 10;
    static final int ROW_WIDTH = TIER_WIDTH - ROW_INDENT;
    static final int ROW_BOX_HEIGHT = 22;

    enum TierState { COMPLETED, SKIPPED, CURRENT, LOCKED }

    enum ChallengeState { COMPLETED, IN_PROGRESS, NOT_STARTED, NEEDS_CONSUME_MODE, LOCKED }

    record ChallengeNode(MiningSkillCardItem.Tier tier, @Nullable Identifier id, int order, int currentPoints, int requiredPoints,
                         ChallengeState state, boolean pinned, OptionalLong completedAt, List<ItemStack> targets, int x, int y) {
        boolean contains(double worldX, double worldY) {
            return worldX >= this.x && worldX < this.x + ROW_WIDTH && worldY >= this.y && worldY < this.y + ROW_BOX_HEIGHT;
        }

        boolean isLive() {
            return this.id != null && this.state != ChallengeState.LOCKED && this.tier != null;
        }

        float progress() {
            return this.requiredPoints <= 0 ? 0.0F : Math.min(1.0F, (float) this.currentPoints / this.requiredPoints);
        }
    }

    record TierNode(MiningSkillCardItem.Tier tier, TierState state, OptionalLong completedAt, boolean hasRecord,
                    List<ChallengeNode> challenges, int x, int y) {
        boolean contains(double worldX, double worldY) {
            return worldX >= this.x && worldX < this.x + TIER_WIDTH && worldY >= this.y && worldY < this.y + TIER_HEIGHT;
        }

        long completedCount() {
            return this.challenges.stream().filter(c -> c.state() == ChallengeState.COMPLETED).count();
        }

        int centerX() {
            return this.x + TIER_WIDTH / 2;
        }
    }

    final List<TierNode> tiers;
    final ScreenRectangle bounds;
    final MiningSkillCardItem.Tier currentTier;

    private CardTree(List<TierNode> tiers, MiningSkillCardItem.Tier currentTier) {
        this.tiers = tiers;
        this.currentTier = currentTier;
        int height = TIER_HEIGHT;
        for (TierNode node : tiers) {
            height = Math.max(height, ROW_TOP + node.challenges().size() * ROW_HEIGHT);
        }
        this.bounds = new ScreenRectangle(0, 0, (tiers.size() - 1) * COLUMN_WIDTH + TIER_WIDTH, height);
    }

    TierNode getTier(MiningSkillCardItem.Tier tier) {
        return this.tiers.get(tier.ordinal());
    }

    static CardTree build(MiningSkillCardData card, @Nullable CardHistory history, boolean consumeMode) {
        MiningSkillCardItem.Tier current = card.getTier();
        List<TierNode> nodes = new ArrayList<>();

        for (MiningSkillCardItem.Tier tier : MiningSkillCardItem.Tier.values()) {
            int x = tier.ordinal() * COLUMN_WIDTH;
            List<ChallengeNode> rows = new ArrayList<>();
            int rowY = ROW_TOP;

            if (tier.ordinal() < current.ordinal()) {
                CardHistory.TierRecord record = history == null ? null : history.getTier(tier).orElse(null);
                if (record != null) {
                    for (CardHistory.ChallengeRecord challenge : record.challenges()) {
                        rows.add(new ChallengeNode(tier, challenge.id(), challenge.order(), challenge.requiredPoints(), challenge.requiredPoints(),
                                ChallengeState.COMPLETED, false, time(challenge.completedAt()), targets(challenge.id()), x + ROW_INDENT, rowY));
                        rowY += ROW_HEIGHT;
                    }
                }
                boolean hasRecord = record != null && !record.skipped();
                nodes.add(new TierNode(tier, record != null && record.skipped() ? TierState.SKIPPED : TierState.COMPLETED,
                        record == null ? OptionalLong.empty() : time(record.completedAt()), hasRecord, rows, x, 0));
            } else if (tier == current) {
                for (MiningSkillCardData.Challenge challenge : card.getChallenges().stream().sorted().toList()) {
                    ChallengeData data = ChallengesManager.INSTANCE.getAllChallenges().get(challenge.getId());
                    ChallengeState state;
                    if (card.isChallengeAccomplished(challenge)) state = ChallengeState.COMPLETED;
                    else if (data != null && data.challengeType().isConsuming() && !consumeMode) state = ChallengeState.NEEDS_CONSUME_MODE;
                    else if (challenge.getCurrentPoints() > 0) state = ChallengeState.IN_PROGRESS;
                    else state = ChallengeState.NOT_STARTED;

                    OptionalLong completedAt = history == null ? OptionalLong.empty() : history.getCompletionTime(challenge.getId());
                    rows.add(new ChallengeNode(tier, challenge.getId(), challenge.getOrder(), challenge.getCurrentPoints(), challenge.getRequiredPoints(),
                            state, challenge.isPinned(), completedAt, targets(challenge.getId()), x + ROW_INDENT, rowY));
                    rowY += ROW_HEIGHT;
                }
                nodes.add(new TierNode(tier, TierState.CURRENT, OptionalLong.empty(), true, rows, x, 0));
            } else {
                // A locked tier's challenges aren't rolled yet; they appear once the tier is unlocked.
                nodes.add(new TierNode(tier, TierState.LOCKED, OptionalLong.empty(), false, rows, x, 0));
            }
        }
        return new CardTree(List.copyOf(nodes), current);
    }

    private static OptionalLong time(long millis) {
        return millis > 0 ? OptionalLong.of(millis) : OptionalLong.empty();
    }

    private static List<ItemStack> targets(Identifier challengeId) {
        ChallengeData data = ChallengesManager.INSTANCE.getAllChallenges().get(challengeId);
        if (data == null) return List.of();
        return ChallengesManager.INSTANCE.utilizeTargetedBlocks(data).stream()
                .map(Block::asItem)
                .map(item -> item.getDefaultInstance())
                .filter(stack -> !stack.isEmpty())
                .toList();
    }
}
