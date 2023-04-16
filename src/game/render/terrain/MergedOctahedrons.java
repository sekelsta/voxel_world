package sekelsta.game.render.terrain;

import java.util.*;
import sekelsta.engine.render.*;
import sekelsta.game.terrain.*;
import sekelsta.tools.Vertex;
import shadowfox.math.Vector2f;
import shadowfox.math.Vector3f;


public class MergedOctahedrons {
    private final Terrain terrain;
    private final TerrainModelData model;

    private HashMap<BlockPos, Vertex> vertexMap = new HashMap<>();

    public MergedOctahedrons(Terrain terrain) {
        this.terrain = terrain;
        this.model = new TerrainModelData();
    }

    private void addFacing(
        short lowerFrontLeft,
        short upperFrontLeft,
        short lowerBackLeft,
        short upperBackLeft,
        short lowerFrontRight,
        short upperFrontRight,
        short lowerBackRight,
        short upperBackRight,
        Vertex center,
        Vertex frontLeft,
        Vertex frontRight,
        Vertex backLeft,
        Vertex backRight,
        Vertex lowerLeft,
        Vertex lowerRight,
        Vertex upperLeft,
        Vertex upperRight,
        Vertex lowerFront,
        Vertex lowerBack,
        Vertex upperFront,
        Vertex upperBack, 
        boolean lowerFrontLeftInChunk,
        boolean lowerFrontRightInChunk,
        boolean lowerBackRightInChunk,
        boolean lowerBackLeftInChunk,
        Vector2f stepUV0,
        Vector2f stepUV1,
        Vector2f stepUV2,
        Vector2f stepUV3,
        boolean edgesVertical)
    {

        Vector2f uv_lower_left = new Vector2f(0.5f, 0.5f);
        Vector2f uv_lower_right = new Vector2f(1.5f, 0.5f);
        Vector2f uv_upper_left = new Vector2f(0.5f, 1.5f);
        Vector2f uv_upper_right = new Vector2f(1.5f, 1.5f);

        if (!isOpaque(upperFrontLeft) && !isOpaque(upperBackLeft)
                && !isOpaque(upperFrontRight) && !isOpaque(upperBackRight) 
                && isOpaque(lowerFrontLeft) && isOpaque(lowerBackLeft)
                && isOpaque(lowerFrontRight) && isOpaque(lowerBackRight)) {
            model.addBoundedUVQuad(frontLeft, frontRight, backRight, backLeft, lowerFrontLeftInChunk,
                uv_lower_left, uv_lower_right, uv_upper_right, uv_upper_left);
        }

        if (edgesVertical) {
            // Twelve-fold symmetry, standard orientation
            if (isOpaque(lowerFrontLeft) && !isOpaque(upperFrontLeft)) {
                if (isOpaque(upperBackLeft)) {
                    if (!isOpaque(upperFrontRight) && isOpaque(lowerFrontRight) && isOpaque(upperBackRight)) {
                        // Ramp
                        model.addBoundedQuad(upperLeft, frontLeft, frontRight, upperRight, lowerFrontLeftInChunk);
                    }
                }
                else if (isOpaque(lowerBackLeft)) {
                    if (!isOpaque(upperFrontRight) && !isOpaque(lowerFrontRight) 
                            && !isOpaque(upperBackRight) && !isOpaque(lowerBackRight)) {
                        // Edge
                        model.addBoundedQuad(frontLeft, lowerFront, lowerBack, backLeft, lowerFrontLeftInChunk);
                    }
                }
            }

            // Twelve-fold symmetry, rotate 180 degrees about Z
            if (isOpaque(lowerBackRight) && !isOpaque(upperBackRight)) {
                if (isOpaque(upperFrontRight)) {
                    if (!isOpaque(upperBackLeft) && isOpaque(lowerBackLeft) && isOpaque(upperFrontLeft)) {
                        // Ramp
                        model.addBoundedQuad(upperRight, backRight, backLeft, upperLeft, lowerBackRightInChunk);
                    }
                }
                else if (isOpaque(lowerFrontRight)) {
                    if (!isOpaque(upperBackLeft) && !isOpaque(lowerBackLeft) 
                            && !isOpaque(upperFrontLeft) && !isOpaque(lowerFrontLeft)) {
                        // Edge
                        model.addBoundedQuad(backRight, lowerBack, lowerFront, frontRight, lowerBackRightInChunk);
                    }
                }
            }
        }
        else {
            // Twelve-fold symmetry, rotate 90 degrees about z
            if (isOpaque(lowerBackLeft) && !isOpaque(upperBackLeft)) {
                if (isOpaque(upperBackRight)) {
                    if (!isOpaque(upperFrontLeft) && isOpaque(lowerFrontLeft) && isOpaque(upperFrontRight)) {
                        // Ramp
                        model.addBoundedQuad(upperBack, backLeft, frontLeft, upperFront, lowerBackLeftInChunk);
                    }
                }
                else if (isOpaque(lowerBackRight)) {
                    if (!isOpaque(upperFrontLeft) && !isOpaque(lowerFrontLeft) 
                            && !isOpaque(upperFrontRight) && !isOpaque(lowerFrontRight)) {
                        // Edge
                        model.addBoundedQuad(backLeft, lowerLeft, lowerRight, backRight, lowerBackLeftInChunk);
                    }
                }
            }

            // Twelve-fold symmetry, rotate 270 degrees about Z
            if (isOpaque(lowerFrontRight) && !isOpaque(upperFrontRight)) {
                if (isOpaque(upperFrontLeft)) {
                    if (!isOpaque(upperBackRight) && isOpaque(lowerBackRight) && isOpaque(upperBackLeft)) {
                        // Ramp
                        model.addBoundedQuad(upperFront, frontRight, backRight, upperBack, lowerFrontRightInChunk);
                    }
                }
                else if (isOpaque(lowerFrontLeft)) {
                    if (!isOpaque(upperBackRight) && !isOpaque(lowerBackRight) 
                            && !isOpaque(upperBackLeft) && !isOpaque(lowerBackLeft)) {
                        // Edge
                        model.addBoundedQuad(frontRight, lowerRight, lowerLeft, frontLeft, lowerFrontRightInChunk);
                    }
                }
            }
        }


        // Minimizes y = (x - sqrt(.5))^2 + ((1 - x) * sqrt(2) - sqrt(.5))^2 + (sqrt(1 + x^2) - sqrt(11/8))^2
        // Where y measures distortion when flattening a specific three triangles onto a 2D texture
        float magic_val = 0.57241f;

        // 24-fold symmetry, standard orientation
        Vector2f magic_top_right = new Vector2f(0.5f + magic_val, 1.5f);
        Vector2f magic_right_above = new Vector2f(1.5f, 0.5f + magic_val);
        if (isOpaque(lowerFrontLeft) && !isOpaque(upperFrontLeft)) {
            if (isOpaque(upperBackRight)) {
                // Upper diagonal
                if (!isOpaque(upperBackLeft) && !isOpaque(upperFrontRight)) {
                    model.addBoundedUVTriangle(frontLeft, upperRight, upperBack, lowerFrontRightInChunk,
                        uv_lower_left, magic_right_above, magic_top_right);
                    if (isOpaque(lowerBackLeft)) {
                        model.addBoundedUVTriangle(frontLeft, upperBack, backLeft, lowerFrontRightInChunk,
                            uv_lower_left, magic_top_right, uv_upper_left);
                    }
                    if (isOpaque(lowerFrontRight)) {
                        model.addBoundedUVTriangle(frontLeft, frontRight, upperRight, lowerFrontRightInChunk,
                            uv_lower_left, uv_lower_right, magic_right_above);
                    }
                }
            }
            else if (isOpaque(upperBackLeft)) {
                // Step up
                if (!isOpaque(upperFrontRight) && !isOpaque(lowerFrontRight) && !isOpaque(lowerBackRight)) {
                    model.addBoundedUVQuad(frontLeft, lowerFront, upperBack, upperLeft, lowerFrontLeftInChunk, 
                        stepUV0, stepUV1, stepUV2, stepUV3);
                }
            }
            else if (isOpaque(upperFrontRight)) {
                // Cutoff
                if (!isOpaque(lowerBackLeft) && isOpaque(lowerBackRight)) {
                    center.type = lowerFrontLeft;
                    model.addBoundedTriangle(frontLeft, center, lowerLeft, lowerFrontLeftInChunk);
                    model.addBoundedTriangle(frontLeft, upperFront, center, lowerFrontLeftInChunk);
                }
            }
            else if (isOpaque(lowerBackLeft) && isOpaque(lowerFrontRight) && !isOpaque(lowerBackRight)) {
                model.addBoundedUVTriangle(backLeft, frontLeft, frontRight, lowerFrontLeftInChunk,
                    uv_upper_left, uv_lower_left, uv_lower_right);
            }
        }

        // 24-fold symmetry, rotate 90 degrees about Z
        Vector2f magic_top_left = new Vector2f(1.5f - magic_val, 1.5f);
        Vector2f magic_left_above = new Vector2f(0.5f, 0.5f + magic_val);
        if (isOpaque(lowerFrontRight) && !isOpaque(upperFrontRight)) {
            if (isOpaque(upperBackLeft)) {
                // Upper diagonal
                if (!isOpaque(upperFrontLeft) && !isOpaque(upperBackRight)) {
                    model.addBoundedUVTriangle(frontRight, upperBack, upperLeft, lowerBackRightInChunk,
                        uv_lower_right, magic_top_left, magic_left_above);
                    if (isOpaque(lowerFrontLeft)) {
                        model.addBoundedUVTriangle(frontRight, upperLeft, frontLeft, lowerBackRightInChunk,
                            uv_lower_right, magic_left_above, uv_lower_left);
                    }
                    if (isOpaque(lowerBackRight)) {
                        model.addBoundedUVTriangle(frontRight, backRight, upperBack, lowerBackRightInChunk,
                            uv_lower_right, uv_upper_right, magic_top_left);
                    }
                }
            }
            else if (isOpaque(upperFrontLeft)) {
                // Step up
                if (!isOpaque(upperBackRight) && !isOpaque(lowerBackRight) && !isOpaque(lowerBackLeft)) {
                    model.addBoundedUVQuad(frontRight, lowerRight, upperLeft, upperFront, lowerFrontRightInChunk,
                        stepUV0, stepUV1, stepUV2, stepUV3);
                }
            }
            else if (isOpaque(upperBackRight)) {
                // Cutoff
                if (!isOpaque(lowerFrontLeft) && isOpaque(lowerBackLeft)) {
                    center.type = lowerFrontRight;
                    model.addBoundedTriangle(frontRight, center, lowerFront, lowerFrontRightInChunk);
                    model.addBoundedTriangle(frontRight, upperRight, center, lowerFrontRightInChunk);
                }
            }
            else if (isOpaque(lowerFrontLeft) && isOpaque(lowerBackRight) && !isOpaque(lowerBackLeft)) {
                model.addBoundedUVTriangle(frontLeft, frontRight, backRight, lowerFrontRightInChunk,
                    uv_lower_left, uv_lower_right, uv_upper_right);
            }
        }

        // 24-fold symmetry, rotate 180 degrees about Z
        Vector2f magic_bottom_left = new Vector2f(1.5f - magic_val, 0.5f);
        Vector2f magic_left_below = new Vector2f(0.5f, 1.5f - magic_val);
        if (isOpaque(lowerBackRight) && !isOpaque(upperBackRight)) {
            if (isOpaque(upperFrontLeft)) {
                // Upper diagonal
                if (!isOpaque(upperFrontRight) && !isOpaque(upperBackLeft)) {
                    model.addBoundedUVTriangle(backRight, upperLeft, upperFront, lowerBackLeftInChunk,
                        uv_upper_right, magic_left_below, magic_bottom_left);
                    if (isOpaque(lowerFrontRight)) {
                        model.addBoundedUVTriangle(backRight, upperFront, frontRight, lowerBackLeftInChunk,
                            uv_upper_right, magic_bottom_left, uv_lower_right);
                    }
                    if (isOpaque(lowerBackLeft)) {
                        model.addBoundedUVTriangle(backRight, backLeft, upperLeft, lowerBackLeftInChunk,
                            uv_upper_right, uv_upper_left, magic_left_below);
                    }
                }
            }
            else if (isOpaque(upperFrontRight)) {
                // Step up
                if (!isOpaque(upperBackLeft) && !isOpaque(lowerBackLeft) && !isOpaque(lowerFrontLeft)) {
                    model.addBoundedUVQuad(backRight, lowerBack, upperFront, upperRight, lowerBackRightInChunk,
                        stepUV0, stepUV1, stepUV2, stepUV3);
                }
            }
            else if (isOpaque(upperBackLeft)) {
                // Cutoff
                if (!isOpaque(lowerFrontRight) && isOpaque(lowerFrontLeft)) {
                    center.type = lowerBackRight;
                    model.addBoundedTriangle(backRight, center, lowerRight, lowerBackRightInChunk);
                    model.addBoundedTriangle(backRight, upperBack, center, lowerBackRightInChunk);
                }
            }
            else if (isOpaque(lowerFrontRight) && isOpaque(lowerBackLeft) && !isOpaque(lowerFrontLeft)) {
                model.addBoundedUVTriangle(frontRight, backRight, backLeft, lowerBackRightInChunk,
                    uv_lower_right, uv_upper_right, uv_upper_left);
            }
        }

        // 24-fold symmetry, rotate 270 degrees about Z
        Vector2f magic_bottom_right = new Vector2f(0.5f + magic_val, 0.5f);
        Vector2f magic_right_below = new Vector2f(1.5f, 1.5f - magic_val);
        if (isOpaque(lowerBackLeft) && !isOpaque(upperBackLeft)) {
            if (isOpaque(upperFrontRight)) {
                // Upper diagonal
                if (!isOpaque(upperBackRight) && !isOpaque(upperFrontLeft)) {
                    model.addBoundedUVTriangle(backLeft, upperFront, upperRight, lowerFrontLeftInChunk,
                        uv_upper_left, magic_bottom_right, magic_right_below);
                    if (isOpaque(lowerBackRight)) {
                        model.addBoundedUVTriangle(backLeft, upperRight, backRight, lowerFrontLeftInChunk,
                            uv_upper_left, magic_right_below, uv_upper_right);
                    }
                    if (isOpaque(lowerFrontLeft)) {
                        model.addBoundedUVTriangle(backLeft, frontLeft, upperFront, lowerFrontLeftInChunk,
                            uv_upper_left, uv_lower_left, magic_bottom_right);
                    }
                }
            }
            else if (isOpaque(upperBackRight)) {
                // Step up
                if (!isOpaque(upperFrontLeft) && !isOpaque(lowerFrontLeft) && !isOpaque(lowerFrontRight)) {
                    model.addBoundedUVQuad(backLeft, lowerLeft, upperRight, upperBack, lowerBackLeftInChunk,
                        stepUV0, stepUV1, stepUV2, stepUV3);
                }
            }
            else if (isOpaque(upperFrontLeft)) {
                // Cutoff
                if (!isOpaque(lowerBackRight) && isOpaque(lowerFrontRight)) {
                    center.type = lowerBackLeft;
                    model.addBoundedTriangle(backLeft, center, lowerBack, lowerBackLeftInChunk);
                    model.addBoundedTriangle(backLeft, upperLeft, center, lowerBackLeftInChunk);
                }
            }
            else if (isOpaque(lowerBackRight) && isOpaque(lowerFrontLeft) && !isOpaque(lowerFrontRight)) {
                model.addBoundedUVTriangle(backRight, backLeft, frontLeft, lowerBackLeftInChunk,
                    uv_upper_right, uv_upper_left, uv_lower_left);
            }
        }
    }

