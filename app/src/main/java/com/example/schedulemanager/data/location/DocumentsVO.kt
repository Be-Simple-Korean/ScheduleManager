package  com.example.schedulemanager.data.location

/**
 * 카카오 API로받는 검색 결과 위치 DATA VO
 */
data class DocumentsVO(
    var place_name: String = "",
    val distance: String = "",
    val place_url: String = "",
    val category_name: String = "",
    val address_name: String = "",
    val road_address_name: String = "",
    val id: String = "",
    val phone: String = "",
    val category_group_code: String = "",
    val category_group_name: String = "",
    val x: String = "",
    val y: String = ""
) {
    fun setQuery(query:String){
       this.place_name=query
    }
    override fun toString(): String {
        return "documents = {" + "\n" +
                "place_name = " + place_name + ",\n" +
                "distance = " + distance + ",\n" +
                "place_url = " + place_url + ",\n" +
                "category_name = " + category_name + ",\n" +
                "address_name = " + address_name + ",\n" +
                "road_address_name = " + road_address_name + ",\n" +
                "id = " + id + ",\n" +
                "phone = " + phone + ",\n" +
                "category_group_code = " + category_group_code + ",\n" +
                "category_group_name = " + category_group_name + ",\n" +
                "x = " + x + ",\n" +
                "y = " + y + ",\n" + "}"
    }
}