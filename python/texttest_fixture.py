# -*- coding: utf-8 -*-
from __future__ import print_function

from gilded_rose import *

if __name__ == "__main__":
    print ("OMGHAI!")
    items = sorted([
             Item(name="+5 Dexterity Vest", sell_in=10, quality=20),
             Item(name="Aged Brie", sell_in=2, quality=0),
             Item(name="Elixir of the Mongoose", sell_in=5, quality=7),
             Item(name="Sulfuras, Hand of Ragnaros", sell_in=0, quality=80),
             Item(name="Sulfuras, Hand of Ragnaros", sell_in=-1, quality=80),
             Item(name="Backstage passes to a TAFKAL80ETC concert", sell_in=15, quality=20),
             Item(name="Backstage passes to a TAFKAL80ETC concert", sell_in=10, quality=49),
             Item(name="Backstage passes to a TAFKAL80ETC concert", sell_in=5, quality=49),
             ConjuredItem(name="Conjured Mana Cake", sell_in=3, quality=6)
            ], key=lambda item: item.name)

    days = 2
    import sys
    if len(sys.argv) > 1:
        days = int(sys.argv[1]) + 1
    for day in range(days):
        print("-------- Day  %s --------" % day)
        print("{:<48} {} {:<8} {} {:<8}".format("Name", "|", "SellIn", "|", "Quality"))
        print("-" * 72)
        for item in items:
            print("{:<48} {} {:>8} {} {:>8}".format(item.name, "|", item.sell_in, "|", item.quality))
        print("")
        GildedRose(items).update_quality()