    // Look at a 2x2x2 group of blocks, and add geometry to the center region
    private void addGeometry(int chunkX, int chunkY, int chunkZ, int x, int y, int z, boolean isChunk) {
        int bx = chunkX * Chunk.SIZE + x;
        int by = chunkY * Chunk.SIZE + y;
        int bz = chunkZ * Chunk.SIZE + z;
        short lowerFrontLeft = terrain.getBlockIfLoaded(bx, by, bz);
        short upperFrontLeft = terrain.getBlockIfLoaded(bx, by, bz + 1);
        short lowerBackLeft = terrain.getBlockIfLoaded(bx, by + 1, bz);
        short upperBackLeft = terrain.getBlockIfLoaded(bx, by + 1, bz + 1);
        short lowerFrontRight = terrain.getBlockIfLoaded(bx + 1, by, bz);
        short upperFrontRight = terrain.getBlockIfLoaded(bx + 1, by, bz + 1);
        short lowerBackRight = terrain.getBlockIfLoaded(bx + 1, by + 1, bz);
        short upperBackRight = terrain.getBlockIfLoaded(bx + 1, by + 1, bz + 1);

        Vertex center = findVertex(2 * x + 2, 2 * y + 2, 2 * z + 2); // Coords doubled for int bins
        center.texture = new Vector2f(0.5f, 0.5f);
        // XY plane
        Vertex frontLeft = setupVertex(lowerFrontLeft, upperFrontLeft, x, y, z, Direction.UP);
        Vertex frontRight = setupVertex(lowerFrontRight, upperFrontRight, x + 1, y, z, Direction.UP);
        Vertex backLeft = setupVertex(lowerBackLeft, upperBackLeft, x, y + 1, z, Direction.UP);
        Vertex backRight = setupVertex(lowerBackRight, upperBackRight, x + 1, y + 1, z, Direction.UP);
        // XZ plane
        Vertex lowerLeft = setupVertex(lowerFrontLeft, lowerBackLeft, x, y, z, Direction.NORTH);
        Vertex lowerRight = setupVertex(lowerFrontRight, lowerBackRight, x + 1, y, z, Direction.NORTH);
        Vertex upperLeft = setupVertex(upperFrontLeft, upperBackLeft, x, y, z + 1, Direction.NORTH);
        Vertex upperRight = setupVertex(upperFrontRight, upperBackRight, x + 1, y, z + 1, Direction.NORTH);
        // YZ plane
        Vertex lowerFront = setupVertex(lowerFrontLeft, lowerFrontRight, x, y, z, Direction.EAST);
        Vertex lowerBack = setupVertex(lowerBackLeft, lowerBackRight, x, y + 1, z, Direction.EAST);
        Vertex upperFront = setupVertex(upperFrontLeft, upperFrontRight, x, y, z + 1, Direction.EAST);
        Vertex upperBack = setupVertex(upperBackLeft, upperBackRight, x, y + 1, z + 1, Direction.EAST);

        boolean allAir = !isOpaque(lowerFrontLeft) && !isOpaque(upperFrontLeft)
                        && !isOpaque(lowerBackLeft) && !isOpaque(upperBackLeft)
                        && !isOpaque(lowerFrontRight) && !isOpaque(upperFrontRight)
                        && !isOpaque(lowerBackRight) && !isOpaque(upperBackRight);

        boolean allSolid = isOpaque(lowerFrontLeft) && isOpaque(upperFrontLeft)
                        && isOpaque(lowerBackLeft) && isOpaque(upperBackLeft)
                        && isOpaque(lowerFrontRight) && isOpaque(upperFrontRight)
                        && isOpaque(lowerBackRight) && isOpaque(upperBackRight);

        if (allAir || allSolid) {
            return;
        }

        boolean lowerFrontLeftInChunk;
        boolean lowerFrontRightInChunk;
        boolean lowerBackLeftInChunk;
        boolean lowerBackRightInChunk;
        boolean upperFrontLeftInChunk;
        boolean upperFrontRightInChunk;
        boolean upperBackLeftInChunk;
        boolean upperBackRightInChunk;
        if (isChunk) {
            lowerFrontLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE && z >= 0 && z < Chunk.SIZE;
            lowerFrontRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE && z >= 0 && z < Chunk.SIZE;
            lowerBackLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE && z >= 0 && z < Chunk.SIZE;
            lowerBackRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE && z >= 0 && z < Chunk.SIZE;
            upperFrontLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE && z + 1 >= 0 && z + 1 < Chunk.SIZE;
            upperFrontRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE && z + 1 >= 0 && z + 1 < Chunk.SIZE;
            upperBackLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE && z + 1 >= 0 && z + 1 < Chunk.SIZE;
            upperBackRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE && z + 1 >= 0 && z + 1 < Chunk.SIZE;
        }
        else {
            lowerFrontLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE;
            lowerFrontRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE;
            lowerBackLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE;
            lowerBackRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE;
            upperFrontLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE && z + 1 >= 0;
            upperFrontRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y >= 0 && y < Chunk.SIZE && z + 1 >= 0;
            upperBackLeftInChunk = x >= 0 && x < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE && z + 1 >= 0;
            upperBackRightInChunk = x + 1 >= 0 && x + 1 < Chunk.SIZE 
                && y + 1 >= 0 && y + 1 < Chunk.SIZE && z + 1 >= 0;
        }

        // Eight-way symmetrical parts
        Vector2f tri_left = new Vector2f(1 - 0.5f * (float)Math.sqrt(.5), 0.5f);
        Vector2f tri_right = new Vector2f(1 + 0.5f * (float)Math.sqrt(.5), 0.5f);
        Vector2f tri_middle_lower = new Vector2f(1f, 0.5f - 0.5f * (float)Math.sqrt(1.5));
        Vector2f tri_middle_upper = new Vector2f(1f, 0.5f + 0.5f * (float)Math.sqrt(1.5));

        // Standard orientation
        if (isOpaque(lowerFrontLeft) && !isOpaque(upperFrontLeft)) {
            if (isOpaque(upperBackLeft)) {
                if (isOpaque(upperFrontRight)) {
                    // Chipped block
                    model.addBoundedUVTriangle(frontLeft, upperFront, upperLeft, lowerFrontLeftInChunk, 
                        tri_middle_lower, tri_right, tri_left);
                }
            }
            else if (!isOpaque(lowerBackLeft) && !isOpaque(upperFrontRight) && !isOpaque(lowerFrontRight)
                && !isOpaque(upperBackRight) && !isOpaque(lowerBackRight))
            {
                // Single tri
                model.addBoundedUVTriangle(frontLeft, lowerFront, lowerLeft, lowerFrontLeftInChunk, 
                    tri_middle_upper, tri_left, tri_right);
            }
        }

        // Rotate 90 degrees about Z
        if (isOpaque(lowerFrontRight) && !isOpaque(upperFrontRight)) {
            if (isOpaque(upperFrontLeft)) {
                if (isOpaque(upperBackRight)) {
                    // Chipped block
                    model.addBoundedUVTriangle(frontRight, upperRight, upperFront, lowerFrontRightInChunk, 
                        tri_middle_lower, tri_right, tri_left);
                }
            }
            else if (!isOpaque(lowerFrontLeft) && !isOpaque(upperBackRight) && !isOpaque(lowerBackRight)
                && !isOpaque(upperBackLeft) && !isOpaque(lowerBackLeft))
            {
                // Single tri
                model.addBoundedUVTriangle(frontRight, lowerRight, lowerFront, lowerFrontRightInChunk, 
                    tri_middle_upper, tri_left, tri_right);
            }
        }

        // Rotate 180 degrees about Z
        if (isOpaque(lowerBackRight) && !isOpaque(upperBackRight)) {
            if (isOpaque(upperFrontRight)) {
                if (isOpaque(upperBackLeft)) {
                    // Chipped block
                    model.addBoundedUVTriangle(backRight, upperBack, upperRight, lowerBackRightInChunk, 
                        tri_middle_lower, tri_right, tri_left);
                }
            }
            else if (!isOpaque(lowerFrontRight) && !isOpaque(upperBackLeft) && !isOpaque(lowerBackLeft)
                && !isOpaque(upperFrontLeft) && !isOpaque(lowerFrontLeft))
            {
                // Single tri
                model.addBoundedUVTriangle(backRight, lowerBack, lowerRight, lowerBackRightInChunk, 
                    tri_middle_upper, tri_left, tri_right);
            }
        }

        // Rotate 270 degrees about Z
        if (isOpaque(lowerBackLeft) && !isOpaque(upperBackLeft)) {
            if (isOpaque(upperBackRight)) {
                if (isOpaque(upperFrontLeft)) {
                    // Chipped block
                    model.addBoundedUVTriangle(backLeft, upperLeft, upperBack, lowerBackLeftInChunk, 
                        tri_middle_lower, tri_right, tri_left);
                }
            }
            else if (!isOpaque(lowerBackRight) && !isOpaque(upperFrontLeft) && !isOpaque(lowerFrontLeft)
                && !isOpaque(upperFrontRight) && !isOpaque(lowerFrontRight))
            {
                // Single tri
                model.addBoundedUVTriangle(backLeft, lowerLeft, lowerBack, lowerBackLeftInChunk, 
                    tri_middle_upper, tri_left, tri_right);
            }
        }

        // Rotated 180 degrees about Y, bottom face on top
        if (isOpaque(upperFrontRight) && !isOpaque(lowerFrontRight)) {
            if (isOpaque(lowerBackRight)) {
                if (isOpaque(lowerFrontLeft)) {
                    // Chipped block
                    model.addBoundedUVTriangle(frontRight, lowerFront, lowerRight, upperFrontRightInChunk, 
                        tri_middle_upper, tri_left, tri_right);
                }
            }
            else if (!isOpaque(upperBackRight) && !isOpaque(lowerFrontLeft) && !isOpaque(upperFrontLeft)
                && !isOpaque(lowerBackLeft) && !isOpaque(upperBackLeft))
            {
                // Single tri
                model.addBoundedUVTriangle(frontRight, upperFront, upperRight, upperFrontRightInChunk, 
                    tri_middle_lower, tri_left, tri_right);
            }
        }

        // Rotate 180 degrees about Y, bottom face on top, then 90 degrees about Z
        if (isOpaque(upperFrontLeft) && !isOpaque(lowerFrontLeft)) {
            if (isOpaque(lowerFrontRight)) {
                if (isOpaque(lowerBackLeft)) {
                    // Chipped block
                    model.addBoundedUVTriangle(frontLeft, lowerLeft, lowerFront, upperFrontLeftInChunk, 
                        tri_middle_upper, tri_left, tri_right);
                }
            }
            else if (!isOpaque(upperFrontRight) && !isOpaque(lowerBackLeft) && !isOpaque(upperBackLeft)
                && !isOpaque(lowerBackRight) && !isOpaque(upperBackRight))
            {
                // Single tri
                model.addBoundedUVTriangle(frontLeft, upperLeft, upperFront, upperFrontLeftInChunk, 
                    tri_middle_lower, tri_left, tri_right);
            }
        }

        // Rotate 180 degrees about Y, bottom face on top, then 180 degrees about Z
        if (isOpaque(upperBackLeft) && !isOpaque(lowerBackLeft)) {
            if (isOpaque(lowerFrontLeft)) {
                if (isOpaque(lowerBackRight)) {
                    // Chipped block
                    model.addBoundedUVTriangle(backLeft, lowerBack, lowerLeft, upperBackLeftInChunk, 
                        tri_middle_upper, tri_left, tri_right);
                }
            }
            else if (!isOpaque(upperFrontLeft) && !isOpaque(lowerBackRight) && !isOpaque(upperBackRight)
                && !isOpaque(lowerFrontRight) && !isOpaque(upperFrontRight))
            {
                // Single tri
                model.addBoundedUVTriangle(backLeft, upperBack, upperLeft, upperBackLeftInChunk, 
                    tri_middle_lower, tri_left, tri_right);
            }
        }

        // Rotate 180 degrees about Y, bottom face on top, then 270 degrees about Z
        if (isOpaque(upperBackRight) && !isOpaque(lowerBackRight)) {
            if (isOpaque(lowerBackLeft)) {
                if (isOpaque(lowerFrontRight)) {
                    // Chipped block
                    model.addBoundedUVTriangle(backRight, lowerRight, lowerBack, upperBackRightInChunk, 
                        tri_middle_upper, tri_left, tri_right);
                }
            }
            else if (!isOpaque(upperBackLeft) && !isOpaque(lowerFrontRight) && !isOpaque(upperFrontRight)
                && !isOpaque(lowerFrontLeft) && !isOpaque(upperFrontLeft))
            {
                // Single tri
                model.addBoundedUVTriangle(backRight, upperRight, upperBack, upperBackRightInChunk, 
                    tri_middle_lower, tri_left, tri_right);
            }
        }

        // Six-way symmetrical parts
        // Standard orientation
        addFacing(
            lowerFrontLeft,
            upperFrontLeft,
            lowerBackLeft,
            upperBackLeft,
            lowerFrontRight,
            upperFrontRight,
            lowerBackRight,
            upperBackRight,
            center,
            frontLeft,
            frontRight,
            backLeft,
            backRight,
            lowerLeft,
            lowerRight,
            upperLeft,
            upperRight,
            lowerFront,
            lowerBack,
            upperFront,
            upperBack, 
            lowerFrontLeftInChunk,
            lowerFrontRightInChunk,
            lowerBackRightInChunk,
            lowerBackLeftInChunk,
            new Vector2f(0.317f, 1.183f),
            new Vector2f(0.5f, 0.5f),
            new Vector2f(1.5f, 1.5f),
            new Vector2f(0.817f, 1.683f),
            true
        );

        // Rotated 90 degrees about Y, left/western face on top, then 90 degrees about Z
        addFacing(
            lowerBackRight,
            lowerBackLeft,
            upperBackRight,
            upperBackLeft,
            lowerFrontRight,
            lowerFrontLeft,
            upperFrontRight,
            upperFrontLeft,
            center,
            lowerBack,
            lowerFront,
            upperBack,
            upperFront,
            backRight,
            frontRight,
            backLeft,
            frontLeft,
            lowerRight,
            upperRight,
            lowerLeft,
            upperLeft,
            lowerBackRightInChunk,
            lowerFrontRightInChunk, 
            upperFrontRightInChunk,
            upperBackRightInChunk,
            new Vector2f(1.183f, 1.683f),
            new Vector2f(0.5f, 1.5f),
            new Vector2f(1.5f, 0.5f),
            new Vector2f(1.683f, 1.183f),
            true
        );

        // Rotated 180 degrees about Y, bottom face on top
        addFacing(
            upperFrontRight,
            lowerFrontRight,
            upperBackRight,
            lowerBackRight,
            upperFrontLeft,
            lowerFrontLeft,
            upperBackLeft,
            lowerBackLeft,
            center,
            frontRight,
            frontLeft,
            backRight,
            backLeft,
            upperRight,
            upperLeft,
            lowerRight,
            lowerLeft,
            upperFront,
            upperBack,
            lowerFront,
            lowerBack,
            upperFrontRightInChunk,
            upperFrontLeftInChunk, 
            upperBackLeftInChunk,
            upperBackRightInChunk,
            new Vector2f(1.683f, 0.817f),
            new Vector2f(1.5f, 1.5f),
            new Vector2f(0.5f, 0.5f),
            new Vector2f(1.183f, 0.317f),
            true
        );

        // Rotated 270 degrees about Y, right/eastern face on top, then 270 degrees about Z
        addFacing(
            lowerFrontLeft,
            lowerFrontRight,
            upperFrontLeft,
            upperFrontRight,
            lowerBackLeft,
            lowerBackRight,
            upperBackLeft,
            upperBackRight,
            center,
            lowerFront,
            lowerBack,
            upperFront,
            upperBack,
            frontLeft,
            backLeft,
            frontRight,
            backRight,
            lowerLeft,
            upperLeft,
            lowerRight,
            upperRight,
            lowerFrontLeftInChunk,
            lowerBackLeftInChunk,
            upperBackLeftInChunk,
            upperFrontLeftInChunk,
            new Vector2f(0.817f, 0.317f),
            new Vector2f(1.5f, 0.5f),
            new Vector2f(0.5f, 1.5f),
            new Vector2f(0.317f, 0.817f),
            true
        );

        // Rotate 90 degreees about X, back face on top, then 180 degrees about Z
        addFacing(
            lowerFrontRight,
            lowerBackRight,
            upperFrontRight,
            upperBackRight,
            lowerFrontLeft,
            lowerBackLeft,
            upperFrontLeft,
            upperBackLeft,
            center,
            lowerRight,
            lowerLeft,
            upperRight,
            upperLeft,
            frontRight,
            frontLeft,
            backRight,
            backLeft,
            lowerFront,
            upperFront,
            lowerBack,
            upperBack, 
            lowerFrontRightInChunk,
            lowerFrontLeftInChunk,
            upperFrontLeftInChunk,
            upperFrontRightInChunk,
            new Vector2f(0.817f, 0.317f),
            new Vector2f(1.5f, 0.5f),
            new Vector2f(0.5f, 1.5f),
            new Vector2f(0.317f, 0.817f),
            false
        );

        // Rotate 270 degrees about X, front face on top
        addFacing(
            lowerBackLeft,
            lowerFrontLeft,
            upperBackLeft,
            upperFrontLeft,
            lowerBackRight,
            lowerFrontRight,
            upperBackRight,
            upperFrontRight,
            center,
            lowerLeft,
            lowerRight,
            upperLeft,
            upperRight,
            backLeft,
            backRight,
            frontLeft,
            frontRight,
            lowerBack,
            upperBack,
            lowerFront,
            upperFront, 
            lowerBackLeftInChunk,
            lowerBackRightInChunk,
            upperBackRightInChunk,
            upperBackLeftInChunk,
            new Vector2f(1.183f, 1.683f),
            new Vector2f(0.5f, 1.5f),
            new Vector2f(1.5f, 0.5f),
            new Vector2f(1.683f, 1.183f),
            false
        );
    }

