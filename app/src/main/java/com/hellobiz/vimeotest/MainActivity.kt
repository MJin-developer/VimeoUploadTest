package com.hellobiz.vimeotest

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.clickntap.vimeo.VimeoResponse
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.gson.JsonElement
import com.hellobiz.mission2.RetrofitInterface
import com.vimeo.networking.Configuration
import com.vimeo.networking.VimeoClient
import com.vimeo.networking.callbacks.ModelCallback
import com.vimeo.networking.model.User
import com.vimeo.networking.model.Video
import com.vimeo.networking.model.VideoList
import com.vimeo.networking.model.error.VimeoError
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    // Vimeo 계정 정보
    val clientID = "5da2fe26383629b7f9600548ad75bc2c6eafcc83"
    val clientSecret = "/FTSs0F8RIPiTNrSCketB0e/vt33/bcKUiTXxXks/BMGFlwho1o42g/DZOFJaCZBxSYJRmizeSHs6YOmg11MHmMWQDMoq2ZUhwCRveroFfK+6HEL604PfdyWSoTff9oj"
    val clientAccessToken = "b3e1d76e0feca663e5a49466268d1167"
    //val clientAccessToken = "4e35b0e77033c711bc132dd43daa607c"

    // access token 의 허용 범위
    // upload 권한이 없는 계정의 scope 범위에 "upload" 가 들어가면 인증 에러 발생
    val scope = "public private purchased create edit delete interact upload promo_codes video_files"
    val clientMemberID = "126387371"

    // 네트워크 요청을 위해 필요한 uri
    // var vimeoRequestUri  = "/users/126387371"
    var vimeoRequestUri  = "/users/126387371/videos"

    val REQ_VIDEO = 5001

    // 동영상 uri
    var videoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 동영상 업로드 버튼 클릭 시, 갤러리 열기
        btn.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "video/*"
            startActivityForResult(intent, REQ_VIDEO)
        }

        // VimeoClient Initialization
        var configBuilder = Configuration.Builder(clientAccessToken)
            .setCacheDirectory(this.cacheDir)
        VimeoClient.initialize(configBuilder.build())

        // ExoPlayer 초기화
        val exoPlayer = SimpleExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        // 현재 인증되어 있는 유저의 정보
        VimeoClient.getInstance().fetchCurrentUser(object : ModelCallback<User>(User::class.java) {
            override fun success(t: User?) {
                vimeoRequestUri = t!!.uri // 기본 요청 uri
                t.videosConnection?.uri // 동영상 관련 요청 uri
            }

            override fun failure(error: VimeoError?) {
                error?.printStackTrace()
            }
        })// fetchCurrentUser ..

        // 인증되어 있는 유저가 소유한 동영상 리스트 가져오기
        VimeoClient.getInstance().fetchNetworkContent(
            vimeoRequestUri,
            object : ModelCallback<VideoList>(VideoList::class.java) {
                override fun success(t: VideoList?) {
                    // 소유한 동영상 중 첫번째 영상 정보 가져오기
                    val video: Video = t!!.data[2]

                    // 가져온 영상 정보에서 재생에 필요한 영상 uri
                    val videoLink = video.download!![video.download!!.size - 1].getLink()

                    // exoplayer 에 set 해줄 영상 uri
                    val videoItem = MediaItem.fromUri(videoLink)

                    // 재생
                    exoPlayer.setMediaItem(videoItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                }

                override fun failure(error: VimeoError?) {
                    error?.printStackTrace()
                }
            })// fetchNetoworkContent ..

        btn2.setOnClickListener{
            // 비메오 영상 업로드 티켓 가져오기
            btn2.setOnClickListener{
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.vimeo.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val vimeoApi = retrofit.create(RetrofitInterface::class.java)

                val upload = HashMap<String, String>()
                upload["approach"] = "tus"
                upload["size"] = "1024"

//                TODO : 바디 구성 문제로 받는 2205 에러 해결

                vimeoApi.generateUploadTicket(upload)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                val data = response.body()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                        }

                    })
            }
        }



    }// Oncreate ..

    // 갤러리에서 동영상을 선택했다면
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_VIDEO && resultCode == RESULT_OK) {

            var videoUri = data?.data.toString()
            var uri = data?.data

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN
            )

            val selection = "${MediaStore.Video.Media.DATE_TAKEN} >= ?"

            val selectionArgs = arrayOf(
                dateToTimestamp(day = 1, month = 1, year = 1970).toString()
            )

            val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"

            val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            // 사용자가 소유하고 있는 영상들의 정보를 조회
            cursor?.use {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)

                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)

                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)

                    // 갤러리에서 선택하여 가져온 동영상이 id를 포함하고 있다면,
                    if (videoUri.contains(id.toString())){
                        // 동영상 업로드
                        // 커뮤니티 제공 library : clickntap (왜 공식문서에 해당 라이브러리가 포함되어 있는지는 의문 ...)

                        // [Github] library 주소 : https://github.com/clickntap/Vimeo
                        val vimeo = com.clickntap.vimeo.Vimeo(clientAccessToken)

                        // 갤러리에서 선택한 동영상의 경로
                        val realPath = FilePath().getPath(
                            this, Uri.withAppendedPath(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id.toString()
                            )
                        )
                    }

                    val dateTaken = Date(cursor.getLong(dateTakenColumn))

                    val displayName = cursor.getString(displayNameColumn)

                    val contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())

//                    Log.d(
//                        "후.....",
//                        "id: $id, display_name: $displayName, date_taken: " + "$dateTaken, content_uri: $contentUri"
//                    )
                }
            }
        }
    }

    private fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
        SimpleDateFormat("dd.MM.yyyy").let { formatter ->
            formatter.parse("$day.$month.$year")?.time ?: 0
        }

    // 파일 생성
    fun createFile(context: Context, fileName: String, fileType: MediaStoreFileType, fileContents: ByteArray) {

        val contentValues = ContentValues()
        /**
         * image allowed directories are [DCIM, Pictures]
         * audio allowed directories are [Alarms, Music, Notifications, Podcasts, Ringtones]
         * video allowed directories are [DCIM, Movies]
         */
        when(fileType){
            MediaStoreFileType.IMAGE -> {
                contentValues.put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + fileType.pathByDCIM
                )
            }
            MediaStoreFileType.AUDIO -> {
                contentValues.put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + fileType.pathByDCIM
                )
            }
            MediaStoreFileType.VIDEO -> {
                contentValues.put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + fileType.pathByDCIM
                )
            }
        }
        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, fileType.mimeType)
        contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1)

        val uri = context.contentResolver.insert(
            fileType.externalContentUri,
            contentValues
        )

        val parcelFileDescriptor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.contentResolver.openFileDescriptor(uri!!, "w", null)
        } else {
            TODO("VERSION.SDK_INT < KITKAT")
        }

        val fileOutputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)
        fileOutputStream.write(fileContents)
        fileOutputStream.close()

        contentValues.clear()
        contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
        context.contentResolver.update(uri, contentValues, null, null)
    }

    enum class MediaStoreFileType(
        val externalContentUri: Uri,
        val mimeType: String,
        val pathByDCIM: String
    ) {
        IMAGE(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*", "/image"),
        AUDIO(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*", "/audio"),
        VIDEO(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*", "/video");
    }

}







