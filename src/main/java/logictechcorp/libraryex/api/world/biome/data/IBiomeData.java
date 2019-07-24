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

package logictechcorp.libraryex.api.world.biome.data;

import com.electronwill.nightconfig.core.Config;
import logictechcorp.libraryex.api.world.biome.IBiomeBlock;
import logictechcorp.libraryex.api.world.generation.IGeneratorStage;
import logictechcorp.libraryex.api.world.generation.trait.IBiomeTrait;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;

public interface IBiomeData
{
    /**
     * Called to write the current state of the biome data to its default config.
     * <p>
     * This should be called after the default values have been changed by a modder.
     * <p>
     * This should not be called after the biome data has been configured from an
     * external config because it may contain player edits.
     */
    void writeToDefaultConfig();

    /**
     * Called when the server is starting.
     * <p>
     * This is used to configure this biome data from a config.
     *
     * @param biomeDataAPI The biome data api that this biome data is registered to.
     * @param config       The config that belongs to the biome data.
     */
    void readFromConfig(IBiomeDataAPI biomeDataAPI, Config config);

    /**
     * Called when the server is stopping.
     * <p>
     * This is used to save this biome data to a config.
     *
     * @param config The config that belongs to the biome data.
     */
    void writeToConfig(Config config);

    /**
     * Called after {@link #writeToConfig}.
     * <p>
     * This is called to read the biome data from its default config.
     *
     * @param biomeDataAPI The biome data api that the biome data is registered to.
     */
    void readFromDefaultConfig(IBiomeDataAPI biomeDataAPI);

    /**
     * Called to check if the associated biome's default features should generate.
     *
     * @return Whether the associated biome's default features should generate.
     */
    boolean useDefaultBiomeDecorations();

    /**
     * Returns true if this data represents a sub biome.
     *
     * @return True if this data represents a sub biome.
     */
    boolean isSubBiomeData();

    /**
     * Called to check if the associated biome is enabled.
     *
     * @return Whether the associated biome is enabled.
     */
    boolean isBiomeEnabled();

    /**
     * Called to get the biome associated with this data.
     *
     * @return The biome associated with this data.
     */
    Biome getBiome();

    /**
     * Called to get the generation weight of the associated biome.
     *
     * @return The generation weight of the associated biome.
     */
    int getBiomeGenerationWeight();

    /**
     * Called to get a block that makes up the associated biome.
     *
     * @param type     The type of block to get.
     * @param fallback The block to fallback to if the biome doesn't have a block for the type.
     * @return A block that makes up the associated biome.
     */
    IBlockState getBiomeBlock(IBiomeBlock type, IBlockState fallback);

    /**
     * Called to get a map containing the biome blocks and their identifiers.
     *
     * @return A map containing the biome blocks and their identifiers.
     */
    Map<String, IBlockState> getBiomeBlocks();

    /**
     * Called to get a list of entities that spawn in the associated biome.
     *
     * @param creatureType The type of entity to get the list for.
     * @return A list of entities that spawn in the associated biome.
     */
    List<Biome.SpawnListEntry> getEntitySpawns(EnumCreatureType creatureType);

    /**
     * Called to get a list of biome traits that generate in the associated biome.
     *
     * @param generationStage The stage to get the list for.
     * @return A list of biome traits that generate in the associated biome.
     */
    List<IBiomeTrait> getBiomeTraits(IGeneratorStage generationStage);

    /**
     * Called to get a list of sub biomes that can generate in the associated biome.
     *
     * @return A list of sub biomes that can generate in the associated biome.
     */
    List<IBiomeData> getSubBiomeData();

    /**
     * Called to get this biome data's relative config path.
     *
     * @return This biome data's relative config path.
     */
    String getRelativeConfigPath();
}
