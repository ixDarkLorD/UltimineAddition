package net.ixdarklord.ultimine_addition.common.event;

public class DevEvents {
    public static void init() {
        /*if (!Platform.isDevelopmentEnvironment()) return;

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, interactionHand) -> {
            if (player.level().isClientSide()) return CompoundEventResult.pass();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack stack1 = player.getItemInHand(InteractionHand.OFF_HAND);

            if (!stack.has(MiningSkillCardData.DATA_COMPONENT) || !stack1.has(MiningSkillCardData.DATA_COMPONENT)) return CompoundEventResult.pass();
            MiningSkillCardData data = stack.get(MiningSkillCardData.DATA_COMPONENT);
            MiningSkillCardData data1 = stack1.get(MiningSkillCardData.DATA_COMPONENT);

            System.out.printf("%s State: %s\n", interactionHand.name(), data.equals(data1));
            return CompoundEventResult.pass();
        });*/
    }
}
