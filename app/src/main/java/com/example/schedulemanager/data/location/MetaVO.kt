package  com.example.schedulemanager.data.location

/**
 * 카카오 API로 받는 데이터 정보 VO
 */
data class MetaVO(val total_count: String, val pageable_count: String, val is_end: Boolean) {
    override fun toString(): String {
        return "meta = {\n" +
                "total_count = " + total_count + ",\n" +
                "pagealbe_count=" + pageable_count + ",\n" +
                "is_end=" + is_end + "\n},\n"
    }
}