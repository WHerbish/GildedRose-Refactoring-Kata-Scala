package com.gildedrose

import com.gildedrose.items.{ConjuredItem, Item}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GildedRoseTest extends AnyFunSuite with Matchers {

  def inventoryErrorMessage(items: Array[Item], errorMessage: String): Unit = {
    val gildedRoseTestApp = new GildedRose(items)
    val exception = intercept[IllegalArgumentException] {
      gildedRoseTestApp.inventoryCheck()
    }
    exception.getMessage.split("\\.")(0) should be(errorMessage)
  }

  def daysLater(days: Int, gildedRoseTestApp: GildedRose): Unit =
    for (_ <- 1 to days) gildedRoseTestApp.updateQuality()

  test("InventoryCheck should return an error message for bad quality items (Regular and Conjured)") {
    val items = Array[Item](
      new Item("FooBar FoodBar", 3, -1),
      new Item("FooBar FoodBar", 3, 51),
      new Item("Sulfuras, Hand of Ragnaros", 3, 81),
      new ConjuredItem("Conjured Sulfuras, Hand of Ragnaros", 3, 79)
    )

    inventoryErrorMessage(items, "Found an item 'FooBar FoodBar' of invalid quality: -1")
    inventoryErrorMessage(items.drop(1), "Found an item 'FooBar FoodBar' of invalid quality: 51")
    inventoryErrorMessage(items.drop(2), "Found an item 'Sulfuras, Hand of Ragnaros' of invalid quality: 81")
    inventoryErrorMessage(items.drop(3), "Found an item 'Conjured Sulfuras, Hand of Ragnaros' of invalid quality: 79")
  }

  test("UpdateQuality for a regular item should display behaviour as specified in Requirements.md") {
    val items = Array[Item](new Item("FooBar FoodBar", 3, 13))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Decrease in quality by 1 at the end of each day
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(2) && gildedRoseTestApp.items(0).quality.equals(12))
    // ...
    // Day 3 - If passed sell-by day, quality drops twice as fast
    daysLater(3, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(-1) && gildedRoseTestApp.items(0).quality.equals(8))
    // ...
    // Day 10 - Stop decreasing in quality at 0
    daysLater(6, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(-7) && gildedRoseTestApp.items(0).quality.equals(0))
  }

  test("UpdateQuality for Aged Brie should display behaviour as specified in Requirements.md") {
    val items = Array[Item](new Item("Aged Brie", 1, 3))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Increases in quality by 1 each day
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(0) && gildedRoseTestApp.items(0).quality.equals(4))
    // Day 2 - Increased in quality by 2 each day after it has expired
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(-1) && gildedRoseTestApp.items(0).quality.equals(6))
    // ...
    // Day 100 - Stop increasing in quality at 50
    daysLater(98, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(-99) && gildedRoseTestApp.items(0).quality.equals(50))
  }

  test("UpdateQuality for Backstage Passes should display behaviour as specified in Requirements.md") {
    val items = Array[Item](new Item("Backstage passes to a TAFKAL80ETC concert", 11, 30))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Increases in quality by 1 each day
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(10) && gildedRoseTestApp.items(0).quality.equals(31))
    // Day 2 - Increases in quality by 2 if the concert is within 10 days
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(9) && gildedRoseTestApp.items(0).quality.equals(33))
    // ...
    // Day 6 - Increases in quality by 3 if the concert is within 5 days
    daysLater(5, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(4) && gildedRoseTestApp.items(0).quality.equals(44))
    // ...
    // Day 9 - Stop increasing in quality at 50
    daysLater(3, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(1) && gildedRoseTestApp.items(0).quality.equals(50))
    // Day 10 - Still holds quality on the day of the concert itself
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(0) && gildedRoseTestApp.items(0).quality.equals(50))
    // Day 11 - Drop quality to 0 the day after the concert
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(-1) && gildedRoseTestApp.items(0).quality.equals(0))
    // Day 12 - Remains 0 afterwards (cannot regain quality)
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(-2) && gildedRoseTestApp.items(0).quality.equals(0))
  }

  test("UpdateQuality for Sulfuras should display behaviour as specified in Requirements.md") {
    val items = Array[Item](new Item("Sulfuras, Hand of Ragnaros", 10, 80))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Never has to be sold (sellIn never decreases) and never changes in quality (quality is static)
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(10) && gildedRoseTestApp.items(0).quality.equals(80))
    // ...
    // Day 365 - Even in one year from now, it will never be sold and never change in quality
    daysLater(365, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(10) && gildedRoseTestApp.items(0).quality.equals(80))
  }

  test("UpdateQuality should know how to behave when presented a Conjured regular item") {
    val items = Array[Item](new ConjuredItem("Conjured FooBar FoodBar", 3, 13))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Decrease in quality by 2 at the end of each day
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(2) && gildedRoseTestApp.items(0).quality.equals(11))
    // ...
    // Day 3 - If passed sell-by day, quality drops twice as fast
    daysLater(3, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(-1) && gildedRoseTestApp.items(0).quality.equals(3))
    // ...
    // Day 10 - Stop decreasing in quality at 0
    daysLater(6, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(-7) && gildedRoseTestApp.items(0).quality.equals(0))
  }

  test("UpdateQuality should know how to behave when presented a Conjured Aged Brie") {
    val items = Array[Item](new ConjuredItem("Conjured Aged Brie", 1, 3))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Increases in quality by 2 each day
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(0) && gildedRoseTestApp.items(0).quality.equals(5))
    // Day 2 - Increased in quality by 4 each day after it has expired
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(-1) && gildedRoseTestApp.items(0).quality.equals(9))
    // ...
    // Day 100 - Stop increasing in quality at 50
    daysLater(98, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(-99) && gildedRoseTestApp.items(0).quality.equals(50))
  }

  test("UpdateQuality should know how to behave when presented a Conjured Backstage Passes") {
    val items = Array[Item](new ConjuredItem("Conjured Backstage passes to a TAFKAL80ETC concert", 11, 10))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Increases in quality by 2 each day
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(10) && gildedRoseTestApp.items(0).quality.equals(12))
    // Day 2 - Increases in quality by 4 if the concert is within 10 days
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(9) && gildedRoseTestApp.items(0).quality.equals(16))
    // ...
    // Day 6 - Increases in quality by 6 if the concert is within 5 days
    daysLater(5, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(4) && gildedRoseTestApp.items(0).quality.equals(38))
    // ...
    // Day 9 - Stop increasing in quality at 50
    daysLater(3, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(1) && gildedRoseTestApp.items(0).quality.equals(50))
    // Day 10 - Still holds quality on the day of the concert itself
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(0) && gildedRoseTestApp.items(0).quality.equals(50))
    // Day 11 - Drop quality to 0 the day after the concert
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(-1) && gildedRoseTestApp.items(0).quality.equals(0))
    // Day 12 - Remains 0 afterwards (cannot regain quality)
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(-2) && gildedRoseTestApp.items(0).quality.equals(0))
  }

  test("UpdateQuality should know how to behave when presented a Conjured Sulfuras") {
    val items = Array[Item](new ConjuredItem("Conjured Sulfuras, Hand of Ragnaros", 10, 80))
    val gildedRoseTestApp = new GildedRose(items)

    // Day 1 - Never has to be sold (sellIn never decreases) and never changes in quality (quality is static)
    gildedRoseTestApp.updateQuality()
    assert(gildedRoseTestApp.items(0).sellIn.equals(10) && gildedRoseTestApp.items(0).quality.equals(80))
    // ...
    // Day 365 - Even in one year from now, it will never be sold and never change in quality
    daysLater(365, gildedRoseTestApp)
    assert(gildedRoseTestApp.items(0).sellIn.equals(10) && gildedRoseTestApp.items(0).quality.equals(80))
  }
}