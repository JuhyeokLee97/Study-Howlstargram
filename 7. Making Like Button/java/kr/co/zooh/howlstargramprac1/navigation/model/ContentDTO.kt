package kr.co.zooh.howlstargramprac1.navigation.model

data class ContentDTO(
    var explain: String? = null,
    var imageUri: String? = null,
    var uid: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap()
) {
    data class Comment(
        var uid: String? = null,
        var userId: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )

}