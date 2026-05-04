package org.forsteri.ratatouille.entry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.forsteri.ratatouille.Ratatouille;

public class CRTags {

    public static final TagKey<Item> MOLD = CRTags.modItemTag("mold");
    public static final TagKey<Item> RAW_MEAT = CRTags.modItemTag("raw_meat");
    public static final TagKey<Item> EGGS = CRTags.modItemTag("eggs");
    public static final TagKey<Item> COMPOSTABLE_ITEMS_1to1 = CRTags.modItemTag("compostable_items_1to1");
    public static final TagKey<Item> COMPOSTABLE_ITEMS_1to4 = CRTags.modItemTag("compostable_items_1to4");
    public static final TagKey<Item> COMPOSTABLE_ITEMS_2to1 = CRTags.modItemTag("compostable_items_2to1");
    public static final TagKey<Item> COMPOSTABLE_ITEMS_4to1 = CRTags.modItemTag("compostable_items_4to1");

    public static final TagKey<Item> SALTS = commonItemTag("salts");

    public static void init() {
    }

    public static TagKey<Item> modItemTag(String id) {
        return ItemTags.create(Ratatouille.asResource(id));
    }

    public static TagKey<Item> commonItemTag(String id) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", id));
    }


}
