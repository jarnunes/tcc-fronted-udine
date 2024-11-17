package com.jarnunes.udinetour.commons

class ExceptionUtils {

    companion object {

        fun getRootCause(e: Throwable): String {
            return getRootCause(e, StringBuilder(), 0)
        }

        private fun getRootCause(e: Throwable, messages: StringBuilder, counter: Int): String {
            messages.append("$counter message: ").append(e.message).append("\n")
            messages.append("$counter localized message: ").append(e.localizedMessage).append("\n\n")
            if (e.cause == null) {
                return messages.toString()
            } else {
                getRootCause(e.cause!!, messages, counter + 1)
            }

            return messages.toString()
        }
    }
}