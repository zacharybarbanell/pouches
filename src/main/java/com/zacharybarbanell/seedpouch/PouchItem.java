/*
 * This class is adapted from part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package com.zacharybarbanell.seedpouch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraftforge.network.NetworkHooks;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.List;
import java.util.function.Supplier;

//import vazkii.botania.client.gui.bag.FlowerPouchContainer;
//import vazkii.botania.common.block.BotaniaDoubleFlowerBlock;
//import vazkii.botania.common.block.BotaniaFlowerBlock;
import com.zacharybarbanell.seedpouch.helper.EntityHelper;
//import vazkii.botania.common.helper.InventoryHelper;
//import vazkii.botania.xplat.XplatAbstractions;

import java.util.stream.IntStream;

public class PouchItem extends Item {
	private Supplier<List<Item>> itemListSupplier;
	private BiMap<Integer, Item> slots;

	public PouchItem(Properties props, Supplier<List<Item>> itemListSupplier) {
		super(props);
		this.itemListSupplier = itemListSupplier;
		this.slots = null;
	}

	public BiMap<Integer, Item> getSlots() {
		if (this.slots == null) {
			List<Item> items = itemListSupplier.get();
			this.slots = HashBiMap.create(items.size());
			int i = 0;
			for (Item item : items) {
				this.slots.put(i, item);
				i++;
			}
		}
		return this.slots;
	}

	public int getSize() {
		return this.getSlots().size();
	}

	public boolean isValid(int slot, ItemStack stack) {
		return this.getSlots().get(slot) == stack.getItem();
	}

	public SimpleContainer getInventory(ItemStack stack) {
		return new ItemBackedInventory(stack, this.getSize()) {
			@Override
			public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
				return PouchItem.this.isValid(slot, stack);
			}
		};
	}

	public static boolean onPickupItem(ItemEntity entity, Player player) {
		ItemStack entityStack = entity.getItem();

		if (entityStack.getCount() > 0) {
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				if (i == player.getInventory().selected) {
					continue; // prevent item deletion
				}

				ItemStack bag = player.getInventory().getItem(i);
				if (!bag.isEmpty() && bag.getItem() instanceof PouchItem) {
					PouchItem bagItem = (PouchItem) bag.getItem();
					Integer slot = bagItem.getSlots().inverse().get(entityStack.getItem());
					if (slot != null) {
						SimpleContainer bagInv = bagItem.getInventory(bag);
						ItemStack existing = bagInv.getItem(slot);
						int newCount = Math.min(existing.getCount() + entityStack.getCount(),
								Math.min(existing.getMaxStackSize(), bagInv.getMaxStackSize()));
						int numPickedUp = newCount - existing.getCount();

						if (numPickedUp > 0) {
							if (existing.isEmpty()) {
								bagInv.setItem(slot, entityStack.split(numPickedUp));
							} else {
								existing.grow(numPickedUp);
								entityStack.shrink(numPickedUp);
							}
							EntityHelper.syncItem(entity);
							bagInv.setChanged();

							player.take(entity, numPickedUp);

							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {

		if (!world.isClientSide) {
			ItemStack stack = player.getItemInHand(hand);
			NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {

				@Override
				public Component getDisplayName() {
					return stack.getHoverName();
				}

				@Override
				public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
					return new PouchContainer(syncId, inv, stack, PouchItem.this);
				}
			}, buf -> buf.writeBoolean(hand == InteractionHand.MAIN_HAND));
		}
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
				world.isClientSide());

	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Direction side = ctx.getClickedFace();

		BlockEntity tile = world.getBlockEntity(pos);
		if (tile != null) {
			if (!world.isClientSide) {
				Container tileInv;
				if (tile instanceof Container container) {
					tileInv = container;
				} else {
					return InteractionResult.FAIL;
				}

				Container bagInv = getInventory(ctx.getItemInHand());
				for (int i = 0; i < bagInv.getContainerSize(); i++) {
					ItemStack item = bagInv.getItem(i);
					ItemStack rem = HopperBlockEntity.addItem(bagInv, tileInv, item, side);
					bagInv.setItem(i, rem);
				}

			}

			return InteractionResult.sidedSuccess(world.isClientSide());
		}
		return InteractionResult.PASS;
	}

	@Override
	public void onDestroyed(@NotNull ItemEntity entity) {
		var container = getInventory(entity.getItem());
		var stream = IntStream.range(0, container.getContainerSize())
				.mapToObj(container::getItem)
				.filter(s -> !s.isEmpty());
		ItemUtils.onContainerDestroyed(entity, stream);
		container.clearContent();
	}

	@Override
	public boolean overrideStackedOnOther(
			@NotNull ItemStack bag, @NotNull Slot slot,
			@NotNull ClickAction clickAction, @NotNull Player player) {
		return false; // TODO implement this
		/*
		 * return InventoryHelper.overrideStackedOnOther(
		 * FlowerPouchItem::getInventory,
		 * player.containerMenu instanceof FlowerPouchContainer,
		 * bag, slot, clickAction, player);
		 */
	}

	@Override
	public boolean overrideOtherStackedOnMe(
			@NotNull ItemStack bag, @NotNull ItemStack toInsert,
			@NotNull Slot slot, @NotNull ClickAction clickAction,
			@NotNull Player player, @NotNull SlotAccess cursorAccess) {
		return false; // TODO implement this
		/*
		 * 
		 * return InventoryHelper.overrideOtherStackedOnMe(
		 * FlowerPouchItem::getInventory,
		 * player.containerMenu instanceof FlowerPouchContainer,
		 * bag, toInsert, clickAction, cursorAccess);
		 */
	}
}
