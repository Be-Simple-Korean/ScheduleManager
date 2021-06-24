package  com.example.schedulemanager.data.location

/**
 * 카카오 API로 받는 Response DATA VO
 */
data class LocationDataVO(val meta: MetaVO, val documents: ArrayList<DocumentsVO>) {
    override fun toString(): String {
        return meta.toString() + "\n" + documents.toString()
    }
}