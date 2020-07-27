
package com.shnupbups.easyexcavate.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;


class EasyExcavateConfig {

    private static Log logger = LogFactory.getLog(EasyExcavateConfig.class);

    public int maxBlocks;
    public int maxRange;
    public float bonusExhaustionMultiplier;
    public boolean debugOutput;
    public boolean enableBlockEntities;
    public boolean reverseBehavior;
    public String[] blacklistBlocks;
    public String[] blacklistTools;
    public boolean checkHardness;
    public boolean isToolRequired;
    public boolean invertBlockBlacklist;
    public boolean invertToolBlacklist;
    public boolean dontTakeDurability;

    static EasyExcavateConfig fromFile(File dir) {
        EasyExcavateConfig config;
        File configFile = new File(dir, "easyexcavate.json");
        try (FileReader reader = new FileReader(configFile)) {
            config = new Gson().fromJson(reader, EasyExcavateConfig.class);
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
            } catch (IOException e2) {
                System.out.println("[EasyExcavate] Failed to update config file!");
            }
            System.out.println("[EasyExcavate] Config loaded!");
logger.debug("[EasyExcavate] Debug Output enabled! " + config.toString());
        } catch (IOException e) {
            System.out.println("[EasyExcavate] No config found, generating!");
            config = new EasyExcavateConfig();
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
            } catch (IOException e2) {
                System.out.println("[EasyExcavate] Failed to generate config file!");
            }
        }
        return config;
    }

    EasyExcavateConfig(int maxBlocks,
            int maxRange,
            float bonusExhaustionMultiplier,
            boolean debugOutput,
            boolean enableBlockEntities,
            boolean reverseBehavior,
            String[] blacklistBlocks,
            String[] blacklistTools,
            boolean checkHardness,
            boolean isToolRequired,
            boolean invertBlockBlacklist,
            boolean invertToolBlacklist,
            boolean dontTakeDurability) {
        this.maxBlocks = maxBlocks;
        this.maxRange = maxRange;
        this.bonusExhaustionMultiplier = bonusExhaustionMultiplier;
        this.debugOutput = debugOutput;
        this.enableBlockEntities = enableBlockEntities;
        this.reverseBehavior = reverseBehavior;
        this.blacklistBlocks = blacklistBlocks;
        this.blacklistTools = blacklistTools;
        this.checkHardness = checkHardness;
        this.isToolRequired = isToolRequired;
        this.invertBlockBlacklist = invertBlockBlacklist;
        this.invertToolBlacklist = invertToolBlacklist;
        this.dontTakeDurability = dontTakeDurability;
        updateConfig();
    }

    public EasyExcavateConfig() {
        this(128,
             8,
             0.125f,
             false,
             false,
             false,
             new String[] {
                 "minecraft:example_block", "somemod:example_block_two"
             },
             new String[] {
                 "minecraft:example_pickaxe", "somemod:example_axe"
             },
             false,
             false,
             false,
             false,
             false);
    }

    public void updateConfig() {
        if (blacklistBlocks == null || blacklistBlocks.length == 0) {
            blacklistBlocks = new String[] {
                "minecraft:example_block", "somemod:example_block_two"
            };
        }
        if (blacklistTools == null || blacklistTools.length == 0) {
            blacklistTools = new String[] {
                "minecraft:example_pickaxe", "somemod:example_axe"
            };
        }
    }

    /** */
    final boolean isDeniedBlock(Block block) {
        return (isDenylistedBlock(block) && !invertBlockBlacklist) ||
                (!isDenylistedBlock(block) && invertBlockBlacklist);
    }

    /** */
    final boolean isDenylistedBlock(Block block) {
        return Arrays.asList(blacklistBlocks).contains(Registry.BLOCK.getId(block).toString());
    }

    /** */
    final boolean isDeniedItem(ItemStack tool) {
        return (tool != null && isDenylistedItem(tool) && !invertToolBlacklist) ||
                (tool != null && !isDenylistedItem(tool) && invertToolBlacklist);
    }

    /** */
    final boolean isDenylistedItem(ItemStack tool) {
        return Arrays.asList(blacklistTools).contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString()));
    }

    public String toString() {
        return "maxB: " + maxBlocks + " maxR: " + maxRange + " bem: " + bonusExhaustionMultiplier + " ebe: " +
               enableBlockEntities + " blackB: " + Arrays.asList(blacklistBlocks) + " blackT: " +
               Arrays.asList(blacklistTools) + " checkH: " + checkHardness + " itr: " + isToolRequired + " invBB: " +
               invertBlockBlacklist + " invTB: " + invertToolBlacklist + " dTD: " + dontTakeDurability;
    }

    public PacketByteBuf toBytes(PacketByteBuf buf) {
        buf.writeInt(this.maxBlocks);
        buf.writeInt(this.maxRange);
        buf.writeFloat(this.bonusExhaustionMultiplier);
        buf.writeBoolean(this.enableBlockEntities);
        if (this.blacklistBlocks != null && this.blacklistBlocks.length > 0) {
            buf.writeInt(this.blacklistBlocks.length);
            for (String s : this.blacklistBlocks) {
                buf.writeInt(s.length());
                buf.writeString(s);
            }
        } else
            buf.writeInt(0);
        if (this.blacklistTools != null && this.blacklistTools.length > 0) {
            buf.writeInt(this.blacklistTools.length);
            for (String s : this.blacklistTools) {
                buf.writeInt(s.length());
                buf.writeString(s);
            }
        } else
            buf.writeInt(0);
        buf.writeBoolean(this.checkHardness);
        buf.writeBoolean(this.isToolRequired);
        buf.writeBoolean(this.invertBlockBlacklist);
        buf.writeBoolean(this.invertToolBlacklist);
        buf.writeBoolean(this.dontTakeDurability);
        return buf;
    }

    public EasyExcavateConfig(PacketByteBuf buf) {
        this.maxBlocks = buf.readInt();
        this.maxRange = buf.readInt();
        this.bonusExhaustionMultiplier = buf.readFloat();
        this.enableBlockEntities = buf.readBoolean();
        int blacklistBlocksLength = buf.readInt();
        this.blacklistBlocks = new String[blacklistBlocksLength];
        if (blacklistBlocksLength != 0) {
            for (int i = 0; i < blacklistBlocksLength; i++) {
                blacklistBlocks[i] = buf.readString(buf.readInt());
            }
        }
        int blacklistToolsLength = buf.readInt();
        this.blacklistTools = new String[blacklistToolsLength];
        if (blacklistToolsLength != 0) {
            for (int j = 0; j < blacklistToolsLength; j++) {
                blacklistTools[j] = buf.readString(buf.readInt());
            }
        }
        this.checkHardness = buf.readBoolean();
        this.isToolRequired = buf.readBoolean();
        this.invertBlockBlacklist = buf.readBoolean();
        this.invertToolBlacklist = buf.readBoolean();
        this.dontTakeDurability = buf.readBoolean();
    }

    public boolean equals(EasyExcavateConfig config) {
        return (config.maxBlocks == maxBlocks && config.maxRange == maxRange &&
                config.bonusExhaustionMultiplier == bonusExhaustionMultiplier &&
                config.enableBlockEntities == enableBlockEntities && Arrays.equals(config.blacklistBlocks, blacklistBlocks) &&
                Arrays.equals(config.blacklistTools, blacklistTools) && config.checkHardness == checkHardness &&
                config.isToolRequired == isToolRequired && config.invertBlockBlacklist == invertBlockBlacklist &&
                config.invertToolBlacklist == invertToolBlacklist && config.dontTakeDurability == dontTakeDurability);
    }
}
