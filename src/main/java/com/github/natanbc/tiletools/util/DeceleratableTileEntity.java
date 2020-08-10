package com.github.natanbc.tiletools.util;

import net.minecraft.tileentity.TileEntity;

/**
 * Interface implemented by generated subclasses for frozen/slowed down
 * tile entities.
 *
 * Method names were chosen to avoid accidental overriding of superclass methods.
 *
 * Changes to this interface should be reflected in {@link TileEntityDecelerator}.
 */
public interface DeceleratableTileEntity {
    /**
     * @return The TileEntity that owns this decelerated tile.
     */
    TileEntity $tiletools_getOwner();
    
    /**
     * Sets whether or not this tile entity is frozen. Frozen tiles do not
     * tick at all.
     *
     * @param frozen Whether or not this tile is frozen.
     */
    void $tiletools_setFrozen(boolean frozen);
    
    /**
     * Sets the deceleration factor of this tile. A factor of N
     * means that this tile will only tick every N calls to tick()
     *
     * @param factor Deceleration factor.
     */
    void $tiletools_setFactor(int factor);
}
