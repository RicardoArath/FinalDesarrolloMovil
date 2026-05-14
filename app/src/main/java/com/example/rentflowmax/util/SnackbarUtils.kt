package com.example.rentflowmax.util

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Observa un mensaje colocado por el fragment anterior vía
 * `previousBackStackEntry.savedStateHandle["saved_message"]` y lo muestra como Snackbar.
 * Se consume tras mostrar para no repetirse al cambiar configuración.
 */
fun Fragment.observeSavedMessage(rootView: View, anchorView: View? = null) {
    val entry = findNavController().currentBackStackEntry ?: return
    entry.savedStateHandle.getLiveData<String>("saved_message").observe(viewLifecycleOwner) { msg ->
        if (msg != null) {
            val sb = Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT)
            if (anchorView != null) sb.anchorView = anchorView
            sb.show()
            entry.savedStateHandle.remove<String>("saved_message")
        }
    }
}
