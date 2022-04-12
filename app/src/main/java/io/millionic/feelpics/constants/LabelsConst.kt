package io.millionic.feelpics.constants

import io.millionic.feelpics.models.LabelObjects

class LabelsConst {

    companion object{

        val customAllLabels = arrayListOf<LabelObjects>(
            LabelObjects(
                mainCategory = "People",
                important = arrayListOf(),
                nonImportant = arrayListOf("Person","Team","Interaction","Community","Dude","Crew","Army","Crowd","Militia","Selfie")
            ),
            LabelObjects(
                mainCategory = "Smile",
                important = arrayListOf(),
                nonImportant = arrayListOf("Smile")
            ),
            LabelObjects(
                mainCategory = "Smiles",
                important = arrayListOf(),
                nonImportant = arrayListOf("Smiles")
            ),
            LabelObjects(
                mainCategory = "Sports",
                important = arrayListOf("Sports","Cycling","Snowboard","Skiing","Soccer"),
                nonImportant = arrayListOf()
            ),
            LabelObjects(
                mainCategory = "Animal",
                important = arrayListOf("Dog","Cat","Horse"),
                nonImportant = arrayListOf("Aquarium","Bird","Pet","Duck","Rein", "Bear")
            ),
            LabelObjects(
                mainCategory = "Nature",
                important = arrayListOf("Mountain","Lake","Beach","Waterfall"),
                nonImportant = arrayListOf("Forest","Garden","Cliff","Sunset","Sky","River","Rainbow")
            ),
            LabelObjects(
                mainCategory = "Party",
                important = arrayListOf("Cake","Bride","Marriage"),
                nonImportant = arrayListOf("Veil","Event","Nightclub","Ballroom","Carnival")
            ),
            LabelObjects(
                mainCategory = "Toys",
                important = arrayListOf(),
                nonImportant = arrayListOf("Stuffed toy","Plush","Lego","Toy")
            ),
            LabelObjects(
                mainCategory = "Baby",
                important = arrayListOf(),
                nonImportant = arrayListOf("Baby")
            ),
            LabelObjects(
                mainCategory = "Drink",
                important = arrayListOf(),
                nonImportant = arrayListOf("Juice","Cola","Coffee","Cup","Wine","Cappuccino","Alcohol")
            ),
            LabelObjects(
                mainCategory = "Food",
                important = arrayListOf(),
                nonImportant = arrayListOf("Fast food","Bento","Cheeseburger","Hot dog","Cuisine","Couscous","Cookie","Gelato","Pho","Pizza","Sushi","Bread","Supper","Lunch","Meal","Pie")
            ),
            LabelObjects(
                mainCategory = "Art",
                important = arrayListOf(),
                nonImportant = arrayListOf("Watercolor paint","Pasteles","Fiction","Doily","Tattoo")
            ),
            LabelObjects(
                mainCategory = "Vehicle",
                important = arrayListOf("Car","Motorbike","Bicycle"),
                nonImportant = arrayListOf()
            ),
            LabelObjects(
                mainCategory = "Text",
                important = arrayListOf(),
                nonImportant = arrayListOf("Menu","Fiction","Presentation","Receipt","News","Newspaper","Paper","Web page","Poster")
            ),
            LabelObjects(
                mainCategory = "House",
                important = arrayListOf(),
                nonImportant = arrayListOf("Barn","Cushion","Bathroom","Roof","Shelf","Drawer","Kitchen","Cabinetry", "Bedroom", "Couch")
            ),
            LabelObjects(
                mainCategory = "Dress",
                important = arrayListOf(),
                nonImportant = arrayListOf("Tights","Necklace","Sari","Tuxedo","Jersey","Jacket","Denim","Gown","Outerwear","Blazer","Leggings","Jeans","Swimwear")
            ),
//            LabelObjects(
//                mainCategory = "Places",
//                important = arrayListOf(),
//                nonImportant = arrayListOf("Cathedral","Mosque","Temple","Palace","Ruins","Lighthouse")
//            ),
            LabelObjects(
                mainCategory = "Screenshot",
                important = arrayListOf(),
                nonImportant = arrayListOf("Screenshot")
            )

        )
    }

}