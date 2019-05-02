/*
 * LibraryEx
 * Copyright (c) 2017-2019 by LogicTechCorp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package logictechcorp.libraryex.world.biome.data.impl;

import com.electronwill.nightconfig.core.Config;
import logictechcorp.libraryex.api.IBiomeDataAPI;
import logictechcorp.libraryex.api.LibraryExAPI;
import logictechcorp.libraryex.config.ModJsonConfigFormat;
import logictechcorp.libraryex.utility.ConfigHelper;
import logictechcorp.libraryex.world.biome.data.iface.IBiomeData;
import logictechcorp.libraryex.world.biome.data.iface.IBiomeDataConfigurable;
import logictechcorp.libraryex.world.generation.GenerationStage;
import logictechcorp.libraryex.world.generation.trait.iface.IBiomeTrait;
import logictechcorp.libraryex.world.generation.trait.iface.IBiomeTraitBuilder;
import logictechcorp.libraryex.world.generation.trait.iface.IBiomeTraitConfigurable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The base class for biome data that can be configured from json.
 */
public class BiomeDataConfigurable extends BiomeData implements IBiomeDataConfigurable
{
    public BiomeDataConfigurable(Biome biome, int biomeGenerationWeight, boolean isSubBiomeData, boolean generateBiome, boolean generateDefaultBiomeFeatures)
    {
        super(biome, biomeGenerationWeight, isSubBiomeData, generateBiome, generateDefaultBiomeFeatures);
    }

    public BiomeDataConfigurable(ResourceLocation biomeRegistryName, int biomeGenerationWeight, boolean isSubBiomeData, boolean generateBiome, boolean generateDefaultBiomeFeatures)
    {
        super(ForgeRegistries.BIOMES.getValue(biomeRegistryName), biomeGenerationWeight, isSubBiomeData, generateBiome, generateDefaultBiomeFeatures);
    }

    public BiomeDataConfigurable(ResourceLocation biomeRegistryName)
    {
        super(biomeRegistryName);
    }

    @Override
    public void readFromConfig(IBiomeDataAPI biomeDataAPI, Config config)
    {
        this.biomeGenerationWeight = config.getOrElse("biomeGenerationWeight", this.biomeGenerationWeight);
        this.isSubBiomeData = config.getOrElse("isSubBiome", false);
        this.generateBiome = config.getOrElse("generateBiome", true);
        this.generateDefaultBiomeFeatures = config.getOrElse("generateDefaultBiomeFeatures", true);

        if(!(config.get("blocks") instanceof Config))
        {
            config.set("blocks", ModJsonConfigFormat.newConfig());
        }

        Config blocks = config.get("blocks");
        this.blocks.clear();

        for(Config.Entry entry : blocks.entrySet())
        {
            IBlockState state = ConfigHelper.getBlockState(config, "blocks." + entry.getKey());

            if(state != null)
            {
                this.blocks.put(entry.getKey(), state);
            }
        }

        if(!(config.get("entities") instanceof List))
        {
            config.set("entities", new ArrayList<Config>());
        }

        List<Config> entities = new ArrayList<>();
        Iterator entityConfigIter = ((List) config.get("entities")).iterator();
        this.entities.clear();

        for(EnumCreatureType type : EnumCreatureType.values())
        {
            entryLoop:
            for(Biome.SpawnListEntry entry : this.biome.getSpawnableList(type))
            {
                ResourceLocation registryKey = ForgeRegistries.ENTITIES.getKey(EntityRegistry.getEntry(entry.entityClass));
                boolean containsEntry = false;

                while(entityConfigIter.hasNext())
                {
                    Config entityConfig = (Config) entityConfigIter.next();

                    if(registryKey != null && entityConfig.get("entity") instanceof String && ((String) entityConfig.get("entity")).equalsIgnoreCase(registryKey.toString()))
                    {
                        containsEntry = true;
                    }

                    entities.add(entityConfig);
                    entityConfigIter.remove();

                    if(containsEntry)
                    {
                        continue entryLoop;
                    }
                }

                Config entityConfig = ModJsonConfigFormat.newConfig();
                entityConfig.add("entity", ForgeRegistries.ENTITIES.getKey(EntityRegistry.getEntry(entry.entityClass)).toString());
                entityConfig.add("spawnWeight", entry.itemWeight);
                entityConfig.add("minimumGroupCount", entry.minGroupCount);
                entityConfig.add("maximumGroupCount", entry.maxGroupCount);
                entityConfig.add("spawn", true);
                entities.add(entityConfig);
            }
        }

        config.set("entities", entities);

        for(Config entityConfig : entities)
        {
            EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityConfig.get("entity")));

