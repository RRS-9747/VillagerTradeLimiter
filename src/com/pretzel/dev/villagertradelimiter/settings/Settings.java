package com.pretzel.dev.villagertradelimiter.settings;

import com.pretzel.dev.villagertradelimiter.VillagerTradeLimiter;
import com.pretzel.dev.villagertradelimiter.lib.Util;
import com.pretzel.dev.villagertradelimiter.wrappers.RecipeWrapper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class Settings {
    private final VillagerTradeLimiter instance;

    /** @param instance The instance of VillagerTradeLimiter.java */
    public Settings(final VillagerTradeLimiter instance) { this.instance = instance; }

    /**
     * @param recipe The wrapped recipe to fetch any overrides for
     * @param key The key where the fetched value is stored in config.yml (e.g, DisableTrading)
     * @param defaultValue The default boolean value to use if the key does not exist
     * @return A boolean value that has the most specific value possible between the global setting and the overrides settings
     */
    public boolean fetchBoolean(final RecipeWrapper recipe, String key, boolean defaultValue) {
        boolean global = instance.getCfg().getBoolean(key, defaultValue);
        final ConfigurationSection override = getOverride(recipe);
        if(override != null) return override.getBoolean(key, global);
        return global;
    }

    /**
     * @param recipe The wrapped recipe to fetch any overrides for
     * @param key The key where the fetched value is stored in config.yml (e.g, MaxDemand)
     * @param defaultValue The default integer value to use if the key does not exist
     * @return An integer value that has the most specific value possible between the global setting and the overrides settings
     */
    public int fetchInt(final RecipeWrapper recipe, String key, int defaultValue) {
        int global = instance.getCfg().getInt(key, defaultValue);
        final ConfigurationSection override = getOverride(recipe);
        if(override != null) return override.getInt(key, global);
        return global;
    }

    /**
     * @param recipe The wrapped recipe to fetch any overrides for
     * @param key The key where the fetched value is stored in config.yml (e.g, MaxDiscount)
     * @param defaultValue The default double value to use if the key does not exist
     * @return A double value that has the most specific value possible between the global setting and the overrides settings
     */
    public double fetchDouble(final RecipeWrapper recipe, String key, double defaultValue) {
        double global = instance.getCfg().getDouble(key, defaultValue);
        final ConfigurationSection override = getOverride(recipe);
        if(override != null) return override.getDouble(key, global);
        return global;
    }

    /**
     * @param recipe The wrapped recipe to fetch any overrides for
     * @return The corresponding override config section for the recipe, if it exists, or null
     */
    public ConfigurationSection getOverride(final RecipeWrapper recipe) {
        final ConfigurationSection overrides = instance.getCfg().getConfigurationSection("Overrides");
        if(overrides != null) {
            for(final String override : overrides.getKeys(false)) {
                final ConfigurationSection item = this.getItem(recipe, override);
                if(item != null) return item;
            }
        }
        return null;
    }

    /**
     * @param recipe The wrapped recipe to fetch any overrides for
     * @param key The key where the override settings are stored in config.yml
     * @return The corresponding override config section for the recipe, if it exists, or null
     */
    public ConfigurationSection getItem(final RecipeWrapper recipe, final String key) {
        final ConfigurationSection item = instance.getCfg().getConfigurationSection("Overrides."+key);
        if(item == null) return null;

        if(!key.contains("_")) {
            //Return the item if the item name is valid
            if(this.verify(recipe, Material.matchMaterial(key))) return item;
            return null;
        }

        final String[] words = key.split("_");
        try {
            //Return the enchanted book item if there's a number in the item name
            final int level = Integer.parseInt(words[words.length-1]);
            if(recipe.getSellItemStack().getType() == Material.ENCHANTED_BOOK) {
                final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) recipe.getSellItemStack().getItemMeta();
                final Enchantment enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(key.substring(0, key.lastIndexOf("_"))));
                if (meta == null || enchantment == null) return null;
                if (meta.hasStoredEnchant(enchantment) && meta.getStoredEnchantLevel(enchantment) == level) return item;
            }
        } catch(NumberFormatException e) {
            //Return the item if the item name is valid
            if(this.verify(recipe, Material.matchMaterial(key)))
                return item;
            return null;
        } catch(Exception e2) {
            //Send an error message
            Util.errorMsg(e2);
        }
        return null;
    }

    /**
     * @param recipe The wrapped recipe to match with the override setting
     * @param material The material to compare the recipe against
     * @return True if a recipe matches an override section, false otherwise
     */
    private boolean verify(final RecipeWrapper recipe, final Material material) {
        return ((recipe.getSellItemStack().getType() == material) || (recipe.getBuyItemStack().getType() == material));
    }
}