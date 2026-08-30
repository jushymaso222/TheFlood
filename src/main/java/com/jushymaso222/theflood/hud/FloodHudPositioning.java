package com.jushymaso222.theflood.hud;

public final class FloodHudPositioning {

    private FloodHudPositioning() {
    }

    public record Position(
            int x,
            int y
    ) {
    }

    /**
     * Resolves the TOP-LEFT position of a HUD element.
     *
     * Offsets always move inward/outward relative to
     * the selected anchor naturally:
     *
     * LEFT anchors:
     *      +X moves right
     *
     * RIGHT anchors:
     *      +X moves left
     *
     * TOP anchors:
     *      +Y moves down
     *
     * BOTTOM anchors:
     *      +Y moves up
     *
     * CENTER anchors use ordinary signed offsets.
     */
    public static Position resolve(
            HudAnchor anchor,
            int screenWidth,
            int screenHeight,
            int elementWidth,
            int elementHeight,
            int xOffset,
            int yOffset
    ) {
        if (anchor == null) {
            anchor = HudAnchor.TOP_LEFT;
        }

        int x = switch (anchor) {

            case TOP_LEFT,
                 CENTER_LEFT,
                 BOTTOM_LEFT ->
                    xOffset;

            case TOP_CENTER,
                 CENTER,
                 BOTTOM_CENTER ->
                    screenWidth / 2
                            - elementWidth / 2
                            + xOffset;

            case TOP_RIGHT,
                 CENTER_RIGHT,
                 BOTTOM_RIGHT ->
                    screenWidth
                            - elementWidth
                            - xOffset;
        };

        int y = switch (anchor) {

            case TOP_LEFT,
                 TOP_CENTER,
                 TOP_RIGHT ->
                    yOffset;

            case CENTER_LEFT,
                 CENTER,
                 CENTER_RIGHT ->
                    screenHeight / 2
                            - elementHeight / 2
                            + yOffset;

            case BOTTOM_LEFT,
                 BOTTOM_CENTER,
                 BOTTOM_RIGHT ->
                    screenHeight
                            - elementHeight
                            - yOffset;
        };

        return new Position(
                x,
                y
        );
    }


    /**
     * Keeps a HUD element completely on-screen.
     *
     * Useful both at runtime and in the layout editor.
     */
    public static Position clampToScreen(
            Position position,
            int screenWidth,
            int screenHeight,
            int elementWidth,
            int elementHeight
    ) {
        int maxX =
                Math.max(
                        0,
                        screenWidth - elementWidth
                );

        int maxY =
                Math.max(
                        0,
                        screenHeight - elementHeight
                );

        int x =
                Math.max(
                        0,
                        Math.min(
                                maxX,
                                position.x()
                        )
                );

        int y =
                Math.max(
                        0,
                        Math.min(
                                maxY,
                                position.y()
                        )
                );

        return new Position(
                x,
                y
        );
    }


    public static Position resolveClamped(
            HudAnchor anchor,
            int screenWidth,
            int screenHeight,
            int elementWidth,
            int elementHeight,
            int xOffset,
            int yOffset
    ) {
        return clampToScreen(
                resolve(
                        anchor,
                        screenWidth,
                        screenHeight,
                        elementWidth,
                        elementHeight,
                        xOffset,
                        yOffset
                ),
                screenWidth,
                screenHeight,
                elementWidth,
                elementHeight
        );
    }
}