    // TO_LATER_DO: Share more code between the surface-based and chunk-based getMesh()
    public TerrainMeshData getMesh(int numIterations, float weight, int chunkX, int chunkY, int chunkZ) {
        TerrainColumn column = terrain.getColumnIfLoaded(chunkX, chunkY);
        if (column == null) {
            return null;
        }
        Chunk chunk = column.getChunk(chunkZ);
        if (chunk == null || chunk.isEmpty()) {
            return null;
        }

        int margin = numIterations + 1;
        for (int z = -1 - margin; z < Chunk.SIZE + margin; ++z) {
            for (int x = -1 - margin; x < Chunk.SIZE + margin; ++x) {
                for (int y = -1 - margin; y < Chunk.SIZE + margin; ++y) {
                    addGeometry(chunkX, chunkY, chunkZ, x, y, z, true);
                }
            }
        }
        model.addTrim();

        List<Vector3f> originalLocations = new ArrayList<>();
        for (Vertex vertex : model.getVertices()) {
            originalLocations.add(new Vector3f(vertex.position));
        }
        for (int i = 0; i < numIterations; ++i) {
            model.contract(originalLocations, 0.5f / terrain.blockSize, weight);
        }

        model.calcNormals();
        addBumps(chunkX, chunkY, chunkZ);
        model.calcNormals();
        model.trim();
        calculateTextureCoords();
        return new TerrainMeshData(model.finalizeToVertexList());
    }

