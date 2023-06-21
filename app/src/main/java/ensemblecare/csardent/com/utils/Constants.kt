package ensemblecare.csardent.com.utils

import android.content.Context

object Constants {
    fun getJson(context: Context): String {
        return context.assets.open("healthrelated_questions.json").bufferedReader()
            .use { it.readText() }
    }

    fun clearJson(context: Context){
        
    }
}