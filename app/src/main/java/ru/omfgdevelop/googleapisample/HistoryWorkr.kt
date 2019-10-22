package ru.omfgdevelop.googleapisample

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class HistoryWorker {
    fun fetchHistoryWalks(context: Context) {
        var startTime = 0L
        val storage: StorageInterface = SharedWorker()
        val savedTime: Long = storage.getSavedTime(context)
        val DELTA_TIME_TO_UNITE = 120000
        val FILTER_TIME = 600000
        val MINUTE = 60000
        if (savedTime != 0L) {
//            startTime = savedTime+1
        }
        Log.d("Log", "start time " + startTime);
        val cal = Calendar.getInstance()
        val now = Calendar.getInstance()
        val endTime: Long
        cal.time = now.time
        endTime = cal.timeInMillis
        if (startTime == 0L) {
            cal.add(Calendar.DAY_OF_MONTH, -300)
            startTime = cal.timeInMillis
        }

        Fitness.getRecordingClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.d("Log", "subscribed")
            }
        Fitness.getRecordingClient(
            context,
            GoogleSignIn.getLastSignedInAccount(context)!!
        )
            .listSubscriptions().addOnSuccessListener { subscriptions ->

                Log.d("Log", "subscriptions" + subscriptions.size);


            }


        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .read(DataType.AGGREGATE_MOVE_MINUTES)
            .bucketBySession(1, TimeUnit.MILLISECONDS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        Fitness.getHistoryClient(
            context as MainActivity,
            GoogleSignIn.getLastSignedInAccount(context)!!
        )

            .readData(readRequest)
            .addOnSuccessListener { dataResponse ->
                val bucket = dataResponse.buckets
                Log.d("Log","bucket size "+bucket.size)
                val preWalks: ArrayList<Walk> = ArrayList()
                bucket.withIndex().forEach { it ->
                    it.value.dataSets.withIndex().forEach { buk ->
                        buk.value.dataPoints.forEach {
                            if (it.dataType.fields.contains(Field.FIELD_STEPS)) {
                                val t =
                                    it.getEndTime(TimeUnit.MILLISECONDS) - it.getStartTime(TimeUnit.MILLISECONDS)
                                val walk: Walk = Walk()
                                walk.duration = t
                                walk.steps = it.getValue(Field.FIELD_STEPS).asInt()
                                walk.endTime = it.getEndTime(TimeUnit.MILLISECONDS)
                                walk.startTime = it.getStartTime(TimeUnit.MILLISECONDS)
                                walk.timeStamp = it.getTimestamp(TimeUnit.MILLISECONDS)
                                walk.speed =
                                    ((it.getValue(Field.FIELD_STEPS).asInt() / t.toFloat()) * MINUTE)
                                preWalks.add(walk)
                            }
                        }
                    }


                }

                preWalks.sortBy { it.startTime }
                for (i in 0 until preWalks.size) {
                    Log.d("Log", "start time " + preWalks[i].startTime)
                    Log.d("Log", "end time " + preWalks[i].endTime)
                    if (preWalks[i].endTime < preWalks[i].startTime)
                        Log.d(
                            "Log",
                            "ALLERT end time " + preWalks[i].endTime + "start time " + preWalks[i].startTime
                        )
                }
                val median: Float = getMedian(preWalks)
                val walks = ArrayList<Walk>()
                for (i in 0 until preWalks.size) {
                    if (preWalks[i].speed > median * 0.8)
                        if (i < preWalks.size - 1) {
                            if (preWalks[i + 1].startTime - preWalks[i].endTime < DELTA_TIME_TO_UNITE && (preWalks[i + 1].startTime - preWalks[i].endTime) >= 0) {
                                val t = preWalks[i + 1].endTime - preWalks[i].startTime
                                if (t < 0) {
                                    Log.d("Log", " T" + t)
                                }
                                preWalks[i].steps = preWalks[i].steps + preWalks[i + 1].steps
                                preWalks[i].endTime = preWalks[i + 1].endTime
                                preWalks[i].speed = ((preWalks[i].steps / t.toFloat()) * MINUTE)
                                Log.d(
                                    "Log",
                                    "UNITE staps" + preWalks[i].steps + " speed " + preWalks[i].speed
                                )
                                walks.add(preWalks[i])
                            } else {
                                walks.add(preWalks[i])
                            }
                        } else {
                            walks.add(preWalks[i])
                        }
                }

                walks.forEach {
                    val t =
                        it.endTime - it.startTime
                    if (t > FILTER_TIME) {
                        Log.d(
                            "Log ",
                            " walks pure " + it.steps + " start time " + it.startTime + " end time  " + it.endTime + " time delta " + t
                        )
                    }
//                    Log.d(
//                        "Log ",
//                        " walks raw " + it.steps + " start time " + it.startTime + " end time  " + it.endTime + " time delta " + t
//                    )


                }
            }.addOnFailureListener {
                Log.d("Log", "onfailure " + it.message)
            }


    }

    private fun getMedian(sortWalks: ArrayList<Walk>): Float {
        sortWalks.sortBy { it.speed }
        if (sortWalks.size <= 0)
            return 0f
        val median = if (sortWalks.size % 2 == 1) {
            sortWalks[sortWalks.size / 2].speed
        } else {
            ((sortWalks[sortWalks.size / 2].speed + sortWalks[(sortWalks.size / 2) + 1].speed) / 2)

        }
        return median
    }


}