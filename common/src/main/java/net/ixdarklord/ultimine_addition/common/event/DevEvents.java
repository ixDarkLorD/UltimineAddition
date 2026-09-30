package net.ixdarklord.ultimine_addition.common.event;

public class DevEvents {
    public static void init() {
        /*if (!Platform.isDevelopmentEnvironment()) return;

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, interactionHand) -> {
            if (player.level().isClientSide()) return CompoundEventResult.pass();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack stack1 = player.getItemInHand(InteractionHand.OFF_HAND);

            if (!MiningSkillCardData.DATA_COMPONENT.has(stack) || !MiningSkillCardData.DATA_COMPONENT.has(stack1)) return CompoundEventResult.pass();
            MiningSkillCardData data = MiningSkillCardData.DATA_COMPONENT.get(stack);
            MiningSkillCardData data1 = MiningSkillCardData.DATA_COMPONENT.get(stack1);

            System.out.printf("%s State: %s\n", interactionHand.name(), data.equals(data1));
            return CompoundEventResult.pass();
        });*/
    }
}