    public TerrainMeshData getMesh(int numIterations, float weight, int chunkX, int chunkY, Surface surface) {
        int margin = numIterations + 1;
        for (int x = -1 - margin; x < Chunk.SIZE + margin; ++x) {
            for (int y = -1 - margin; y < Chunk.SIZE + margin; ++y) {
                int nx = (int)Math.max(Chunk.SIZE - 1, Math.min(0, x));
                int ny = (int)Math.max(Chunk.SIZE - 1, Math.min(0, y));
                int h = surface.getHeight(nx, ny);
                for (int z = h - 1 - margin; z <= h + margin; ++z) {
                    addGeometry(chunkX, chunkY, 0, x, y, z, false);
                }
            }
        }
        model.addTrim();

        List<Vector3f> originalLocations = new ArrayList<>();
        for (Vertex vertex : model.getVertices()) {
            originalLocations.add(new Vector3f(vertex.position));
        }
        for (int i = 0; i < numIterations; ++i) {
            model.contract(originalLocations, 0.5f / terrain.blockSize, weight);
        }

        model.calcNormals();
        addBumps(chunkX, chunkY, 0);
        model.calcNormals();
        model.trim();
        calculateTextureCoords();
        return new TerrainMeshData(model.finalizeToVertexList());
    }

    private void addBumps(int chunkX, int chunkY, int chunkZ) {
        for (Map.Entry<BlockPos, Vertex> entry : vertexMap.entrySet()) {
            BlockPos pos = entry.getKey();
            Vertex v = entry.getValue();
            if (v.normal == null) {
                continue;
            }
            int x = chunkX * Chunk.SIZE * 2 + pos.x;
            int y = chunkY * Chunk.SIZE * 2 + pos.y;
            int z = chunkZ * Chunk.SIZE * 2 + pos.z;
            byte r = quickPseudorandom(x, y, z);
            byte r2 = quickPseudorandom(x/2, y/2, z/2);
            byte r3 = quickPseudorandom((x + 1) / 2, (y + 1) / 2, (z + 1) / 2);
            float s = 0.5f * (r2 - 128) / 256 + 0.5f * (r3 - 128) / 256 + 0.25f * (r - 127) / 256;
            s = 0.25f * s / terrain.blockSize;
            v.position.add(v.normal.x * s, v.normal.y * s, v.normal.z * s);
        }
    }

