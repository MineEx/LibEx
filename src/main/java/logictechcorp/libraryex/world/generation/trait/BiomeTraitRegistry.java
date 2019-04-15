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

package logictechcorp.libraryex.world.generation.trait;

import com.electronwill.nightconfig.core.Config;
import logictechcorp.libraryex.LibraryEx;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BiomeTraitRegistry
{
    private static final Map<ResourceLocation, Class<? extends IBiomeTraitConfigurable>> FEATURES = new HashMap<>();

    public static void registerFeature(ResourceLocation name, Class<? extends IBiomeTraitConfigurable> cls)
    {
        if(!FEATURES.containsKey(name))
        {
            FEATURES.put(name, cls);
        }
        else
        {
            LibraryEx.LOGGER.warn("A feature with the name, {}, is already registered!", name.toString());
        }
    }

    public static IBiomeTraitConfigurable createFeature(ResourceLocation name, Config config)
    {
        if(FEATURES.containsKey(name))
        {
            try
            {
                return FEATURES.get(name).getConstructor(Config.class).newInstance(config);
            }
            catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static ResourceLocation getTraitRegistryName(Class<? extends IBiomeTraitConfigurable> cls)
    {
        return FEATURES.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), cls)).map(Map.Entry::getKey).collect(Collectors.toList()).get(0);
    }

    static
    {
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":scatter"), BiomeTraitScatter.class);
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":cluster"), BiomeTraitCluster.class);
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":fluid"), BiomeTraitFluid.class);
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":ore"), BiomeTraitOre.class);
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":pool"), BiomeTraitPool.class);
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":big_mushroom"), BiomeTraitBigMushroom.class);
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":oak_tree"), BiomeTraitOakTree.class);
        registerFeature(new ResourceLocation(LibraryEx.MOD_ID + ":structure"), BiomeTraitStructure.class);
    }
}