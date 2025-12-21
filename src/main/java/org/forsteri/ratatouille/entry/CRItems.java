package org.forsteri.ratatouille.entry;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.forsteri.ratatouille.Ratatouille;
import org.forsteri.ratatouille.content.chef_hat.ChefHatItem;
import org.forsteri.ratatouille.content.chef_hat.ChefHatModel;
import org.forsteri.ratatouille.content.chef_hat.ChefHatWithGogglesItem;
import org.forsteri.ratatouille.content.chef_hat.ChefHatWithGogglesModel;
import org.forsteri.ratatouille.content.chocolate_mold_filled.ChocolateMoldFilledItem;
import org.forsteri.ratatouille.content.compost_tea.CompostTeaBottleItem;

public class CRItems {
    static {
        Ratatouille.REGISTRATE.setCreativeTab(CRCreativeModeTabs.BASE_CREATIVE_TAB);
    }

    public static final ItemEntry<Item> SAUSAGE_CASING = Ratatouille.REGISTRATE.item("sausage_casing", Item::new).register();
    public static final ItemEntry<Item> SAUSAGE = Ratatouille.REGISTRATE.item("sausage", Item::new).properties(p -> p.food(new FoodProperties.Builder().nutrition(6)
            .saturationMod(0.6F)
            .build())).register();
    public static final ItemEntry<Item> RAW_SAUSAGE = Ratatouille.REGISTRATE.item("raw_sausage", Item::new).register();
    public static final ItemEntry<Item> SALT = Ratatouille.REGISTRATE.item("salt", Item::new).register();
    public static final ItemEntry<Item> COCOA_POWDER = Ratatouille.REGISTRATE.item("cocoa_powder", Item::new).register();
    public static final ItemEntry<Item> COCOA_BUTTER = Ratatouille.REGISTRATE.item("cocoa_butter", Item::new).register();
    public static final ItemEntry<Item> DRIED_COCOA_BEANS = Ratatouille.REGISTRATE.item("dried_cocoa_beans", Item::new).register();
    public static final ItemEntry<Item> DRIED_COCOA_NIBS = Ratatouille.REGISTRATE.item("dried_cocoa_nibs", Item::new).register();
    public static final ItemEntry<Item> COCOA_SOLIDS = Ratatouille.REGISTRATE.item("cocoa_solids", Item::new).register();
    public static final ItemEntry<Item> CHOCOLATE_MOLD = Ratatouille.REGISTRATE.item("chocolate_mold", Item::new).register();
    public static final ItemEntry<ChocolateMoldFilledItem> CHOCOLATE_MOLD_FILLED = Ratatouille.REGISTRATE.item("chocolate_mold_filled", ChocolateMoldFilledItem::new).register();
    public static final ItemEntry<Item> CHOCOLATE_MOLD_SOLID = Ratatouille.REGISTRATE.item("chocolate_mold_solid", Item::new).register();
    public static final ItemEntry<Item> CAKE_MOLD = Ratatouille.REGISTRATE.item("cake_mold", Item::new).register();
    public static final ItemEntry<Item> CAKE_MOLD_FILLED = Ratatouille.REGISTRATE.item("cake_mold_filled", Item::new).register();
    public static final ItemEntry<Item> CAKE_MOLD_BAKED = Ratatouille.REGISTRATE.item("cake_mold_baked", Item::new).register();
    public static final ItemEntry<Item> CAKE_BASE = Ratatouille.REGISTRATE.item("cake_base", Item::new).properties(p -> p.food(new FoodProperties.Builder().nutrition(10)
            .saturationMod(0.5F)
            .build())).register();
    public static final ItemEntry<Item> POPSICLE_MOLD = Ratatouille.REGISTRATE.item("popsicle_mold", Item::new).register();
    public static final ItemEntry<Item> MELON_POPSICLE_MOLD_FILLED = Ratatouille.REGISTRATE.item("melon_popsicle_mold_filled", Item::new).register();
    public static final ItemEntry<Item> MELON_POPSICLE_MOLD_SOLID = Ratatouille.REGISTRATE.item("melon_popsicle_mold_solid", Item::new).register();
    public static final ItemEntry<Item> EGG_SHELL = Ratatouille.REGISTRATE.item("egg_shell", Item::new).register();
    public static final ItemEntry<Item> WHEAT_KERNELS = Ratatouille.REGISTRATE.item("wheat_kernels", Item::new).register();
    public static final ItemEntry<Item> SALTY_DOUGH = Ratatouille.REGISTRATE.item("salty_dough", Item::new).register();
    public static final ItemEntry<Item> COMPOST_RESIDUE = Ratatouille.REGISTRATE.item("compost_residue", Item::new).register();
    public static final ItemEntry<Item> COMPOST_MASS = Ratatouille.REGISTRATE.item("compost_mass", Item::new).register();
    public static final ItemEntry<Item> RIPEN_MATTER = Ratatouille.REGISTRATE.item("ripen_matter", Item::new).onRegisterAfter(Registries.ITEM, item -> ItemDescription.useKey(item, "item.ratatouille.ripen_matter")).register();
    public static final ItemEntry<SequencedAssemblyItem> UNPROCESSED_RIPEN_MATTER_FOLD = Ratatouille.REGISTRATE.item("unprocessed_ripen_matter_fold", SequencedAssemblyItem::new).register();
    public static final ItemEntry<Item> RIPEN_MATTER_FOLD = Ratatouille.REGISTRATE.item("ripen_matter_fold", Item::new).register();
    public static final ItemEntry<Item> MATURE_MATTER = Ratatouille.REGISTRATE.item("mature_matter", Item::new).onRegisterAfter(Registries.ITEM, item -> ItemDescription.useKey(item, "item.ratatouille.mature_matter")).register();
    public static final ItemEntry<SequencedAssemblyItem> UNPROCESSED_MATURE_MATTER_FOLD = Ratatouille.REGISTRATE.item("unprocessed_mature_matter_fold", SequencedAssemblyItem::new).register();
    public static final ItemEntry<Item> MATURE_MATTER_FOLD = Ratatouille.REGISTRATE.item("mature_matter_fold", Item::new).register();
    public static final ItemEntry<Item> BOIL_STONE = Ratatouille.REGISTRATE.item("boil_stone", Item::new).register();
    public static final ItemEntry<Item> FISH_FEED = Ratatouille.REGISTRATE.item("fish_feed", Item::new).register();
    public static final ItemEntry<CompostTeaBottleItem> COMPOST_TEA_BOTTLE =
            Ratatouille.REGISTRATE.item("compost_tea_bottle", CompostTeaBottleItem::new)
                    .properties(p -> p.stacksTo(16))
                    .onRegisterAfter(Registries.ITEM, item -> ItemDescription.useKey(item, "item.ratatouille.compost_tea_bottle"))
                    .register();
    public static final ItemEntry<ChefHatItem> CHEF_HAT = Ratatouille.REGISTRATE.item("chef_hat", ChefHatItem::new)
            .properties(p -> p.stacksTo(1))
            .onRegister(CreateRegistrate.itemModel(() -> ChefHatModel::new))
            .register();
    public static final ItemEntry<ChefHatWithGogglesItem> CHEF_HAT_WITH_GOGGLES = Ratatouille.REGISTRATE.item("chef_hat_with_goggles", ChefHatWithGogglesItem::new)
            .properties(p -> p.stacksTo(1))
            .onRegister(CreateRegistrate.itemModel(() -> ChefHatWithGogglesModel::new))
            .register();

    //public static final ItemEntry<Item> WET_COPPER_INGOT = Ratatouille.REGISTRATE.item("wet_copper_ingot", Item::new).register();
    //public static final ItemEntry<Item> WET_GOLD_INGOT = Ratatouille.REGISTRATE.item("wet_gold_ingot", Item::new).register();
    //public static final ItemEntry<Item> SUGAR_CUBE = Ratatouille.REGISTRATE.item("sugar_cube", Item::new).register();
    //public static final ItemEntry<Item> VANILLA_POWDER = Ratatouille.REGISTRATE.item("vanilla_powder", Item::new).register();
    //public static final ItemEntry<Item> ICE_CRYSTAL = Ratatouille.REGISTRATE.item("ice_crystal", Item::new).register();
    public CRItems() {}

    public static void register() {}
}