    private byte quickPseudorandom(int x, int y, int z) {
        int r = x * x * x;
        r = 15 * r + y * y * y;
        r = 15 * r + z * z * z;
        return (byte)r;
    }

    private void calculateTextureCoords() {
        for (int i = 0; i < model.quads.size(); ++i) {
            int[] quad = model.quads.get(i);
            assert(quad.length == 4);
            Vertex v0 = model.getVertices().get(quad[0]);
            Vertex v1 = model.getVertices().get(quad[1]);
            Vertex v2 = model.getVertices().get(quad[2]);
            Vertex v3 = model.getVertices().get(quad[3]);
            float s = terrain.blockSize * terrain.blockSize;
            float d01sq = s * v0.position.distanceSquared(v1.position);
            float d12sq = s * v1.position.distanceSquared(v2.position);
            float d23sq = s * v2.position.distanceSquared(v3.position);
            float d30sq = s * v3.position.distanceSquared(v0.position);
            float uv01sq = v0.texture.distanceSquared(v1.texture);
            float uv12sq = v1.texture.distanceSquared(v2.texture);
            float uv23sq = v2.texture.distanceSquared(v3.texture);
            float uv30sq = v3.texture.distanceSquared(v0.texture);
            if (uv01sq < 2 * d01sq && uv01sq > 0.5 * d01sq
                && uv12sq < 2 * d12sq && uv12sq > 0.5 * d12sq
                && uv23sq < 2 * d23sq && uv23sq > 0.5 * d23sq
                && uv30sq < 2 * d30sq && uv30sq > 0.5 * d30sq) {
                    continue;
            }
            if (model.uvQuadOverrides.size() > i && model.uvQuadOverrides.get(i) != null) {
                continue;
            }

            // TO_OPTIMIZE: Making and sorting a list may be slow
            ArrayList<Vector3f> sortList = new ArrayList<>();
            sortList.add(v0.position);
            sortList.add(v1.position);
            sortList.add(v2.position);
            sortList.add(v3.position);
            sortList.sort((a, b) -> Float.compare(a.z, b.z));
            boolean v0first = v0.position == sortList.get(0) || v0.position == sortList.get(1);
            boolean v1first = v1.position == sortList.get(0) || v1.position == sortList.get(1);
            boolean v2first = v2.position == sortList.get(0) || v2.position == sortList.get(1);
            boolean v3first = v3.position == sortList.get(0) || v3.position == sortList.get(1);
            Vector2f uv0 = new Vector2f(0, v0first ? 0.5f : -0.5f);
            Vector2f uv1 = new Vector2f(0, v1first ? 0.5f : -0.5f);
            Vector2f uv2 = new Vector2f(0, v2first ? 0.5f : -0.5f);
            Vector2f uv3 = new Vector2f(0, v3first ? 0.5f : -0.5f);

            Vector3f normal = Vector3f.average(v0.normal, v1.normal, v2.normal, v3.normal);
            if (normal.y < 0 && Math.abs(normal.y) > Math.abs(normal.x)) {
                sortList.sort((a, b) -> Float.compare(a.x, b.x));
            }
            else if (normal.y > 0 && Math.abs(normal.y) > Math.abs(normal.x)) {
                sortList.sort((a, b) -> Float.compare(-1 * a.x, -1 * b.x));
            }
            else if (normal.x < 0) {
                sortList.sort((a, b) -> Float.compare(-1 * a.y, -1 * b.y));
            }
            else {
                assert(normal.x > 0);
                sortList.sort((a, b) -> Float.compare(a.y, b.y));
            }
            v0first = v0.position == sortList.get(0) || v0.position == sortList.get(1);
            v1first = v1.position == sortList.get(0) || v1.position == sortList.get(1);
            v2first = v2.position == sortList.get(0) || v2.position == sortList.get(1);
            v3first = v3.position == sortList.get(0) || v3.position == sortList.get(1);
            uv0.x = v0first ? 0.5f : 1.5f;
            uv1.x = v1first ? 0.5f : 1.5f;
            uv2.x = v2first ? 0.5f : 1.5f;
            uv3.x = v3first ? 0.5f : 1.5f;

            Vector2f[] uvs = {uv0, uv1, uv2,  uv3};
            while (i >= model.uvQuadOverrides.size()) {
                model.uvQuadOverrides.add(null);
            }
            model.uvQuadOverrides.set(i, uvs);
        }
    }