            if(entityEntry != null && config.getOrElse("spawn", true))
            {
                Class<? extends Entity> cls = entityEntry.getEntityClass();
                EnumCreatureType creatureType = null;

                for(EnumCreatureType type : EnumCreatureType.values())
                {
                    if(type.getCreatureClass().isAssignableFrom(cls))
                    {
                        creatureType = type;
                        break;
                    }
                }

                if(creatureType != null && EntityLiving.class.isAssignableFrom(cls))
                {
                    this.entities.computeIfAbsent(creatureType, k -> new ArrayList<>()).add(new Biome.SpawnListEntry((Class<? extends EntityLiving>) cls, config.getOrElse("biomeGenerationWeight", 10), config.getOrElse("minGroupCount", 1), config.getOrElse("maxGroupCount", 4)));
                }
            }
        }

        if(!(config.get("traits") instanceof List))
        {
            config.set("traits", new ArrayList<Config>());
        }

        List<Config> biomeTraits = new ArrayList<>();
        List<Config> biomeTraitConfigs = config.get("traits");
        this.biomeTraits.clear();

        for(Config biomeTraitConfig : biomeTraitConfigs)
        {
            IBiomeTraitBuilder biomeTraitBuilder = LibraryExAPI.getInstance().getBiomeTraitRegistry().getBiomeTraitBuilder(new ResourceLocation(biomeTraitConfig.get("trait")));

            if(biomeTraitBuilder != null)
            {
                IBiomeTrait biomeTrait = biomeTraitBuilder.create();

                if(!(biomeTrait instanceof IBiomeTraitConfigurable))
                {
                    continue;
                }

                IBiomeTraitConfigurable biomeTraitConfigurable = (IBiomeTraitConfigurable) biomeTrait;
                biomeTraitConfigurable.readFromConfig(biomeTraitConfig);

                if(this.generateBiome)
                {
                    GenerationStage generationStage = biomeTraitConfig.getEnumOrElse("generationStage", GenerationStage.DECORATE);

                    if(generationStage != null)
                    {
                        this.biomeTraits.computeIfAbsent(generationStage, k -> new ArrayList<>()).add(biomeTraitConfigurable);
                    }
                    else
                    {
                        this.biomeTraits.computeIfAbsent(GenerationStage.POST_DECORATE, k -> new ArrayList<>()).add(biomeTraitConfigurable);
                    }
                }

            }

            biomeTraits.add(biomeTraitConfig);
        }

        config.set("traits", biomeTraits);

        if(!this.isSubBiomeData)
        {
            if(!(config.get("subBiomes") instanceof List))
            {
                config.set("subBiomes", new ArrayList<String>());
            }

            List<String> subBiomeNames = config.get("subBiomes");
            this.subBiomeData.clear();

            for(String subBiomeName : subBiomeNames)
            {
                Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(subBiomeName));

                if(biome != null)
                {
                    this.subBiomeData.add(biomeDataAPI.getBiomeDataRegistry().getBiomeData(biome));
                }
            }
        }
    }

    @Override
    public void writeToConfig(Config config)
    {
        config.add("biome", this.biome.getRegistryName().toString());
        config.add("biomeGenerationWeight", this.biomeGenerationWeight);
        config.add("isSubBiome", this.isSubBiomeData);
        config.add("generateBiome", this.generateBiome);
        config.add("generateDefaultBiomeFeatures", this.generateDefaultBiomeFeatures);
        Config blockConfigs = ModJsonConfigFormat.newConfig();

        for(Map.Entry<String, IBlockState> entry : this.blocks.entrySet())
        {
            ConfigHelper.setBlockState(blockConfigs, entry.getKey(), entry.getValue());
        }

        config.add("blocks", blockConfigs);
        List<Config> entityConfigs = new ArrayList<>();

        for(EnumCreatureType type : EnumCreatureType.values())
        {
            for(Biome.SpawnListEntry entry : this.getBiomeEntities(type))
            {
                ResourceLocation entityRegistryName = EntityList.getKey(entry.entityClass);

                if(entityRegistryName != null)
                {
                    Config entityConfig = ModJsonConfigFormat.newConfig();
                    entityConfig.add("entity", entityRegistryName.toString());
                    entityConfig.add("entitySpawnWeight", entry.itemWeight);
                    entityConfig.add("minimumGroupCount", entry.minGroupCount);
                    entityConfig.add("maximumGroupCount", entry.maxGroupCount);
                    entityConfig.add("spawn", true);
                    entityConfigs.add(entityConfig);
                }
            }
        }

        config.add("entities", entityConfigs);
        List<Config> biomeTraitConfigs = new ArrayList<>();

        for(GenerationStage stage : GenerationStage.values())
        {
            for(IBiomeTrait biomeTrait : this.getBiomeTraits(stage))
            {
                if(!(biomeTrait instanceof IBiomeTraitConfigurable))
                {
                    continue;
                }

                Config biomeTraitConfig = ModJsonConfigFormat.newConfig();
                ((IBiomeTraitConfigurable) biomeTrait).writeToConfig(biomeTraitConfig);
                biomeTraitConfig.add("generationStage", stage.toString().toLowerCase());
                biomeTraitConfigs.add(biomeTraitConfig);
            }
        }

        config.add("traits", biomeTraitConfigs);

        if(!this.isSubBiomeData)
        {
            List<String> subBiomeNames = new ArrayList<>();

            for(IBiomeData biomeData : this.subBiomeData)
            {
                if(biomeData.getBiome().getRegistryName() != null)
                {
                    subBiomeNames.add(biomeData.getBiome().getRegistryName().toString());
                }
            }

            config.add("subBiomes", subBiomeNames);
        }
    }

    @Override
    public String getRelativeSaveFile()
    {
        return "biomes/" + this.biome.getRegistryName().toString().replace(":", "/") + ".json";
    }
}
