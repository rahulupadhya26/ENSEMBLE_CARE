package ensemblecare.csardent.com.controller

import androidx.fragment.app.Fragment


interface IFragment {

    fun addFragment(fragment: Fragment, frameId: Int, fragmentName: String)
    fun replaceFragment(fragment: Fragment, frameId: Int, fragmentName: String)
    fun replaceFragmentNoBackStack(fragment: Fragment, frameId: Int, fragmentName: String)
    fun popBackStack()
}