    private Vertex textureCopy(Vertex v) {
        Vertex n = new Vertex();
        n.position = v.position;
        n.normal = v.normal;
        n.texture = new Vector2f(v.texture.x, v.texture.y);
        n.type = v.type;
        return n;
    }

    protected Vertex findVertex(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (vertexMap.containsKey(pos)) {
            return vertexMap.get(pos);
        }
        Vertex vertex = new Vertex();
        vertex.position = new Vector3f(x, y, z).scale(0.5f/terrain.blockSize);
        vertexMap.put(pos, vertex);
        return vertex;
    }

    protected Vertex setupVertex(short a, short b, int x, int y, int z, Direction direction) {
        if (isOpaque(a) == isOpaque(b)) {
            return null;
        }
        int vx = 2 * x + 1 + direction.x;
        int vy = 2 * y + 1 + direction.y;
        int vz = 2 * z + 1 + direction.z;
        Vertex vertex = findVertex(vx, vy, vz);
        int dx = direction.x;
        int dy = direction.y;
        int dz = direction.z;
        if (isOpaque(a)) {
            vertex.type = a;
        }
        else {
            vertex.type = b;
            dx *= -1;
            dy *= -1;
            dz *= -1;
        }
        if (dx < 0) {
            vertex.texture = new Vector2f(-1 * vy, vz);
        }
        else if (dx > 0) {
            vertex.texture = new Vector2f(vy, vz);
        }
        else if (dy < 0) {
            vertex.texture = new Vector2f(vx, vz);
        }
        else if (dy > 0) {
            vertex.texture = new Vector2f(-1 * vx, vz);
        }
        else if (dz > 0) {
            vertex.texture = new Vector2f(vx, vy);
        }
        else {
            assert(dz < 0);
            vertex.texture = new Vector2f(vx, 1 - vy);
        }
        vertex.texture.scale(0.5f);

        return vertex;
    }

    private boolean isOpaque(short block) {
        return Block.isOpaque(block);
    }
}
