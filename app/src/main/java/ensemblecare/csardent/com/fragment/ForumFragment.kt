package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.ForumAdapter
import ensemblecare.csardent.com.controller.OnForumItemClickListener
import ensemblecare.csardent.com.data.ForumData
import ensemblecare.csardent.com.databinding.FragmentForumBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ForumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ForumFragment : BaseFragment(), OnForumItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentForumBinding
    private var forumList: ArrayList<ForumData> = arrayListOf()
    private var adapter: ForumAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_forum
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        getForumData()

        binding.etForumSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty())
                    filterOne(editable.toString())
                else
                    binding.recyclerViewForumChatRoom.apply {
                        layoutManager =
                            LinearLayoutManager(
                                requireActivity(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        adapter = ForumAdapter(
                            mActivity!!,
                            forumList, this@ForumFragment
                        )
                    }
            }
        })

    }

    private fun getForumData() {
        binding.layoutNoForum.visibility = View.GONE
        binding.layoutForumList.visibility = View.VISIBLE
        binding.shimmerForum.startShimmer()
        binding.shimmerForum.visibility = View.VISIBLE
        binding.recyclerViewForumChatRoom.visibility = View.GONE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getForumData(
                        "PI0074",
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerForum.stopShimmer()
                            binding.shimmerForum.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val forumType: Type =
                                object : TypeToken<ArrayList<ForumData?>?>() {}.type
                            forumList = Gson().fromJson(responseBody, forumType)
                            if (forumList.isNotEmpty()) {
                                binding.layoutForumList.visibility = View.VISIBLE
                                binding.recyclerViewForumChatRoom.visibility = View.VISIBLE
                                binding.layoutNoForum.visibility = View.GONE
                                binding.recyclerViewForumChatRoom.apply {
                                    layoutManager = LinearLayoutManager(
                                        requireActivity(),
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )
                                    adapter = ForumAdapter(
                                        requireActivity(),
                                        forumList, this@ForumFragment
                                    )
                                }
                            } else {
                                binding.recyclerViewForumChatRoom.visibility = View.GONE
                                binding.layoutForumList.visibility = View.GONE
                                binding.layoutNoForum.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerForum.stopShimmer()
                            binding.shimmerForum.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            binding.shimmerForum.stopShimmer()
                            binding.shimmerForum.visibility = View.GONE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun filterOne(text: String) {
        val filteredNames = ArrayList<ForumData>()
        forumList.filterTo(filteredNames) {
            it.name.toLowerCase().contains(text.toLowerCase())
        }

        if (filteredNames.isEmpty()) {
            binding.recyclerViewForumChatRoom.apply {
                layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                adapter = ForumAdapter(
                    mActivity!!,
                    forumList, this@ForumFragment
                )
            }
        } else {
            val layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = ForumAdapter(
                mActivity!!,
                forumList, this
            )
            adapter!!.filterList(filteredNames)
            binding.recyclerViewForumChatRoom.layoutManager = layoutManager
            binding.recyclerViewForumChatRoom.adapter = adapter
        }
    }

    private fun getChatRoomToken(myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getChatRoomToken(getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ForumFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ForumFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Forum"
    }

    override fun onForumItemClickListener(forumData: ForumData) {
        getChatRoomToken { response ->
            replaceFragment(
                ForumChatRoomFragment.newInstance(
                    forumData,
                    JSONObject(response!!).getString("rtm_token")
                ),
                R.id.layout_home,
                ForumChatRoomFragment.TAG
            )
        }
    }
}