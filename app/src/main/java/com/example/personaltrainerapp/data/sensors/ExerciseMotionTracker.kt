package com.example.personaltrainerapp.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class ExerciseMotionTracker(
    context: Context,
    private val requiredReps: Int,
    private val estimatedMillisPerRep: Long,
    private val movementThreshold: Float = 2.2f,
    private val requiredMovementRatio: Float = 0.5f,

    // Debug scale compresses long exercise durations for testing.
    // 0.05 means 10 minutes becomes 30 seconds.
    private val debugTimeScale: Float = 0.05f,

    private val onProgressChanged: (
        completedReps: Int,
        requiredReps: Int,
        currentRepMovementRatio: Float
    ) -> Unit,
    private val onCompleted: () -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val handler = Handler(Looper.getMainLooper())

    private var isTracking = false
    private var completedReps = 0

    private var repStartedAt = 0L
    private var lastSensorTime = 0L
    private var movingMillisThisRep = 0L

    private val scaledMillisPerRep: Long
        get() = max(3_000L, (estimatedMillisPerRep * debugTimeScale).toLong())

    private val repCheckRunnable = object : Runnable {
        override fun run() {
            if (!isTracking) return

            val now = System.currentTimeMillis()
            val elapsed = now - repStartedAt

            if (elapsed >= scaledMillisPerRep) {
                finishCurrentRepWindow()
            }

            if (isTracking) {
                handler.postDelayed(this, 250L)
            }
        }
    }

    fun start(): Boolean {
        if (accelerometer == null) return false

        isTracking = true
        completedReps = 0
        startNewRepWindow()

        onProgressChanged(completedReps, requiredReps, 0f)

        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        handler.post(repCheckRunnable)

        return true
    }

    fun stop() {
        if (!isTracking) return

        isTracking = false
        handler.removeCallbacks(repCheckRunnable)
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isTracking) return
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()

        if (lastSensorTime == 0L) {
            lastSensorTime = now
            return
        }

        val delta = now - lastSensorTime
        lastSensorTime = now

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)
        val movement = abs(magnitude - SensorManager.GRAVITY_EARTH)

        if (movement > movementThreshold) {
            movingMillisThisRep += delta
        }

        val currentRatio =
            (movingMillisThisRep.toFloat() / scaledMillisPerRep.toFloat())
                .coerceIn(0f, 1f)

        onProgressChanged(completedReps, requiredReps, currentRatio)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun finishCurrentRepWindow() {
        val movementRatio =
            (movingMillisThisRep.toFloat() / scaledMillisPerRep.toFloat())
                .coerceIn(0f, 1f)

        if (movementRatio >= requiredMovementRatio) {
            completedReps += 1
        }

        onProgressChanged(completedReps, requiredReps, 0f)

        if (completedReps >= requiredReps) {
            stop()
            onCompleted()
        } else {
            startNewRepWindow()
        }
    }

    private fun startNewRepWindow() {
        repStartedAt = System.currentTimeMillis()
        lastSensorTime = 0L
        movingMillisThisRep = 0L
    }
}