package com.gildedrose

import scala.io.Source
import com.gildedrose.items.{ConjuredItem, Item}

object Main {
  def main(args: Array[String]): Unit = {
    val fileReader = Source.fromFile("Inventory.tsv")
    val items = fileReader.getLines().drop(1)
      .map { l =>
        val Array(name, sellIn, quality) = l.split("\t")
        if (name.startsWith("Conjured"))
          new ConjuredItem(name, sellIn.toInt, quality.toInt)
        else
          new Item(name, sellIn.toInt, quality.toInt)
      }.toArray.sortBy(_.name)
    fileReader.close()

    val gildedRoseApp = new GildedRose(items)
    gildedRoseApp.inventoryCheck()

    val days = if (args.length > 0) args(0).toInt + 1 else 2
    if (days <= 0)
      throw new IllegalArgumentException(s"Value of days is non-positive: ${days}")

    System.out.println("OMGHAI!")
    (0 until days).foreach { i =>
      System.out.println("-------- Day " + i + " --------")
      System.out.println(f"${"Name"}%-56s | ${"SellIn"}%-8s | Quality")
      System.out.println("-" * 72)
      items.foreach { item =>
        System.out.println(f"${item.name}%-56s" + " | " + f"${item.sellIn}%8s" + " | " + f"${item.quality}%8s")
      }
      System.out.println()
      gildedRoseApp.updateQuality()
    }
  }
}
