package com.jarnunes.udinetour.commons

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.TextView

class TextUtils {

    companion object{
        fun applyBoldToAsterisks(textView: TextView, inputText: String) {
            // Substituir os asteriscos para exibição sem eles
            val plainText = inputText.replace("\\*\\*(.*?)\\*\\*".toRegex(), "$1")
            val spannableString = SpannableString(plainText)

            // Regex para encontrar texto entre ** no texto original
            val regex = "\\*\\*(.*?)\\*\\*".toRegex()

            // Calcula os índices com base no texto ajustado
            var offset = 0
            regex.findAll(inputText).forEach { matchResult ->
                val boldText = matchResult.groupValues[1]
                val start = plainText.indexOf(boldText, offset)
                val end = start + boldText.length

                // Aplica negrito ao conteúdo
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                offset = end
            }

            textView.text = spannableString
        }
    }
}