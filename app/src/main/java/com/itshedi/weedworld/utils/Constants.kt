package com.itshedi.weedworld.utils

import androidx.compose.ui.graphics.Color
import com.itshedi.weedworld.R

class ProductCategory(val name:String, val image:Int,val isWeed:Boolean)

val productCategories = listOf(
    ProductCategory("Flower", R.drawable.category_flower,true),
    ProductCategory("Pre Rolls", R.drawable.category_prerolls,true),
    ProductCategory("Edibles", R.drawable.category_edibles,true),
    ProductCategory("Vape Carts", R.drawable.category_vapecarts,true),
    ProductCategory("CBD", R.drawable.category_cbd,true),
    ProductCategory("Concentrates", R.drawable.category_concentrates,true),
    ProductCategory("Oil", R.drawable.category_oil,true),
    ProductCategory("Vaping", R.drawable.category_vaping,true),
    ProductCategory("Topicals", R.drawable.category_topicals,true),
    ProductCategory("Accessories", R.drawable.category_accessories,false),
    ProductCategory("Seeds", R.drawable.category_seeds,true),
    ProductCategory("Clones", R.drawable.category_clones,true)
)


class ProductSpecie(val name:String, val color:Color)
val productSpecies = listOf(
    ProductSpecie("Indica", Color(0xFFFF00F5)),
    ProductSpecie("Sativa", Color(0xFF35B24F)),
    ProductSpecie("Hybrid", Color(0xFFFF7A00)),
)