package com.tatanstudios.astropollocliente.componentes

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.tatanstudios.astropollocliente.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState as animarCompottie

/**
 * Envoltorio de Compottie (Lottie multiplataforma) con los mismos nombres que usaba
 * com.airbnb.lottie.compose, para que las vistas casi no cambien.
 * Las animaciones están en composeResources/files (antes res/raw).
 */
object LottieConstants {
    const val IterateForever = Compottie.IterateForever
}

/** Reemplaza `rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.x))`. */
@Composable
fun rememberLottieArchivo(ruta: String): State<LottieComposition?> =
    rememberLottieComposition {
        LottieCompositionSpec.JsonString(Res.readBytes(ruta).decodeToString())
    }

@Composable
fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    iterations: Int = 1,
): State<Float> = animarCompottie(composition = composition, iterations = iterations)

@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = rememberLottiePainter(composition = composition, progress = progress),
        contentDescription = null,
        modifier = modifier,
    )
}
