package com.example.securtiy_home_2

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.securtiy_home_2.databinding.FragmentSettingDetailPgeBinding
import com.example.securtiy_home_2.security_home_service.*
import com.example.securtiy_home_2.security_home_service.model.Setting
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.Base64.getEncoder
import kotlin.collections.ArrayList


class SettingDetailPge : Fragment() {

    private var _binding: FragmentSettingDetailPgeBinding? = null
    private val binding get() = _binding!!


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingDetailPgeBinding.inflate(inflater, container, false)

        val arrayList = ArrayList<Setting>()
        binding.title5Tv.text = arguments?.get("title") as String

        when(arguments?.get("settingDetail")){
            SettingDetail.EmailVerify -> {
                arrayList.add(Setting(R.drawable.surf, "askes4102@gmail.com", "Not Verify", SettingType.Text))
                binding.settingDetilRv.visibility = View.VISIBLE
            }
            SettingDetail.EditProfile -> {
                SecurityHomeService(requireContext()).getProfile({
                    arrayList.add(Setting(R.drawable.name, "Name", it.Name.toString(), SettingType.TextBox))
                    arrayList.add(Setting(R.drawable.description, "Description", it.Description.toString(), SettingType.TextBox))

                    if(it.Born.toString().isEmpty()){
                        arrayList.add(Setting(R.drawable.born, "Born", "null", SettingType.DateSelector))
                    }
                    else{
                        arrayList.add(Setting(R.drawable.born, "Born", "unknown", SettingType.DateSelector))
                    }

                    val gender: ArrayList<Any> = arrayListOf("Male", "Female", "Gay")
                    if(it.Gender.toString().isEmpty()){
                        gender.add(0, "Unknown")
                        arrayList.add(Setting(R.drawable.gender, "Gender", "0", SettingType.Selector, null, gender))
                    }
                    else{
                        arrayList.add(Setting(R.drawable.gender, "Gender", it.Gender.toString(), SettingType.Selector, null, gender))
                    }

                    //binding.profileImg.setImageBitmap(it.Image as Bitmap)

                    binding.settingDetilRv.visibility = View.VISIBLE
                    binding.profileImg.visibility = View.VISIBLE
                }, {
                    view?.let { view ->
                        when(it){
                            NetworkStatus.NoOpenInternet -> {
                                Snackbar.make(view, "No Open Internet: Please open the internet", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                            NetworkStatus.ServerError -> {
                                Snackbar.make(view, "Server Error: Please wait a second and try again", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                            NetworkStatus.Failed -> {
                                Snackbar.make(view, "Device Login Failed", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show()
                            }
                        }
                    }
                })
            }
        }

        val gridLayoutManager = GridLayoutManager(context, 1, LinearLayoutManager.VERTICAL, false)
        val adapter = SettingAdapter(binding.root.context, R.layout.fragment_setting_detail_pge, arrayList)
        binding.settingDetilRv.layoutManager = gridLayoutManager
        binding.settingDetilRv.adapter = adapter

        binding.backIv.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }

        binding.saveBtn.setOnClickListener {
            val loading = LoadingDialog(this)
            loading.startLoading()

            val imageBitmap = (binding.profileImg.drawable as BitmapDrawable).bitmap
            val imageOutputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream)
            val imageBytes = imageOutputStream.toByteArray()

            val bass64String = getEncoder().encodeToString(imageBytes)
            Log.e("A~ ", bass64String)

            Handler(Looper.getMainLooper()).postDelayed({
                loading.loadingOk()
            }, 3000)
            //Log.e("A~ ", adapter.getSetValue().toString())
        }

        binding.profileImg.setOnClickListener{
            AlertDialog.Builder(requireContext())
                .setTitle("Set Profile Image")
                .setItems(arrayOf("Take a picture", "Choose from gallery")) { _, position ->
                    when(position){
                        0 -> {
                            val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            requireActivity().startActivityFromFragment(this, camera, TAKE_PICTURE)
                        }
                        1 -> {
                            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI).setType("image/*")
                            requireActivity().startActivityFromFragment(this, gallery, PICK_GALLERY)
                        }
                    }
                }.show()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun cropImage(image: Uri){
        Log.e("A~ ", image.toString())

        UCrop.of(image, Uri.fromFile(File(requireActivity().cacheDir, "SH_2_IMG_${System.currentTimeMillis()}.jpeg")))
            .withAspectRatio(1F, 1F)
            .withMaxResultSize(512, 512)
            .start(requireContext(), this)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when(requestCode){
                UCrop.REQUEST_CROP -> binding.profileImg.setImageURI(UCrop.getOutput(data!!))
                PICK_GALLERY -> {
                    val galleryImage = data?.data
                    if(galleryImage != null){
                        cropImage(galleryImage)
                    }
                }
                TAKE_PICTURE -> {
                    val cameraImage = data?.extras?.get("data")

                    if(cameraImage != null) {
                        val image = cameraImage as Bitmap
                        val bytes = ByteArrayOutputStream()
                        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

                        val filename = "SH_2_IMG_${System.currentTimeMillis()}.jpeg"
                        var fos: OutputStream? = null
                        var imageUri: Uri? = null
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_PICTURES
                            )
                            put(MediaStore.Video.Media.IS_PENDING, 1)
                        }

                        val contentResolver = requireContext().contentResolver

                        contentResolver.also { resolver ->
                            imageUri = resolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                            fos = imageUri.let { uri ->
                                resolver.openOutputStream(uri!!)
                            }
                        }

                        fos?.use { outputStream ->
                            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }

                        contentValues.clear()
                        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                        contentResolver.update(imageUri!!, contentValues, null, null)

                        cropImage(imageUri!!)
                    }
                }
            }
        }
    }

    companion object{
        const val PICK_GALLERY = 87
        const val TAKE_PICTURE = 187
    }
}
