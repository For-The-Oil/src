package io.github.shared.shared_engine.manager;

import java.util.ArrayList;

import io.github.shared.data.EnumsTypes.CellType;
import io.github.shared.data.EnumsTypes.Direction;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;

public class ShapeManager {

    /**
     * Overlays a sub-region of 'overlay' onto 'base' in-place.
     * The rectangle (overlayX, overlayY, overlayWidth, overlayHeight) in 'overlay'
     * is placed with its top-left corner at (baseX, baseY) in 'base'.
     *
     * @param base          The destination Shape (modified in-place).
     * @param overlay       The source Shape.
     * @param baseX         X position in base where the overlay region starts.
     * @param baseY         Y position in base where the overlay region starts.
     * @param overlayX      X position in overlay where the region starts.
     * @param overlayY      Y position in overlay where the region starts.
     * @param overlayWidth  Width of the region to copy.
     * @param overlayHeight Height of the region to copy.
     */
    public static void overlayShape(Shape base, Shape overlay, int baseX, int baseY, int overlayX, int overlayY, int overlayWidth, int overlayHeight) {
        if (base == null || overlay == null) return;

        final int baseW = base.getWidth();
        final int baseH = base.getHeight();
        final int overW = overlay.getWidth();
        final int overH = overlay.getHeight();

        // Clamp region to overlay bounds
        int maxW = Math.min(overlayWidth, overW - overlayX);
        int maxH = Math.min(overlayHeight, overH - overlayY);

        for (int x = 0; x < maxW; x++) {
            for (int y = 0; y < maxH; y++) {
                int srcX = overlayX + x;
                int srcY = overlayY + y;
                int dstX = baseX + x;
                int dstY = baseY + y;

                if (dstX < 0 || dstY < 0 || dstX >= baseW || dstY >= baseH) continue;

                final Cell src = overlay.getCells(srcX, srcY);
                final Cell dst = base.getCells(dstX, dstY);
                if (src == null || dst == null) continue;

                final CellType srcType = src.getCellType();
                final CellType dstType = dst.getCellType();

                if (srcType != CellType.VOID && dstType != CellType.VOID) {
                    dst.setCellType(srcType);
                    dst.setNetId(src.getNetId());
                }
            }
        }
    }

    /**
     * Returns true if the overlay sub-rectangle can be applied on base without exceeding base bounds
     * and only replacing destination cells whose type is included in canBePlacedOn.
     *
     * Conditions:
     *  - Entire destination rectangle [baseX..baseX+overlayWidth-1, baseY..baseY+overlayHeight-1]
     *    must lie within base.
     *  - Source rectangle [overlayX..overlayX+overlayWidth-1, overlayY..overlayY+overlayHeight-1]
     *    must lie within overlay.
     *  - For every (src != VOID) cell, the destination cellType must be in canBePlacedOn.
     */
    public static boolean canOverlayShape(Shape base, Shape overlay, int baseX, int baseY, int overlayX, int overlayY, int overlayWidth, int overlayHeight, ArrayList<CellType> canBePlacedOn) {
        if (base == null || overlay == null || canBePlacedOn == null) return false;

        final int baseW = base.getWidth();
        final int baseH = base.getHeight();
        final int overW = overlay.getWidth();
        final int overH = overlay.getHeight();

        // Reject if source sub-rectangle is outside overlay bounds
        if (overlayX < 0 || overlayY < 0 ||
            overlayWidth < 0 || overlayHeight < 0 ||
            overlayX + overlayWidth > overW ||
            overlayY + overlayHeight > overH) {
            return false;
        }

        // Reject if destination rectangle exceeds base bounds (no partial allowed)
        if (baseX < 0 || baseY < 0 ||
            baseX + overlayWidth > baseW ||
            baseY + overlayHeight > baseH) {
            return false;
        }

        // Check every cell that would be replaced
        for (int dx = 0; dx < overlayWidth; dx++) {
            for (int dy = 0; dy < overlayHeight; dy++) {
                final int srcX = overlayX + dx;
                final int srcY = overlayY + dy;
                final int dstX = baseX + dx;
                final int dstY = baseY + dy;

                final Cell src = overlay.getCells(srcX, srcY);
                final Cell dst = base.getCells(dstX, dstY);
                if (src == null || dst == null) {
                    // Defensive: if shapes guarantee non-null in-bounds, you could return false here
                    return false;
                }

                final CellType srcType = src.getCellType();   // VOID means "no replacement"
                if (srcType != CellType.VOID) {               // only replaced cells are constrained
                    final CellType dstType = dst.getCellType();
                    if (!canBePlacedOn.contains(dstType)) {
                        return false; // destination cell type is not allowed to be replaced
                    }
                }
            }
        }
        return true;
    }

    /**
     * Rotates a Shape based on the given Direction.
     * The original Shape is assumed to be oriented NORTH by default.
     *
     * @param original  The original Shape to rotate.
     * @param direction The target Direction (NORTH, EAST, SOUTH, WEST).
     * @return A new Shape rotated according to the specified direction.
     */
    public static Shape rotateShape(Shape original, Direction direction) {
        if (original == null) {
            return null;
        }

        Cell[][] cells = original.getTab_cells();
        int width = original.getWidth();
        int height = original.getHeight();
        Cell[][] rotated;

        switch (direction) {
            case NORTH:
                // No rotation needed, copy the original matrix
                return new Shape(original);
            case EAST:
                // Rotate 90° clockwise
                rotated = new Cell[width][height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        rotated[x][height - 1 - y] = cells[y][x];
                    }
                }
                break;
            case SOUTH:
                // Rotate 180°
                rotated = new Cell[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        rotated[height - 1 - y][width - 1 - x] = cells[y][x];
                    }
                }
                break;
            case WEST:
                // Rotate 90° counterclockwise
                rotated = new Cell[width][height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        rotated[width - 1 - x][y] = cells[y][x];
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }

        return new Shape(rotated);
    }


}
