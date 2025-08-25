package util

/** RecipeData case class used for holding static recipe seed data */
case class RecipeData(
                       title: String,
                       category: String,
                       prepTime: String,
                       cookTime: String,
                       totalCalories: Int,
                       imageUrl: String,
                       ingredients: String,
                       protein: Double,
                       fat: Double,
                       carbs: Double,
                       steps: String
                     )

object RecipeDataList:
  /** Predefined list of recipes for seeding database */
  val recipes: List[RecipeData] = List(
    RecipeData(
      "Chicken Caesar Salad",
      "Main Dish",
      "5 minutes",
      "15 minutes",
      358,
      "/images/chicken_caesar_salad.jpg",
      "2 cup shredded lettuce, 1 tbsp caesar dressing, 2 tsp olive oil, 1 chicken breast fillet, 2 tbsp Parmesan cheese",
      31.8, 23.6, 3.9,
      "1. Slice the raw chicken breast into strips\n2. Fry the chicken strips in a pan with olive oil over medium heat until fully cooked, about 10-12 minutes.\n3. In a bowl, mix the cooked chicken, lettuce, Caesar salad dressing, and Parmesan cheese until well combined.\n4. Serve immediately"
    ),
    RecipeData(
      "Berry Smoothie",
      "Beverage",
      "2 minutes",
      "0 minutes",
      210,
      "/images/berry_smoothie.jpg",
      "1 cup mixed berries, 1 banana, 1 cup milk, 1 tsp honey",
      4, 2, 48,
      "1. Add ingredients to blender\n2. Blend until smooth\n3. Serve cold"
    ),
    RecipeData(
      "Garlic Bread",
      "Side Dish",
      "5 minutes",
      "10 minutes",
      250,
      "/images/garlic_bread.jpg",
      "1 baguette, 3 tbsp butter, 2 cloves garlic, parsley",
      6, 8, 38,
      "1. Slice bread\n2. Spread garlic butter\n3. Bake for 10 mins"
    ),
    RecipeData(
      "Chocolate Cake",
      "Desserts",
      "15 minutes",
      "30 minutes",
      450,
      "/images/chocolate_cake.jpg",
      "Flour, cocoa powder, sugar, eggs, butter",
      6, 20, 65,
      "1. Mix dry ingredients\n2. Add wet ingredients\n3. Bake 30 mins"
    ),
    RecipeData(
      "Tomato Soup",
      "Soups & Stews",
      "10 minutes",
      "20 minutes",
      150,
      "/images/tomato_soup.jpg",
      "Tomatoes, onion, garlic, vegetable broth, cream",
      3, 4, 20,
      "1. Sauté onion and garlic\n2. Add tomatoes and broth\n3. Simmer 15 mins\n4. Blend smooth"
    ),
    RecipeData(
      "Basic scrambled eggs",
      "Side Dish",
      "5 minutes",
      "5 minutes",
      273,
      "/images/scramble_egg.jpg",
      "3 large egg, ½ tbsp butter, ½ tbsp Chives, ½ tbsp Tarragon, ½ dash Salt, ½ dash Pepper",
      19.5, 20.2, 2.4,
      "1. SWhisk the eggs in a medium bowl and until broken up. Season with a pinch each of salt and pepper and beat to incorporate. Place 2 tablespoons of the eggs in a small bowl; set aside.\n2. Heat a 10-inch nonstick frying pan over medium-low heat until hot, about 2 minutes. Add butter to the pan and, using a rubber spatula, swirl until it’s melted and foamy and the pan is evenly coated. Pour in the larger portion of the eggs, sprinkle with chives and/or tarragon (if using), and let sit undisturbed until eggs just start to set around the edges, about 1 to 2 minutes. Using the rubber spatula, push the eggs from the edges into the center. Let sit again for about 30 seconds, then repeat pushing the eggs from the edges into the center every 30 seconds until just set, for a total cooking time of about 5 minutes.\n3. Add remaining 2 tablespoons raw egg and stir until eggs no longer look wet. Remove from heat and season with salt and pepper as needed. Serve immediately."
    )
  )
// Add more recipes

