package geotrellis.raster.op.global

import geotrellis.raster._
import geotrellis.raster.testkit._

import org.scalatest._

/**
 * Created by jchien on 4/24/14.
 */
class ViewshedSpec extends FunSpec
                      with Matchers
                      with RasterMatchers with TestFiles
                      with TileBuilders {
  describe("Viewshed") {
    it("computes the viewshed of a flat int plane") {
      val r = createTile(Array.fill(7 * 8)(1), 7, 8)
      assertEqual(BitConstantTile(true, 7, 8), r.viewshed(4, 3, true))
    }

    it("computes the viewshed of a flat double plane") {
      val r = createTile(Array.fill(7 * 8)(1.5), 7, 8)
      assertEqual(BitConstantTile(true, 7, 8), r.viewshed(4, 3, true))
    }

    it("computes the viewshed of a double line") {
      val rasterData = Array (
        300.0, 1.0, 99.0, 0.0, 10.0, 200.0, 137.0
      )
      val viewable = Array (
        1, 0, 1, 1, 1, 1, 0
      )
      val r = createTile(rasterData, 7, 1)
      val viewRaster = createTile(viewable, 7, 1).convert(BitCellType)
      assertEqual(viewRaster, r.viewshed(3, 0, true))
    }

    it("computes the viewshed of a double plane") {
      val rasterData = Array (
        999.0, 1.0,   1.0,   1.0,   1.0,   1.0,   999.0,
        1.0,   1.0,   1.0,   1.0,   1.0,   499.0, 1.0,
        1.0,   1.0,   1.0,   1.0,   99.0,  1.0,   1.0,
        1.0,   1.0,   999.0, 1.0,   1.0,   1.0,   1.0,
        1.0,   1.0,   1.0,   1.0,   100.0, 1.0,   1.0,
        1.0,   1.0,   1.0,   1.0,   1.0,   101.0, 1.0,
        1.0,   1.0,   1.0,   1.0,   1.0,   1.0,   102.0
      )
      val viewable = Array (
          1,     1,     1,     1,     0,     0,     1,
          0,     1,     1,     1,     0,     1,     0,
          0,     0,     1,     1,     1,     0,     0,
          0,     0,     1,     1,     1,     1,     1,
          0,     0,     1,     1,     1,     0,     0,
          0,     1,     1,     1,     0,     0,     0,
          1,     1,     1,     1,     0,     0,     0
      )
      val r = createTile(rasterData, 7, 7)
      val viewRaster = createTile(viewable, 7, 7).convert(BitCellType)
      assertEqual(viewRaster, r.viewshed(3, 3, true))
    }

    it("computes the viewshed of a int plane") {
      val rasterData = Array (
        999, 1,   1,   1,   1,   499, 999,
        1,   1,   1,   1,   1,   499, 250,
        1,   1,   1,   1,   99,  1,   1,
        1,   999, 1,   1,   1,   1,   1,
        1,   1,   1,   1,   1,   1,   1,
        1,   1,   1,   0,   1,   1,   1,
        1,   1,   1,   1,   1,   1,   1
      )
      val viewable = Array (
        1,     1,     1,     1,     0,     1,     1,
        1,     1,     1,     1,     0,     1,     1,
        0,     1,     1,     1,     1,     0,     0,
        0,     1,     1,     1,     1,     1,     1,
        0,     1,     1,     1,     1,     1,     1,
        1,     1,     1,     0,     1,     1,     1,
        1,     1,     1,     1,     1,     1,     1
      )
      val r = createTile(rasterData, 7, 7)
      val viewRaster = createTile(viewable, 7, 7).convert(BitCellType)
      assertEqual(viewRaster, r.viewshed(3, 3, true))
    }

    it("ignores NoData values and indicates they're unviewable"){
      val rasterData = Array (
        300.0, 1.0, 99.0, 0.0, Double.NaN, 200.0, 137.0
      )
      val viewable = Array (
        1, 0, 1, 1, 0, 1, 0
      )
      val r = createTile(rasterData, 7, 1)
      val viewRaster = createTile(viewable, 7, 1).convert(BitCellType)
      assertEqual(viewRaster, r.viewshed(3, 0, true))
    }

    it("should match veiwshed generated by ArgGIS") {
      val rs = loadTestArg("data/viewshed-elevation")
      val elevation = rs.tile
      val rasterExtent = rs.rasterExtent
      val expected = loadTestArg("data/viewshed-expected")

      val (x, y) = (-93.63300872055451407, 30.54649743277299123) // create overload
      val (col, row) = rasterExtent.mapToGrid(x, y)
      val actual = elevation.viewshed(col, row, true)

      def countDiff(a: Tile, b: Tile): Int = {
        var ans = 0
        for(col <- 0 until 256) {
          for(row <- 0 until 256) {
            if (a.get(col, row) != b.get(col, row)) ans += 1;
          }
        }
        ans
      }

      val diff = (countDiff(expected, actual) / (256 * 256).toDouble) * 100
      val allowable = 5.0
      System.out.println(s"${diff} / ${256 * 256} = ${diff / (256 * 256).toDouble}")
      withClue(s"Percent difference from ArgGIS raster is more than $allowable%:") {
        diff should be < allowable
      }
    }
  }
}
