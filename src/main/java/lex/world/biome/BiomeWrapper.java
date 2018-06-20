/*
 * LibEx
 * Copyright (c) 2017-2018 by MineEx
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

package lex.world.biome;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lex.config.Config;
import lex.world.gen.GenerationStage;
import lex.world.gen.feature.Feature;
import lex.world.gen.feature.FeatureRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;

public class BiomeWrapper
{
    protected Biome biome;
    protected int weight;
    protected Map<String, IBlockState> blocks = new HashMap<>();
    protected Map<GenerationStage, List<Feature>> generationStageFeatures = new HashMap<>();
    protected Map<EnumCreatureType, List<Biome.SpawnListEntry>> spawnableMobs = new HashMap<>();
    protected boolean enabled;
    protected boolean genDefaultFeatures;
    protected Config config;

    public BiomeWrapper(Config configIn)
    {
        config = configIn;
        parse();
    }

    private void parse()
    {
        biome = ForgeRegistries.BIOMES.getValue(config.getResource("biome"));
        weight = config.getInt("weight", 10);
        Config blockConfig = config.getDataBranch("blocks", new JsonObject());
        blockConfig.getBlock("topBlock", biome.topBlock);
        blockConfig.getBlock("fillerBlock", biome.fillerBlock);

        for(Map.Entry<String, JsonElement> entry : blockConfig.getAllData().entrySet())
        {
            if(blockConfig.getBlock(entry.getKey()) != null)
            {
                blocks.put(entry.getKey(), blockConfig.getBlock(entry.getKey()));
            }
        }

        List<Config> entityConfigs = config.getDataBranches("entities", new ArrayList<>());
        List<JsonObject> entityObjects = new ArrayList<>();

        for(EnumCreatureType creatureType : EnumCreatureType.values())
        {
            entryLoop:
            for(Biome.SpawnListEntry entry : biome.getSpawnableList(creatureType))
            {
                ResourceLocation entityName = ForgeRegistries.ENTITIES.getKey(EntityRegistry.getEntry(entry.entityClass));
                boolean containsEntry = false;

                Iterator<Config> configIter = entityConfigs.iterator();

                while(configIter.hasNext())
                {
                    Config entityConfig = configIter.next();

                    if(entityName != null && entityConfig.getString("entity").equals(entityName.toString()))
                    {
                        containsEntry = true;
                    }

                    entityObjects.add(entityConfig.serialize().getAsJsonObject());
                    configIter.remove();

                    if(containsEntry)
                    {
                        continue entryLoop;
                    }
                }

                JsonObject entityObject = new JsonObject();
                entityObject.addProperty("entity", ForgeRegistries.ENTITIES.getKey(EntityRegistry.getEntry(entry.entityClass)).toString());
                entityObject.addProperty("creatureType", creatureType.toString().toLowerCase());
                entityObject.addProperty("weight", entry.itemWeight);
                entityObject.addProperty("minGroupCount", entry.minGroupCount);
                entityObject.addProperty("maxGroupCount", entry.maxGroupCount);
                entityObject.addProperty("spawn", true);
                entityObjects.add(entityObject);
            }
        }

        config.removeData("entities");
        entityConfigs = config.getDataBranches("entities", entityObjects);

        for(Config entityConfig : entityConfigs)
        {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(entityConfig.getResource("entity"));

            if(entry != null && entityConfig.getBoolean("spawn", true))
            {
                Class<? extends Entity> entityCls = entry.getEntityClass();
                EnumCreatureType creatureType = entityConfig.getEnum("creatureType", EnumCreatureType.class);

                if(EntityLiving.class.isAssignableFrom(entityCls))
                {
                    spawnableMobs.computeIfAbsent(creatureType, k -> new ArrayList<>()).add(new Biome.SpawnListEntry((Class<? extends EntityLiving>) entityCls, entityConfig.getInt("weight", 10), entityConfig.getInt("minGroupCount", 1), entityConfig.getInt("maxGroupCount", 4)));
                }
            }
        }

        List<Config> featureConfigs = config.getDataBranches("features", new ArrayList<>());
        List<JsonObject> featureObjects = new ArrayList<>();

        for(Config featureConfig : featureConfigs)
        {
            Feature feature = FeatureRegistry.createFeature(featureConfig.getResource("feature"), featureConfig);

            if(feature != null && featureConfig.getBoolean("generate", true))
            {
                GenerationStage generationStage = featureConfig.getEnum("genStage", GenerationStage.class, GenerationStage.POST_DECORATE);
                generationStageFeatures.computeIfAbsent(generationStage, k -> new ArrayList<>()).add(feature);
            }

            featureObjects.add(featureConfig.serialize().getAsJsonObject());
        }

        config.removeData("features");
        config.getDataBranches("features", featureObjects);
        enabled = config.getBoolean("enabled", true);
        genDefaultFeatures = config.getBoolean("genDefaultFeatures", true);
    }

    public Biome getBiome()
    {
        return biome;
    }

    public IBlockState getBlock(String key, IBlockState fallbackValue)
    {
        IBlockState value = getBlock(key);

        if(value == null)
        {
            config.getDataBranch("blocks").getBlock(key, fallbackValue);
            blocks.put(key, fallbackValue);
            return fallbackValue;
        }

        return value;
    }

    public IBlockState getBlock(String key)
    {
        return blocks.get(key);
    }

    public List<IBlockState> getBlocks()
    {
        return ImmutableList.copyOf(blocks.values());
    }

    public List<Feature> getFeatures(GenerationStage generationStage)
    {
        return ImmutableList.copyOf(generationStageFeatures.computeIfAbsent(generationStage, k -> new ArrayList<>()));
    }

    public List<Biome.SpawnListEntry> getSpawnableMobs(EnumCreatureType creatureType)
    {
        return ImmutableList.copyOf(spawnableMobs.computeIfAbsent(creatureType, k -> new ArrayList<>()));
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public boolean shouldGenDefaultFeatures()
    {
        return genDefaultFeatures;
    }
}
