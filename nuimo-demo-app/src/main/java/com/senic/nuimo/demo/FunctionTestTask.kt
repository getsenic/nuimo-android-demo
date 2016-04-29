package com.senic.nuimo.demo

import android.os.AsyncTask
import com.senic.nuimo.*

val rotationTestOnly = false

class FunctionTestTask(val controller: NuimoController) : NuimoControllerListener {

    var testGesture = NuimoGesture.BUTTON_PRESS
    var rotationDirection = 1 // > 0: clockwise, < 0: counterclockwise
    var value = 0
    val numberOfSwipesToSucceed = 1
    val numberOfFlightsToSucceed = 1

    fun start() {
        startTest(if (rotationTestOnly) { NuimoGesture.ROTATE } else { NuimoGesture.BUTTON_PRESS })
    }

    private fun finish() {
        controller.displayLedMatrix(NuimoLedMatrix.emoticonHappyMatrix(), 10.0)
    }

    override fun onGestureEvent(event: NuimoGestureEvent) {
        if (testGesture != event.gesture) { return }

        when (testGesture) {
            NuimoGesture.BUTTON_PRESS -> startTest(NuimoGesture.BUTTON_RELEASE)
            NuimoGesture.BUTTON_RELEASE -> startTest(NuimoGesture.ROTATE, 1)
            NuimoGesture.ROTATE -> {
                value += event.value ?: 0
                if (value * rotationDirection < 2860) { return }
                if (rotationDirection > 0) {
                    startTest(NuimoGesture.ROTATE, -1)
                }
                else {
                    if (rotationTestOnly) {
                        finish()
                    }
                    else {
                        startTest(NuimoGesture.SWIPE_LEFT)
                    }
                }
            }
            NuimoGesture.SWIPE_LEFT -> {
                if (++value >= numberOfSwipesToSucceed) { startTest(NuimoGesture.SWIPE_RIGHT) }
            }
            NuimoGesture.SWIPE_RIGHT -> {
                if (++value >= numberOfSwipesToSucceed) { startTest(NuimoGesture.SWIPE_UP) }
            }
            NuimoGesture.SWIPE_UP -> {
                if (++value >= numberOfSwipesToSucceed) { startTest(NuimoGesture.SWIPE_DOWN) }
            }
            NuimoGesture.SWIPE_DOWN -> {
                if (++value >= numberOfSwipesToSucceed) { startTest(NuimoGesture.FLY_LEFT) }
            }
            NuimoGesture.FLY_LEFT -> {
                if (++value >= numberOfFlightsToSucceed) { startTest(NuimoGesture.FLY_RIGHT) }
            }
            NuimoGesture.FLY_RIGHT -> {
                if (++value >= numberOfFlightsToSucceed) { finish() }
            }
        }
    }

    private fun startTest(gesture: NuimoGesture, rotationDirection: Int = 0) {
        testGesture = gesture
        this.rotationDirection = rotationDirection
        value = 0
        controller.displayLedMatrix(
                when(gesture) {
                    NuimoGesture.BUTTON_PRESS   -> NuimoLedMatrix.pressButtonMatrix()
                    NuimoGesture.BUTTON_RELEASE -> NuimoLedMatrix.releaseButtonMatrix()
                    NuimoGesture.ROTATE         -> NuimoLedMatrix(if (rotationDirection > 0) { NuimoLedMatrix.rotateRightMatrixString() } else { NuimoLedMatrix.rotateLeftMatrixString() })
                    NuimoGesture.SWIPE_LEFT     -> NuimoLedMatrix(NuimoLedMatrix.swipeLeftMatrixString())
                    NuimoGesture.SWIPE_RIGHT    -> NuimoLedMatrix(NuimoLedMatrix.swipeRightMatrixString())
                    NuimoGesture.SWIPE_UP       -> NuimoLedMatrix(NuimoLedMatrix.swipeUpMatrixString())
                    NuimoGesture.SWIPE_DOWN     -> NuimoLedMatrix(NuimoLedMatrix.swipeDownMatrixString())
                    NuimoGesture.FLY_LEFT       -> NuimoLedMatrix(NuimoLedMatrix.flyLeftMatrixString())
                    NuimoGesture.FLY_RIGHT      -> NuimoLedMatrix(NuimoLedMatrix.flyRightMatrixString())
                    else -> NuimoLedMatrix("")
                }, 0.0
        )
    }
}

private fun NuimoLedMatrix.Companion.pressButtonMatrix() = NuimoLedMatrix(
        "         " +
        "***  *** " +
        "*  * *  *" +
        "*  * *  *" +
        "***  *** " +
        "*    *  *" +
        "*    *  *" +
        "*    *  *" +
        "         ")


private fun NuimoLedMatrix.Companion.releaseButtonMatrix() = NuimoLedMatrix(
        "         " +
        "***  ****" +
        "*  * *   " +
        "*  * *   " +
        "***  *** " +
        "*  * *   " +
        "*  * *   " +
        "*  * ****" +
        "         ")

private fun NuimoLedMatrix.Companion.rotateRightMatrixString() =
        "  ***    " +
        " *   *   " +
        "*     *  " +
        "*     *  " +
        "*   *****" +
        " *   *** " +
        "      *  " +
        "         " +
        "         "

private fun NuimoLedMatrix.Companion.rotateLeftMatrixString() =
        "    ***  " +
        "   *   * " +
        "  *     *" +
        "  *     *" +
        "*****   *" +
        " ***   * " +
        "  *      " +
        "         " +
        "         "

private fun NuimoLedMatrix.Companion.swipeLeftMatrixString() =
        "    *    " +
        "   **    " +
        "  ****** " +
        " ******* " +
        "  ****** " +
        "   **    " +
        "    *    " +
        "         " +
        "         "

private fun NuimoLedMatrix.Companion.swipeRightMatrixString() =
        "    *    " +
        "    **   " +
        " ******  " +
        " ******* " +
        " ******  " +
        "    **   " +
        "    *    " +
        "         " +
        "         "

private fun NuimoLedMatrix.Companion.swipeUpMatrixString() =
        "    *    " +
        "   ***   " +
        "  *****  " +
        " ******* " +
        "   ***   " +
        "   ***   " +
        "   ***   " +
        "         " +
        "         "

private fun NuimoLedMatrix.Companion.swipeDownMatrixString() =
        "   ***   " +
        "   ***   " +
        "   ***   " +
        " ******* " +
        "  *****  " +
        "   ***   " +
        "    *    " +
        "         " +
        "         "

private fun NuimoLedMatrix.Companion.flyLeftMatrixString() =
        "      *  " +
        "     **  " +
        "    *** *" +
        " ********" +
        "*********" +
        " ********" +
        "    *** *" +
        "     **  " +
        "      *  "

private fun NuimoLedMatrix.Companion.flyRightMatrixString() =
        "  *      " +
        "  **     " +
        "* ***    " +
        "******** " +
        "*********" +
        "******** " +
        "* ***    " +
        "  **     " +
        "  *      "


private fun NuimoLedMatrix.Companion.emoticonHappyMatrix() = NuimoLedMatrix(
        "***   ***" +
        " **   ** " +
        "         " +
        "    *    " +
        "    *    " +
        "*       *" +
        " *     * " +
        "  *****  " +
        "         ")
