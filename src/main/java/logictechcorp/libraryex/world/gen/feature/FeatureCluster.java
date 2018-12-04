/*
 * LibraryEx
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

package logictechcorp.libraryex.world.gen.feature;

import com.electronwill.nightconfig.core.Config;
import logictechcorp.libraryex.util.ConfigHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class FeatureCluster extends Feature
{
    private IBlockState blockToSpawn;
    private IBlockState blockToAttachTo;
    private EnumFacing direction;

    public FeatureCluster(Config config)
    {
        super(config);
        this.blockToSpawn = ConfigHelper.getBlockState(config, "blockToSpawn");
        this.blockToAttachTo = ConfigHelper.getBlockState(config, "blockToAttachTo");
        this.direction = ConfigHelper.getEnum(config, "direction", EnumFacing.class);
    }

    @Override
    public Config serialize()
    {
        Config config = super.serialize();
        config.add("direction", this.direction == null ? null : this.direction.toString().toLowerCase());
        ConfigHelper.setBlockState(config, "blockToAttachTo", this.blockToAttachTo);
        ConfigHelper.setBlockState(config, "blockToSpawn", this.blockToSpawn);
        return config;
    }

    public FeatureCluster(int genAttempts, double genProbability, boolean randomizeGenAttempts, int minGenHeight, int maxGenHeight, IBlockState blockToSpawn, IBlockState blockToAttachTo, EnumFacing direction)
    {
        super(genAttempts, genProbability, randomizeGenAttempts, minGenHeight, maxGenHeight);
        this.blockToSpawn = blockToSpawn;
        this.blockToAttachTo = blockToAttachTo;
        this.direction = direction;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos)
    {
        if(this.blockToSpawn == null || this.blockToAttachTo == null || this.direction == null)
        {
            return false;
        }

        if(!world.isAirBlock(pos))
        {
            return false;
        }
        else if(world.getBlockState(pos.offset(this.direction.getOpposite())) != this.blockToAttachTo)
        {
            return false;
        }
        else
        {
            world.setBlockState(pos, this.blockToSpawn, 3);

            for(int i = 0; i < 1500; i++)
            {
                BlockPos newPos;

                switch(this.direction)
                {
                    default:
                    case DOWN:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));
                        break;
                    case UP:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));
                        break;
                    case NORTH:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12));
                        break;
                    case SOUTH:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8), rand.nextInt(12));
                        break;
                    case WEST:
                        newPos = pos.add(-rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8));
                        break;
                    case EAST:
                        newPos = pos.add(rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8));
                        break;
                }

                if(world.isAirBlock(newPos))
                {
                    int j = 0;

                    for(EnumFacing facing : EnumFacing.values())
                    {
                        if(world.getBlockState(newPos.offset(facing)).getBlock() == this.blockToSpawn.getBlock())
                        {
                            j++;
                        }

                        if(j > 1)
                        {
                            break;
                        }
                    }

                    if(j == 1)
                    {
                        world.setBlockState(newPos, this.blockToSpawn, 3);
                    }
                }
            }

            return true;
        }
    }
}