package tombenpotter.sanguimancy.tiles;

import WayofTime.bloodmagic.api.registry.AltarRecipeRegistry;
import WayofTime.bloodmagic.block.BlockAltar;
import WayofTime.bloodmagic.tile.TileAltar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import tombenpotter.sanguimancy.api.tiles.TileBaseInventory;

public class TileAltarDiviner extends TileBaseInventory implements ITickable {

    public TileAltarDiviner() {
        super(1);
        customNBTTag = new NBTTagCompound();
    }

    @Override
    public void update() {
        if (noCooldown()) {
            for (EnumFacing dir : EnumFacing.VALUES) {
                BlockPos newPos = pos.add(dir.getDirectionVec());
                if (!worldObj.isAirBlock(newPos) && worldObj.getBlockState(newPos).getBlock() instanceof BlockAltar) {
                    if (worldObj.getTileEntity(newPos) != null && worldObj.getTileEntity(newPos) instanceof TileAltar) {
                        TileAltar tile = (TileAltar) worldObj.getTileEntity(newPos);

                        if (getInventory(null).getStackInSlot(0) != null) {
                            checkBloodAndMoveItems(tile, worldObj.getBlockState(pos));
                        } else if (getInventory(null) == null && tile.getStackInSlot(0) != null) {
                            moveItemFromAltar(tile, worldObj.getBlockState(pos));
                        }
                    }
                }
            }
        }
    }


    public void checkBloodAndMoveItems(TileAltar tile, IBlockState state) {
        ItemStack stack = getInventory(null).getStackInSlot(0).copy();
        if (AltarRecipeRegistry.getRecipeForInput(stack) != null && AltarRecipeRegistry.getRecipeForInput(stack).doesRequiredItemMatch(stack, tile.getTier())) {
            AltarRecipeRegistry.AltarRecipe recipe = AltarRecipeRegistry.getRecipeForInput(stack);
            int maxAmount = tile.getCurrentBlood() / recipe.getSyphon();
            int amount = getInventory(null).getStackInSlot(0).stackSize;


            if (tile.getStackInSlot(0) == null && amount > 0 && amount <= maxAmount) {
                stack.stackSize = amount;
                tile.setInventorySlotContents(0, stack);
                getInventory(null).extractItem(0, amount, false);

                tile.startCycle();
            } else if (tile.getStackInSlot(0) != null) {
                ItemStack altarItem = tile.getStackInSlot(0).copy();
                if (altarItem.isItemEqual(stack) && altarItem.stackSize < altarItem.getMaxStackSize()) {
                    tile.getStackInSlot(0).stackSize += 1;
                    getInventory(null).extractItem(0, 1, false);

                    worldObj.notifyBlockUpdate(pos, state, state, 3);
                    IBlockState altar = worldObj.getBlockState(tile.getPos());
                    worldObj.notifyBlockUpdate(tile.getPos(), altar, altar, 3);

                    cooldown = 10;
                }
            }
        }
    }


    public void moveItemFromAltar(TileAltar tile, IBlockState state) {
        ItemStack stack = tile.getStackInSlot(0).copy();
        if (!tile.isActive() && tile.getProgress() <= 0) {
            getInventory(null).insertItem(0, stack, false);
            tile.setInventorySlotContents(0, null);

            worldObj.notifyBlockUpdate(pos, state, state, 3);
            IBlockState altar = worldObj.getBlockState(tile.getPos());
            worldObj.notifyBlockUpdate(tile.getPos(), altar, altar, 3);

            cooldown = 30;
        }
    }
}