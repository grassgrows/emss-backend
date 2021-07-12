package top.warmthdawn.emss.features.file.vo


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */

enum class FileChunkStatus {
    VALID,
    NON_UPLOADER_REQUEST,
    INVALID_UPLOADER_REQUEST1,
    INVALID_UPLOADER_REQUEST2,
    INVALID_UPLOADER_REQUEST3,
    INVALID_UPLOADER_REQUEST4,
    INVALID_UPLOADER_REQUEST5,
    FOUND,
    NOT_FOUND,
    DONE,
    PARTLY_DONE
}