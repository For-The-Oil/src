package io.github.shared.local.shared_engine.manager;

import io.github.shared.local.data.EnumsTypes.CellType;
import io.github.shared.local.data.gameobject.Cell;
import io.github.shared.local.data.gameobject.Shape;

public class ShapeManager {

    /**
     * Overlays 'overlay' onto 'base' in-place.
     * The top-left of 'overlay' (0,0) is placed at (baseX, baseY) in 'base'.
     *
     * @param base    The destination Shape (modified in-place).
     * @param overlay The source Shape placed on top of the base.
     * @param baseX   X offset where overlay(0,0) maps on base.
     * @param baseY   Y offset where overlay(0,0) maps on base.
     */
    public static void overlayShape(Shape base, Shape overlay, int baseX, int baseY) {
        if (base == null || overlay == null) return;

        final int baseW = base.getWidth();
        final int baseH = base.getHeight();
        final int overW = overlay.getWidth();
        final int overH = overlay.getHeight();

        // Iterate in overlay local coordinates (x: 0..overW-1, y: 0..overH-1)
        for (int x = 0; x < overW; x++) {
            for (int y = 0; y < overH; y++) {

                // Map overlay (x,y) to base (bx,by)
                final int bx = baseX + x;
                final int by = baseY + y;

                // Clip: skip if outside base bounds
                if (bx < 0 || by < 0 || bx >= baseW || by >= baseH) {
                    continue;
                }

                // With getCells(x, y) = tab_cells[y][x]
                final Cell src = overlay.getCells(x, y);
                final Cell dst = base.getCells(bx, by);
                if (src == null || dst == null) {
                    continue;
                }

                final CellType srcType = src.getCellType();
                final CellType dstType = dst.getCellType();

                // Copy only if both are non-VOID
                if (srcType != CellType.VOID && dstType != CellType.VOID) {
                    dst.setCellType(srcType);
                    dst.setNetId(src.getNetId());
                }
            }
        }
    }


}
