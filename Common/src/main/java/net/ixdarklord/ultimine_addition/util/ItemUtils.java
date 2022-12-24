package net.ixdarklord.ultimine_addition.util;

public class ItemUtils {
    public static class IntArrayMaker {
        private final int slotID;
        private final int amount;
        private final int state;

        public IntArrayMaker(int slotID, int amount, int state) {
            this.slotID = slotID;
            this.amount = amount;
            this.state = state;
        }

        public int[] getArray() {
            return new int[]{slotID, amount, state};
        }

        public static boolean getBoolean(int value) {
            return (value!=0);
        }
    }
}
