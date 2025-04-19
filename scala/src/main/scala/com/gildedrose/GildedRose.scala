package com.gildedrose

import com.gildedrose.items.{ConjuredItem, Item}

class GildedRose(val items: Array[Item]) {

  /**
   * Rules:
   * - The Quality-Decrease-factor (QDF) of a regular item is -1 (i.e. an item degrades by 1 at the end of each day).
   * - An item's sellIn can be negative - this just means the item is past sell-by date.
   * - When an item's sellIn is negative, QDF is twice that of normal (i.e. -1 * 2 = -2).
   * - An item's quality can never decrease below 0 and can never increase above 50 (This should included when you define the item on Day 0).
   * - Aged Brie increases in quality by 1 each day (caps at 50) - This is to say that the QDF of Aged Brie is +1.
   * - Sulfuras is never sold (sellIn is fixed) and always has a quality of 80 (quality is fixed) - This is the only exception to the cap-at-50 rule (QDF is 0).
   * - The quality of Backstage Passes (cap-at-50 rule applies here too)...
   *   - Increases by 1 if 10 < sellIn (same as Aged Brie - QF is +1),
   *   - Increases by 2 if 5 < sellIn <= 10 (QDF is +2),
   *   - Increases by 3 if 0 <= sellIn <= 5 (QDF is +3),
   *   - Becomes 0 if SellIn < 0 (QDF drops to 0).
   * - Conjured items degrade twice as fast, meaning the QDF of a conjured item is the QDF of its' non-conjured version * 2.
   * - This stacks with normal item behaviour (e.g. a regular item that is past sell-by date has QDF = -1 * 2 * 2 = -4)
   *
   * Important:
   * - The definition of what is a Conjured item and how it should influence the QDF of an item is vague.
   * - This code assumes that the Conjured trait simply accelerates the rate at which an item degrades.
   * - The alternative is to assume Conjured as a penalty rather than a factor.
   * - E.g. This code would say that Conjured Aged Brie increases in quality at twice the rate than a non-conjured Aged Brie:
   *    i.e. QDF = 1 * 2 = 2
   * - However this alternative definition would say that Conjured Aged Brie decreases in quality at the same rate as a non-conjured regular item:
   *    i.e. QDF = 1 - 2 = -1
   */
  def updateQuality() : Unit = {
    items.foreach { item =>
      // Filter out Sulfuras because it never needs to be sold and it never changes in quality, even when Conjured
      if (!item.name.contains("Sulfuras, Hand of Ragnaros")) {
        // Always decrease sellIn by 1
        item.sellIn = item.sellIn - 1
        // Adjust quality depending on the item
        item.name match {
          case name if name.contains("Aged Brie") =>
            item.quality = (item.quality + ((if (item.sellIn < 0) 2 else 1) * (if (item.isInstanceOf[ConjuredItem]) 2 else 1))).min(50)
          case name if name.contains("Backstage passes to a TAFKAL80ETC concert") =>
            if (item.sellIn < 0)
              item.quality = 0
            else if (item.sellIn < 5)
              item.quality = (item.quality + (3 * (if (item.isInstanceOf[ConjuredItem]) 2 else 1))).min(50)
            else if (item.sellIn < 10)
              item.quality = (item.quality + (2 * (if (item.isInstanceOf[ConjuredItem]) 2 else 1))).min(50)
            else
              item.quality = (item.quality + (1 * (if (item.isInstanceOf[ConjuredItem]) 2 else 1))).min(50)
          case _ => // Regular item
            item.quality = (item.quality - ((if (item.sellIn < 0) 2 else 1) * (if (item.isInstanceOf[ConjuredItem]) 2 else 1))).max(0)
        }
      }
    }
  }

  def inventoryCheck(): Unit = {
    items.foreach { item =>
      if ((item.quality < 0) || (item.quality > 50 && !item.name.contains("Sulfuras, Hand of Ragnaros")) || (item.name.contains("Sulfuras, Hand of Ragnaros") && item.quality != 80))
        throw new IllegalArgumentException(s"Found an item '${item.name}' of invalid quality: ${item.quality}. Please remove from the store or correct its value before we open")
    }
  }
}